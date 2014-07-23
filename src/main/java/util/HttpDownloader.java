package util;

import exceptions.DataRetrievingError;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class HttpDownloader {
    public static final String USER_AGENT = "Test UserAgent 1.0";
    private static HttpClient client =  HttpClientBuilder.create().build();
    private static Map<String, Long> lastTimes = new HashMap<>();
    private static final long WAIT_DELTA = 5 * 1000;

    private static void checkTime(URL url) {
        try {
            Thread.sleep(WAIT_DELTA);
        } catch (InterruptedException e) {
            System.out.println("<------------Sleep interrupted");
            e.printStackTrace();
            System.exit(1);
        }
    }

//    public static JSONObject post(String targetUrl, String data, Map<String, String> headers) {
//        URL url;
//        HttpURLConnection connection = null;
//        try {
//
//            // Create connection
//            url = new URL(targetUrl);
//            checkTime(url);
//            connection = (HttpURLConnection) url.openConnection();
//            connection.setRequestMethod("POST");
//
//            if (headers != null) {
//                for (String k : headers.keySet()) {
//                    connection.setRequestProperty(k, headers.get(k));
//                }
//            }
//            connection.setRequestProperty("Content-Length", Integer.toString(data.getBytes().length));
//
//            connection.setUseCaches(false);
//            connection.setDoInput(true);
//            connection.setDoOutput(true);
//
//            // Send request
//            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
//            wr.writeBytes(data);
//            wr.flush();
//            wr.close();
//
//            // Get Response
//            InputStream is = connection.getInputStream();
//            String response = IOUtils.streamToString(is);
//            is.close();
//
//            lastTimes.put(url.getHost(), System.currentTimeMillis());
//            return new JSONObject(response);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        } finally {
//            if(connection != null) {
//                connection.disconnect();
//            }
//        }
//    }
//
//    public static String get(final String targetUrl, final Map<String, String> params, final Map<String, String> headers) {
//        HttpURLConnection connection = null;
//        try {
//            String query = buildQuery(params);
//
//            URL url = new URL(targetUrl + "?" + query);
//            checkTime(url);
//            connection = (HttpURLConnection) url.openConnection();
//            connection.setRequestProperty("Accept-Charset", "UTF-8");
//
//            if (headers != null) {
//                for (String k : headers.keySet()) {
//                    connection.setRequestProperty(k, headers.get(k));
//                }
//            }
//
//            InputStream is = connection.getInputStream();
//            String response = IOUtils.streamToString(is);
//            is.close();
//
//            lastTimes.put(url.getHost(), System.currentTimeMillis());
//            return response;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        } finally {
//            if(connection != null) {
//                connection.disconnect();
//            }
//        }
//    }
//
//    public static String get(final String targetUrl) {
//        return get(targetUrl, null, null);
//    }

    public static String get(String url) throws IOException, DataRetrievingError {
        return get(url, null, null);
    }

    public static String get(String url, List<NameValuePair> params, Map<String, String> headers) throws IOException, DataRetrievingError {
        URIBuilder uriBuilder = null;
        try {
             uriBuilder = new URIBuilder(url);
        } catch (URISyntaxException e) {
            throw new DataRetrievingError(e);
        }

        if (params != null) {
            uriBuilder.setParameters(params);
        }

        HttpGet request = null;
        try {
            request = new HttpGet(uriBuilder.build());
        } catch (URISyntaxException ignored) {
        }
        setHeaders(request, headers);

        HttpResponse response = client.execute(request);
        return getResponse(response);
    }

    public static String post(String url, List<NameValuePair> formData, Map<String, String> headers) throws IOException {
        HttpPost request = new HttpPost(url);
        request.setEntity(new UrlEncodedFormEntity(formData, "UTF-8"));
        setHeaders(request, headers);


        HttpResponse response = client.execute(request);
        return getResponse(response);
    }

    public static String post(String url, String data, Map<String, String> headers) throws IOException {
        HttpPost request = new HttpPost(url);
        request.setEntity(new StringEntity(data, "UTF-8"));
        request.setHeader("Content-Type", "application/json");
        setHeaders(request, headers);

        HttpResponse response = client.execute(request);
        return getResponse(response);
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
    }


    @Deprecated
    private static String buildQuery(Map<String, String> params) {
        if (params == null) {
            return "";
        }
        String query = "";
        for (String key : params.keySet()) {
            try {
                query += key + "=" + URLEncoder.encode(params.get(key), "UTF-8") + "&";
            } catch (UnsupportedEncodingException ignored) {
            }
        }
        return query.substring(0, query.length() - 1);
    }
}
