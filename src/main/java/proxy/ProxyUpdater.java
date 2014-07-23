package proxy;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import util.HttpDownloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProxyUpdater implements Runnable {
    class Urls {
        public static final String HOME_PAGE = "http://proxylist.hidemyass.com/";
    }

    private static final int UPDATE_PERIOD = 30 * 60 * 1000; // 30 minutes
    private boolean doWork = false;

    @Override
    public void run() {
        doWork = true;
        while (doWork) {
            List<ProxyPair> newList = null;
            try {
                newList = retrieveProxyList();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ProxyList.loadNewList(newList);

            try {
                Thread.sleep(UPDATE_PERIOD);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public void stop() {
        doWork = false;
    }


    private List<ProxyPair> retrieveProxyList() throws IOException {
        JSONObject resp = new JSONObject(retrieveRawData());
        String proxyTableStr = resp.getString("table");
        Document proxyTable = Jsoup.parseBodyFragment(proxyTableStr);

        List<ProxyPair> proxies = new ArrayList<>();
        for (Element tr : proxyTable.getElementsByTag("tr")) {
            String ip = tr.child(1).text();
            int port = Integer.parseInt(tr.child(2).text());
            String country = tr.child(3).text();
            proxies.add(new ProxyPair(ip, port, country));
        }

        return proxies;
    }

    private String retrieveRawData() throws IOException {
        List<NameValuePair> formData = new ArrayList<>();
        Map<String, String> headers = new HashMap<>();

        String[] countries = {
                "China",
                "Venezuela",
                "United States",
                "Indonesia",
                "Russian Federation",
                "Brazil",
                "China",
                "Venezuela",
                "Thailand",
                "India",
                "Romania",
                "United Kingdom",
                "Germany",
                "Colombia",
                "Hong Kong",
                "Netherlands",
                "Viet Nam",
                "Australia",
                "Bangladesh",
                "Argentina",
                "Sri Lanka",
                "Turkey",
                "United Arab Emirates",
                "Korea, Republic of",
                "Japan",
                "Serbia",
                "Iran",
                "Poland",
                "Saudi Arabia",
                "Bosnia and Herzegovina",
                "Netherlands Antilles",
                "Albania",
                "Zaire",
                "Sweden",
                "Maldives",
                "Taiwan",
                "Nigeria",
                "Paraguay",
                "Panama",
                "France",
                "Bulgaria",
                "Ukraine",
                "Hungary",
                "Chile",
                "Malaysia",
                "Italy",
                "Mexico",
                "Austria",
                "Ecuador",
                "Moldova, Republic of",
                "Taiwan",
                "Philippines",
                "Lithuania"
        };

        formData.add(new BasicNameValuePair("ac", "on"));
        for (String country : countries) {
            formData.add(new BasicNameValuePair("c[]", country));
        }
        formData.add(new BasicNameValuePair("allPorts", "1"));
        formData.add(new BasicNameValuePair("p", ""));
        formData.add(new BasicNameValuePair("pr[]", "0"));
        formData.add(new BasicNameValuePair("a[]", "0"));
        formData.add(new BasicNameValuePair("a[]", "1"));
        formData.add(new BasicNameValuePair("a[]", "2"));
        formData.add(new BasicNameValuePair("a[]", "3"));
        formData.add(new BasicNameValuePair("a[]", "4"));
        formData.add(new BasicNameValuePair("pl", "on"));
        formData.add(new BasicNameValuePair("sp[]", "2"));
        formData.add(new BasicNameValuePair("sp[]", "3"));
        formData.add(new BasicNameValuePair("ct[]", "2"));
        formData.add(new BasicNameValuePair("ct[]", "3"));
        formData.add(new BasicNameValuePair("s", "1"));
        formData.add(new BasicNameValuePair("o", "0"));
        formData.add(new BasicNameValuePair("pp", "3"));
        formData.add(new BasicNameValuePair("sortBy", "response_time"));

        headers.put("X-Requested-With", "XMLHttpRequest");
        return HttpDownloader.post(Urls.HOME_PAGE, formData, headers);
    }

    public static void main(String[] args) {
        ProxyUpdater updater = new ProxyUpdater();
        Thread th = new Thread(updater);
        th.start();
    }
}
