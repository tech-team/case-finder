package util;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public abstract class HttpDownloader {
    public static final String USER_AGENT = "Test UserAgent 1.0";
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

    public static JSONObject post(String targetUrl, String data, Map<String, String> headers) {
        URL url;
        HttpURLConnection connection = null;
        try {

            // Create connection
            url = new URL(targetUrl);
            checkTime(url);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            if (headers != null) {
                for (String k : headers.keySet()) {
                    connection.setRequestProperty(k, headers.get(k));
                }
            }
            connection.setRequestProperty("Content-Length", Integer.toString(data.getBytes().length));

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // Send request
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(data);
            wr.flush();
            wr.close();

            // Get Response
            InputStream is = connection.getInputStream();
            String response = IOUtils.streamToString(is);
            is.close();

            lastTimes.put(url.getHost(), System.currentTimeMillis());
            return new JSONObject(response);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
    }

    public static String get(final String targetUrl, final Map<String, String> params, final Map<String, String> headers) {
        HttpURLConnection connection = null;
        try {
            String query = buildQuery(params);

            URL url = new URL(targetUrl + "?" + query);
            checkTime(url);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Accept-Charset", "UTF-8");

            if (headers != null) {
                for (String k : headers.keySet()) {
                    connection.setRequestProperty(k, headers.get(k));
                }
            }

            InputStream is = connection.getInputStream();
            String response = IOUtils.streamToString(is);
            is.close();

            lastTimes.put(url.getHost(), System.currentTimeMillis());
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
    }

    public static String get(final String targetUrl) {
        return get(targetUrl, null, null);
    }

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
