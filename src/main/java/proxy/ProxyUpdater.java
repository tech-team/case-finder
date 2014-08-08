package proxy;

import exceptions.DataRetrievingError;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import util.HttpDownloader;
import util.MyLogger;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ProxyUpdater {
    class Urls {
        public static final String HIDE_MY_ASS = "http://proxylist.hidemyass.com/";
        public static final String COOL_PROXY = "http://www.cool-proxy.net/proxies/http_proxy_list/sort:download_speed_average/direction:desc/country_code:/port:/anonymous:1";
        public static final String COOL_PROXY2 = "http://www.cool-proxy.net/proxies/http_proxy_list/sort:download_speed_average/direction:desc/country_code:/port:/anonymous:0";
        public static final String GOOGLE_PROXY = "http://www.google-proxy.net/";

    }

    private static final int UPDATE_PERIOD = 30 * 60 * 1000; // 30 minutes
    private static final int SLEEP_TIME = 100;

    private boolean doWork = false;
    private Logger logger = MyLogger.getLogger(this.getClass().toString());

    public void run(ProxyList proxyList) {
        if (!doWork) {
            Thread th = new Thread(() -> {
                logger.info("ProxyUpdater started");
                doWork = true;
                while (doWork) {
                    List<ProxyInfo> newList = null;
                    List<ProxyInfo> newGoogleList = null;
                    try {
                        newList = retrieveProxyListCoolProxy();
                        newGoogleList = retrieveProxyListGoogleProxies();
                    } catch (IOException | DataRetrievingError e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        logger.info("ProxyUpdater has been interrupted");
                        return;
                    }
                    proxyList.loadNewList(newList);
                    proxyList.loadNewGoogleList(newGoogleList);

                    int sleepCount = 0;
                    while (doWork && sleepCount != (UPDATE_PERIOD / SLEEP_TIME)) {
                        try {
                            Thread.sleep(SLEEP_TIME);
                            sleepCount += 1;
                        } catch (InterruptedException e) {
                            logger.info("ProxyUpdater has been interrupted");
                            return;
                        }
                    }
                }
                logger.info("ProxyUpdater finished");
            });
            th.setDaemon(true);
            th.start();
        } else {
            throw new RuntimeException("ProxyUpdater is already running");
        }
    }

    public void stopWork() {
        doWork = false;
    }

    private List<ProxyInfo> retrieveProxyListGoogleProxies() throws InterruptedException, DataRetrievingError, IOException {
        List<ProxyInfo> proxies = new ArrayList<>();

        String raw = HttpDownloader.i().get(Urls.GOOGLE_PROXY, false);
        Elements proxyTable = Jsoup.parse(raw)
                                   .body()
                                   .getElementById("proxylisttable")
                                   .getElementsByTag("tbody").first()
                                   .getElementsByTag("tr");

        int max = 20;
        for (int i = 0; proxies.size() < max && i < proxyTable.size(); ++i) {
            Element tr = proxyTable.get(i);
            Elements tds = tr.getElementsByTag("td");

            String ip = tds.first().text();
            int port = Integer.parseInt(tds.get(1).text());
            String country = tds.get(3).text();
            boolean isHttps = tds.get(6).text().equals("yes");
            if (isHttps)
                proxies.add(new ProxyInfo(ip, port, country));
        }

        return proxies;
    }

    private List<ProxyInfo> retrieveProxyListCoolProxy() throws IOException, DataRetrievingError, InterruptedException {
        List<ProxyInfo> proxies = new ArrayList<>();

        String raw = HttpDownloader.i().get(Urls.COOL_PROXY, false);
        Element page = Jsoup.parse(raw).body();
        Element table = page.select("#main table").first();
        Elements trs = table.getElementsByTag("tr");
        for (int i = 1; i < trs.size(); ++i) {
            try {
                Element tr = trs.get(i);

                Pattern ipBase64Pattern = Pattern.compile(".*\"(.*)\".*");
                String ipBase64String = tr.child(0).getElementsByTag("script").first().html();
                Matcher m = ipBase64Pattern.matcher(ipBase64String);
                String ip = "";
                while (m.find()) {
                    ip = new String(Base64.getDecoder().decode(m.group(1)));
                }

                int port = Integer.parseInt(tr.child(1).text());
                String country = tr.child(3).text();
                int rating = Integer.parseInt(tr.child(4).getElementsByTag("img").attr("alt").substring(0, 1));

                int working = 0;
                String workingStyleString = tr.child(6).select(".graph .bar").first().attr("style");
                m = Pattern.compile(".*width:([\\d\\.]+)%.*").matcher(workingStyleString);
                while (m.find()) {
                    working = (int) Double.parseDouble(m.group(1));
                }

                int responseTime = 0;
                String responseTimeStyleString = tr.child(7).select(".graph .bar").first().attr("style");
                m = Pattern.compile(".*width:([\\d\\.]+)%.*").matcher(responseTimeStyleString);
                while (m.find()) {
                    responseTime = (int) Double.parseDouble(m.group(1));
                }

                int downloadSpeed = 0;
                String downloadSpeedStyleString = tr.child(8).select(".graph .bar").first().attr("style");
                m = Pattern.compile(".*width:([\\d\\.]+)%.*").matcher(downloadSpeedStyleString);
                while (m.find()) {
                    downloadSpeed = (int) Double.parseDouble(m.group(1));
                }

                proxies.add(new ProxyInfo(ip, port, country, rating, working, responseTime, downloadSpeed));

            } catch (Exception e) {
//                System.out.println("<---Wrong line");
            }
        }

        final int RATING_THRESHOLD = 3;
        for (int i = proxies.size() - 1; i >= 0; --i) {
            if (proxies.get(i).getRating() < RATING_THRESHOLD) {
                proxies.remove(i);
            }
        }
        return proxies;
    }
}
