package util;

import caseloader.kad.Urls;
import exceptions.DataRetrievingError;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import proxy.ProxyInfo;
import proxy.ProxyList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public abstract class HttpDownloader {
    public static final String USER_AGENT = "Mozilla/5.0 (compatible; YandexBot/3.0; +http://yandex.com/bots)";
    private static final int REQUEST_TIMEOUT = 5 * 1000;
    private static final boolean USE_PROXY_DEFAULT = true;
    private static final ConcurrentHashMap<String, Long> LAST_TIMES = new ConcurrentHashMap<>();
    private static final long WAIT_DELTA = 3 * 1000;
    private static final String DEFAULT_ENCODING = "UTF-8";
    private static int getRetryCount = 1;
    private static int postRetryCount = 1;
    private static Logger logger = MyLogger.getLogger(HttpDownloader.class.toString());

    private static void checkSleep(String hostname) throws InterruptedException {
        Long lastTime = LAST_TIMES.get(hostname);
        if (lastTime != null) {
            long time = System.currentTimeMillis();
            long delta = time - lastTime;
            if (delta < WAIT_DELTA) {
                logger.fine("Sleeping for " + (WAIT_DELTA - delta));
                Thread.sleep(WAIT_DELTA - delta);
                logger.fine("Sleep finished");
            }
        }
        LAST_TIMES.put(hostname, System.currentTimeMillis());
    }

    private static void updateTime(String hostname) {
        LAST_TIMES.put(hostname, System.currentTimeMillis());
    }

    public static String get(String url, boolean useProxy) throws IOException, DataRetrievingError, InterruptedException {
        return get(url, null, null, useProxy, DEFAULT_ENCODING);
    }

    public static String get(String url) throws IOException, DataRetrievingError, InterruptedException {
        return get(url, null, null, USE_PROXY_DEFAULT, DEFAULT_ENCODING);
    }

    public static String get(String url, String encoding) throws IOException, DataRetrievingError, InterruptedException {
        return get(url, null, null, USE_PROXY_DEFAULT, encoding);
    }

    public static String get(String url, List<NameValuePair> params, Map<String, String> headers) throws IOException, DataRetrievingError, InterruptedException {
        return get(url, params, headers, USE_PROXY_DEFAULT, DEFAULT_ENCODING);
    }

    public static String get(String url, List<NameValuePair> params, Map<String, String> headers, boolean useProxy) throws IOException, DataRetrievingError, InterruptedException {
        return get(url, params, headers, useProxy, DEFAULT_ENCODING);
    }

    public static String get(String url, List<NameValuePair> params, Map<String, String> headers, boolean useProxy, String encoding) throws IOException, DataRetrievingError, InterruptedException {
        URIBuilder uriBuilder = buildUriBuilder(url, encoding);

        if (params != null) {
            uriBuilder.setParameters(params);
        }

        URI uri;
        try {
            uri = uriBuilder.build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }



        HttpGet request = new HttpGet(getPathAndQuery(uri));
        request.setConfig(buildRequestConfig(useProxy));
        setHeaders(request, headers);

        checkSleep(uriBuilder.getHost());

        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpResponse response = client.execute(buildTarget(uriBuilder), request);
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Thread is already stopped");
            }
            updateTime(uriBuilder.getHost());
            getRetryCount = 1;
            return getResponse(response, encoding);
        } catch (HttpHostConnectException | ConnectTimeoutException | NoHttpResponseException e) {
            logger.warning("Exception happened. Retry #" + getRetryCount++);
            if (getRetryCount < 3) {
                updateTime(uriBuilder.getHost());
                return get(url, params, headers, useProxy, encoding);
            }
            logger.severe("Exception happened again after " + getRetryCount + "retries");
            throw e;
        }

    }

    public static String post(String url, List<NameValuePair> formData, Map<String, String> headers) throws IOException, DataRetrievingError, InterruptedException {
        return post(url, formData, headers, USE_PROXY_DEFAULT);
    }

    public static String post(String url, List<NameValuePair> formData, Map<String, String> headers, boolean useProxy) throws IOException, DataRetrievingError, InterruptedException {
        return post(url, new UrlEncodedFormEntity(formData, "UTF-8"), headers, useProxy);
    }

    public static String post(String url, String data, Map<String, String> headers) throws IOException, DataRetrievingError, InterruptedException {
        return post(url, data, headers, USE_PROXY_DEFAULT);
    }

    public static String post(String url, String data, Map<String, String> headers, boolean useProxy) throws IOException, DataRetrievingError, InterruptedException {
        return post(url, new StringEntity(data, "UTF-8"), headers, useProxy);
    }

    public static String post(String url, AbstractHttpEntity data, Map<String, String> headers, boolean useProxy) throws IOException, DataRetrievingError, InterruptedException {
        URIBuilder uriBuilder = buildUriBuilder(url);

        assert data != null;

        URI uri;
        try {
            uri = uriBuilder.build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        HttpPost request = new HttpPost(getPathAndQuery(uri));
        request.setEntity(data);
        request.setConfig(buildRequestConfig(useProxy));
        setHeaders(request, headers);

        checkSleep(uriBuilder.getHost());

        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpResponse response = client.execute(buildTarget(uriBuilder), request);
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Thread is already stopped");
            }
            updateTime(uriBuilder.getHost());
            postRetryCount = 1;
            return getResponse(response, DEFAULT_ENCODING);
        } catch (HttpHostConnectException | ConnectTimeoutException | NoHttpResponseException e) {
            logger.warning("Exception happened. Retry #" + postRetryCount++);
            if (postRetryCount < 3)
                return post(url, data, headers, useProxy);
            logger.severe("Exception happened again after " + postRetryCount + "retries");
            throw e;
        }
    }

    private static String getPathAndQuery(URI uri) {
        String query = uri.getRawQuery() == null ? "" : uri.getRawQuery();
        if (query.equals(""))
            return uri.getPath();
        else
            return uri.getPath() + "?" + query;
    }

    private static URIBuilder buildUriBuilder(String url) throws DataRetrievingError {
        return buildUriBuilder(url, DEFAULT_ENCODING);
    }

    public static URIBuilder buildUriBuilder(String url, String encoding) throws DataRetrievingError {
        URIBuilder uriBuilder;
        try {
            uriBuilder = new URIBuilder(url);
        } catch (URISyntaxException e) {
            throw new DataRetrievingError(e);
        }
        uriBuilder.setCharset(Charset.forName(encoding));
        return uriBuilder;
    }

    private static HttpHost buildTarget(URIBuilder uriBuilder) {
        return new HttpHost(uriBuilder.getHost(), uriBuilder.getPort(), uriBuilder.getScheme());
    }

    private static RequestConfig buildRequestConfig(boolean useProxy) throws InterruptedException {
        if (useProxy) {
            ProxyInfo proxyInfo = ProxyList.instance().getNext();
            HttpHost proxy = new HttpHost(proxyInfo.getIp(), proxyInfo.getPort());
            return RequestConfig.custom().setProxy(proxy).setConnectTimeout(REQUEST_TIMEOUT).build();
        } else {
            return RequestConfig.custom().setConnectTimeout(REQUEST_TIMEOUT).build();
        }
    }

    private static String getResponse(HttpResponse response, String encoding) throws IOException, InterruptedException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), encoding));

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Thread is already stopped");
            }
            sb.append(line);
        }
        return sb.toString();
    }

    private static void setHeaders(HttpRequestBase request, Map<String, String> headers) {
        if (headers != null) {
            Set<String> hs = headers.keySet();
            for (String key : hs) {
                request.setHeader(key, headers.get(key));
            }
        }
        request.setHeader("User-Agent", USER_AGENT);
    }

    public static void main(String[] args) throws IOException, DataRetrievingError, InterruptedException {
        String res = HttpDownloader.get(Urls.KAD_HOME);
        System.out.println(res);
    }
}
