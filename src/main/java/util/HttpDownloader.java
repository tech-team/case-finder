package util;

import caseloader.kad.Urls;
import exceptions.DataRetrievingError;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
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
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public abstract class HttpDownloader {
    public static final String USER_AGENT = "Test UserAgent 1.0";
    private static final int REQUEST_TIMEOUT = 5 * 1000;
    private static final boolean USE_PROXY_DEFAULT = true;
    //    private static HttpClient client =  HttpClientBuilder.create().build();
    private static ConcurrentHashMap<String, Long> lastTimes = new ConcurrentHashMap<>();
    private static final long WAIT_DELTA = 3 * 1000;
    private static int retryCount = 1; // TODO: make it local
    private static Logger logger = MyLogger.getLogger(HttpDownloader.class.toString());

    private static void checkSleep(String hostname) {
        Long lastTime = lastTimes.get(hostname);
        if (lastTime != null) {
            long time = System.currentTimeMillis();
            long delta = time - lastTime;
            if (delta < WAIT_DELTA) {
                try {
                    logger.fine("Sleeping for " + (WAIT_DELTA - delta));
                    Thread.sleep(WAIT_DELTA - delta);
                    logger.fine("Sleep finished");
                } catch (InterruptedException e) {
                    logger.severe("Sleep interrupted");
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }
        lastTimes.put(hostname, System.currentTimeMillis());
    }

    private static void updateTime(String hostname) {
        lastTimes.put(hostname, System.currentTimeMillis());
    }

    public static String get(String url, boolean useProxy) throws IOException, DataRetrievingError {
        return get(url, null, null, useProxy);
    }

    public static String get(String url) throws IOException, DataRetrievingError {
        return get(url, null, null, USE_PROXY_DEFAULT);
    }

    public static String get(String url, List<NameValuePair> params, Map<String, String> headers) throws IOException, DataRetrievingError {
        return get(url, params, headers, USE_PROXY_DEFAULT);
    }

    public static String get(String url, List<NameValuePair> params, Map<String, String> headers, boolean useProxy) throws IOException, DataRetrievingError {
        URIBuilder uriBuilder = buildUriBuilder(url);

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
        if (useProxy)
            request.setConfig(buildRequestConfig());
        setHeaders(request, headers);

        checkSleep(uriBuilder.getHost());

        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpResponse response = client.execute(buildTarget(uriBuilder), request);
            updateTime(uriBuilder.getHost());
            retryCount = 0;
            return getResponse(response);
        } catch (HttpHostConnectException | ConnectTimeoutException e) {
            logger.warning("Exception happened. Retry #" + retryCount++);
            if (retryCount < 3)
                return get(url, params, headers, useProxy);
            logger.severe("Exception happened again after " + retryCount + "retries");
            throw e;
        }

    }

    public static String post(String url, List<NameValuePair> formData, Map<String, String> headers) throws IOException, DataRetrievingError {
        return post(url, formData, headers, USE_PROXY_DEFAULT);
    }

    public static String post(String url, List<NameValuePair> formData, Map<String, String> headers, boolean useProxy) throws IOException, DataRetrievingError {
        return post(url, new UrlEncodedFormEntity(formData, "UTF-8"), headers, useProxy);
    }

    public static String post(String url, String data, Map<String, String> headers) throws IOException, DataRetrievingError {
        return post(url, data, headers, USE_PROXY_DEFAULT);
    }

    public static String post(String url, String data, Map<String, String> headers, boolean useProxy) throws IOException, DataRetrievingError {
        return post(url, new StringEntity(data, "UTF-8"), headers, useProxy);
    }

    public static String post(String url, AbstractHttpEntity data, Map<String, String> headers, boolean useProxy) throws IOException, DataRetrievingError {
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
        if (useProxy)
            request.setConfig(buildRequestConfig());
        setHeaders(request, headers);

        checkSleep(uriBuilder.getHost());

        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpResponse response = client.execute(buildTarget(uriBuilder), request);
            updateTime(uriBuilder.getHost());
            retryCount = 0;
            return getResponse(response);
        } catch (HttpHostConnectException | ConnectTimeoutException e) {
            logger.warning("Exception happened. Retry #" + retryCount++);
            if (retryCount < 3)
                return post(url, data, headers, useProxy);
            logger.severe("Exception happened again after " + retryCount + "retries");
            throw e;
        }
    }

    private static String getPathAndQuery(URI uri) {
        String query = uri.getRawQuery() == null ? "" : uri.getRawQuery();
        return uri.getPath() + "?" + query;
    }

    private static URIBuilder buildUriBuilder(String url) throws DataRetrievingError {
        URIBuilder uriBuilder;
        try {
            uriBuilder = new URIBuilder(url);
        } catch (URISyntaxException e) {
            throw new DataRetrievingError(e);
        }
        uriBuilder.setCharset(Charset.forName("UTF-8"));
        return uriBuilder;
    }

    private static HttpHost buildTarget(URIBuilder uriBuilder) {
        return new HttpHost(uriBuilder.getHost(), uriBuilder.getPort(), uriBuilder.getScheme());
    }

    private static RequestConfig buildRequestConfig() {
        ProxyInfo proxyInfo = ProxyList.getNext();
        HttpHost proxy = new HttpHost(proxyInfo.getIp(), proxyInfo.getPort());
        return RequestConfig.custom().setProxy(proxy).setConnectTimeout(REQUEST_TIMEOUT).build();
    }

    private static String getResponse(HttpResponse response) throws IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
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

    public static void main(String[] args) throws IOException, DataRetrievingError {
        String res = HttpDownloader.get(Urls.KAD_HOME);
        System.out.println(res);
    }
}
