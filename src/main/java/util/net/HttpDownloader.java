package util.net;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import proxy.ProxyInfo;
import proxy.ProxyList;
import util.BasicPair;
import util.MyLogger;
import util.Pair;
import util.ThreadPool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpDownloader {
    private static final String USER_AGENT = "Mozilla/5.0 (compatible; YandexBot/3.0; +http://yandex.com/bots)";
    private static final int REQUEST_TIMEOUT = 20 * 1000;
    private static final long WAIT_DELTA = 2 * 1000;
    private static final long KAD_WAIT_DELTA = 30 * 1000;
    private static final boolean USE_PROXY_DEFAULT = true;
    private static final String DEFAULT_ENCODING = "UTF-8";
    private final ConcurrentHashMap<String, Long> LAST_TIMES = new ConcurrentHashMap<>();

    private static final String GOOGLE_HOST = "www.google.ru";
    private static final String YANDEX_HOST = "www.yandex.ru";
    private static final String KAD_ARBITR_HOST = "kad.arbitr.ru";

    private static HttpDownloader instance = null;
    private final ThreadPool pool = new ThreadPool(4);
    private final Logger logger = MyLogger.getLogger(HttpDownloader.class.toString());

    private HttpDownloader() {
    }

    public static HttpDownloader i() {
        if (instance == null) {
            instance = new HttpDownloader();
        }
        return instance;
    }

    public void stop() {
        pool.stopExecution();
    }

    private void checkSleep(String hostname) throws InterruptedException {
        long waitDelta;
        if (hostname.equalsIgnoreCase(KAD_ARBITR_HOST)) {
            waitDelta = KAD_WAIT_DELTA;
        } else {
            waitDelta = WAIT_DELTA;
        }

        Long lastTime = LAST_TIMES.get(hostname);
        if (lastTime != null) {
            long time = System.currentTimeMillis();
            long delta = time - lastTime;
            if (delta < waitDelta) {
                logger.fine("Sleeping for " + (waitDelta - delta));
                Thread.sleep(waitDelta - delta);
                logger.fine("Sleep finished");
            }
        }
        LAST_TIMES.put(hostname, System.currentTimeMillis());
    }

    private void updateTime(String hostname) {
        LAST_TIMES.put(hostname, System.currentTimeMillis());
    }

    public String get(String url, boolean useProxy) throws MalformedUrlException, InterruptedException {
        return get(url, null, null, useProxy, DEFAULT_ENCODING);
    }

    public String get(String url) throws MalformedUrlException, InterruptedException {
        return get(url, null, null, USE_PROXY_DEFAULT, DEFAULT_ENCODING);
    }

    public String get(String url, String encoding) throws MalformedUrlException, InterruptedException {
        return get(url, null, null, USE_PROXY_DEFAULT, encoding);
    }

    public String get(String url, List<NameValuePair> params, Map<String, String> headers) throws MalformedUrlException, InterruptedException {
        return get(url, params, headers, USE_PROXY_DEFAULT, DEFAULT_ENCODING);
    }

    public String get(String url, List<NameValuePair> params, Map<String, String> headers, boolean useProxy) throws MalformedUrlException, InterruptedException {
        return get(url, params, headers, useProxy, DEFAULT_ENCODING);
    }

    public String get(String url, List<NameValuePair> params, Map<String, String> headers, boolean useProxy, String encoding) throws MalformedUrlException, InterruptedException {
        return get(url, params, headers, useProxy, encoding, 1);
    }

    private String get(String url, List<NameValuePair> params, Map<String, String> headers, boolean useProxy, String encoding, int retryNo) throws MalformedUrlException, InterruptedException {
        URIBuilder uriBuilder = buildUriBuilder(url, encoding);

        if (params != null) {
            uriBuilder.setParameters(params);
        }

        URI uri;
        try {
            uri = uriBuilder.build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new MalformedUrlException(e);
        }

        HttpGet request = new HttpGet(getPathAndQuery(uri));
        Pair<RequestConfig, ProxyInfo> config = buildRequestConfig(uri.getHost(), useProxy);
        request.setConfig(config.getFirst());
        setHeaders(request, headers);

        checkSleep(uriBuilder.getHost());

        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            Future<HttpResponse> responseFuture = pool.submit(() -> {
                try {
                    return client.execute(buildTarget(uriBuilder), request);
                } catch (IOException e) {
                    return null;
                }
            });
            HttpResponse response = responseFuture.get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);

            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }

            if (response == null)
                throw new DataRetrievingError(String.format("No response from the get request to %s", uri.getHost()));

            updateTime(uriBuilder.getHost());
            String result = getResponse(response, encoding);

            if (useProxy && uriBuilder.getHost().equals(GOOGLE_HOST)) {
                checkForBan(result, config.getSecond());
            }

            return result;
        } catch (DataRetrievingError | IOException | TimeoutException e) {
            if (retryNo <= 3) {
                logger.warning("Exception happened. Retry #" + retryNo);
                Thread.sleep(100);
                updateTime(uriBuilder.getHost());
                return get(url, params, headers, useProxy, encoding, retryNo + 1);
            }
            logger.severe("Exception happened again after " + (retryNo - 1) + " retries");
            return null;
        } catch (ExecutionException e) {
            logger.log(Level.SEVERE, "ExecutionException", e);
            return null;
        }

    }

    private void checkForBan(String response, ProxyInfo usedProxy) throws InterruptedException {
        usedProxy.decreaseReliability();

        Element captcha = Jsoup.parse(response)
                               .body()
                               .getElementById("captcha");
        if (captcha != null) {
            usedProxy.decreaseReliability(4);
            logger.info(String.format("Banned proxy. unreliability=%d, ip=%s, port=%d", usedProxy.getUnreliability(), usedProxy.getIp(), usedProxy.getPort()));
        }

        ProxyList.instance().returnGoogleProxy(usedProxy);
    }

    public String post(String url, List<NameValuePair> formData, Map<String, String> headers) throws MalformedUrlException, InterruptedException {
        return post(url, formData, headers, USE_PROXY_DEFAULT);
    }

    public String post(String url, List<NameValuePair> formData, Map<String, String> headers, boolean useProxy) throws MalformedUrlException, InterruptedException {
        try {
            return post(url, new UrlEncodedFormEntity(formData, DEFAULT_ENCODING), headers, useProxy);
        } catch (UnsupportedEncodingException ignored) {
            return null;
        }
    }

    public String post(String url, String data, Map<String, String> headers) throws MalformedUrlException, InterruptedException {
        return post(url, data, headers, USE_PROXY_DEFAULT);
    }

    public String post(String url, String data, Map<String, String> headers, boolean useProxy) throws MalformedUrlException, InterruptedException {
        return post(url, new StringEntity(data, DEFAULT_ENCODING), headers, useProxy);
    }

    public String post(String url, AbstractHttpEntity data, Map<String, String> headers, boolean useProxy) throws MalformedUrlException, InterruptedException {
        return post(url, data, headers, useProxy, 1);
    }

    private String post(String url, AbstractHttpEntity data, Map<String, String> headers, boolean useProxy, int retryNo) throws InterruptedException, MalformedUrlException {
        URIBuilder uriBuilder = buildUriBuilder(url);

        assert data != null;

        URI uri;
        try {
            uri = uriBuilder.build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new MalformedUrlException(e);
        }

        HttpPost request = new HttpPost(getPathAndQuery(uri));
        request.setEntity(data);
        request.setConfig(buildRequestConfig(uri.getHost(), useProxy).getFirst());
        setHeaders(request, headers);

        checkSleep(uriBuilder.getHost());

        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            Future<HttpResponse> responseFuture = pool.submit(() -> {
                try {
                    return client.execute(buildTarget(uriBuilder), request);
                } catch (IOException e) {
                    return null;
                }
            });
            HttpResponse response = responseFuture.get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);

            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }

            if (response == null)
                throw new DataRetrievingError(String.format("No response from the post request to %s", uri.getHost()));

            updateTime(uriBuilder.getHost());
            return getResponse(response, DEFAULT_ENCODING);
        } catch (IOException | DataRetrievingError | TimeoutException e) {
            if (retryNo <= 3) {
                logger.warning("Exception happened. Retry #" + retryNo);
                Thread.sleep(50);
                updateTime(uriBuilder.getHost());
                return post(url, data, headers, useProxy, retryNo + 1);
            }
            logger.severe("Exception happened again after " + (retryNo - 1) + " retries");
            return null;
        } catch (ExecutionException e) {
            logger.log(Level.SEVERE, "ExecutionException", e);
            return null;
        }
    }

    private String getPathAndQuery(URI uri) {
        String query = uri.getRawQuery() == null ? "" : uri.getRawQuery();
        if (query.equals(""))
            return uri.getPath();
        else
            return uri.getPath() + "?" + query;
    }

    private URIBuilder buildUriBuilder(String url) throws MalformedUrlException {
        return buildUriBuilder(url, DEFAULT_ENCODING);
    }

    private URIBuilder buildUriBuilder(String url, String encoding) throws MalformedUrlException {
        URIBuilder uriBuilder;
        try {
            uriBuilder = new URIBuilder(url);
        } catch (URISyntaxException e) {
            throw new MalformedUrlException(e);
        }
        uriBuilder.setCharset(Charset.forName(encoding));
        return uriBuilder;
    }

    private HttpHost buildTarget(URIBuilder uriBuilder) {
        return new HttpHost(uriBuilder.getHost(), uriBuilder.getPort(), uriBuilder.getScheme());
    }

    private Pair<RequestConfig, ProxyInfo> buildRequestConfig(String hostname, boolean useProxy) throws InterruptedException {
        RequestConfig.Builder b = RequestConfig.custom().setConnectTimeout(REQUEST_TIMEOUT).setSocketTimeout(REQUEST_TIMEOUT);
        if (useProxy) {
            ProxyInfo proxyInfo;
            if (hostname.equals(GOOGLE_HOST) || hostname.equals(YANDEX_HOST)) {
                proxyInfo = ProxyList.instance().getGoogleNext();
            } else {
                proxyInfo = ProxyList.instance().getNext();
            }
            HttpHost proxy = new HttpHost(proxyInfo.getIp(), proxyInfo.getPort());
            return new BasicPair<>(b.setProxy(proxy).build(), proxyInfo);
        } else {
            return new BasicPair<>(b.build(), null);
        }
    }

    private String getResponse(HttpResponse response, String encoding) throws IOException, InterruptedException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), encoding));

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            sb.append(line);
        }
        return sb.toString();
    }

    private void setHeaders(HttpRequestBase request, Map<String, String> headers) {
        if (headers != null) {
            Set<String> hs = headers.keySet();
            for (String key : hs) {
                request.setHeader(key, headers.get(key));
            }
        }
        request.setHeader("User-Agent", USER_AGENT);
    }

}
