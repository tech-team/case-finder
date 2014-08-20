package proxy;

import util.net.MalformedUrlException;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import util.net.HttpDownloader;
import util.MyLogger;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ProxyUpdater {
    class Urls {
        public static final String HIDE_MY_ASS = "http://proxylist.hidemyass.com/search-1311573#listable";
        public static final String COOL_PROXY = "http://www.cool-proxy.net/proxies/http_proxy_list/sort:download_speed_average/direction:desc/country_code:/port:/anonymous:1";
        public static final String GOOGLE_PROXY = "http://www.google-proxy.net/";
    }

    private static final int UPDATE_PERIOD = 30 * 60 * 1000; // 30 minutes
    private static final int GOOGLE_UPDATE_PERIOD = 12 * 60 * 1000; // 10 minutes

    private boolean doWork = false;
    private final Logger logger = MyLogger.getLogger(this.getClass().toString());

    public void run(ProxyList proxyList) {
        if (!doWork) {
            Thread ordinalThread = new Thread(() -> {
                logger.info("ProxyUpdater for ordinal proxies started");
                doWork = true;
                while (doWork) {
                    List<ProxyInfo> newList = null;
                    try {
                        newList = retrieveProxyListCoolProxy();
                    } catch (InterruptedException e) {
                        logger.info("ProxyUpdater for ordinal proxies has been interrupted");
                        return;
                    } catch (MalformedUrlException e) {
                        logger.log(Level.WARNING, "Exception happened", e);
                        return;
                    }
                    proxyList.loadNewList(newList);

                    try {
                        Thread.sleep(UPDATE_PERIOD);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                logger.info("ProxyUpdater for ordinal proxies finished");
            });

            Thread googleThread = new Thread(() -> {
                logger.info("ProxyUpdater for google proxies started");
                doWork = true;
                while (doWork) {
                    List<ProxyInfo> newGoogleList = null;
                    try {
                        newGoogleList = retrieveProxyListGoogleProxies();
                    } catch (InterruptedException e) {
                        logger.info("ProxyUpdater for google proxies has been interrupted");
                        return;
                    } catch (MalformedUrlException e) {
                        logger.log(Level.WARNING, "Exception happened", e);
                        return;
                    }
                    proxyList.loadNewGoogleList(newGoogleList);

                    try {
                        Thread.sleep(GOOGLE_UPDATE_PERIOD);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                logger.info("ProxyUpdater for google proxies finished");
            });
            ordinalThread.setDaemon(true);
            googleThread.setDaemon(true);

            ordinalThread.start();
            googleThread.start();
        } else {
            throw new RuntimeException("ProxyUpdater is already running");
        }
    }

    private List<ProxyInfo> retrieveProxyListGoogleProxies() throws InterruptedException, MalformedUrlException {
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

    private List<ProxyInfo> retrieveProxyListCoolProxy() throws MalformedUrlException, InterruptedException {
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

    @SuppressWarnings("UnusedDeclaration")
    public List<ProxyInfo> retrieveProxyListHideMyAss() throws InterruptedException {
        List<ProxyInfo> proxies = new ArrayList<>();
        final AtomicBoolean loaded = new AtomicBoolean(false);

        Platform.runLater(() -> {
            final Stage stage = new Stage();

            final WebView webView = new WebView();
            final WebEngine webEngine = webView.getEngine();
            webView.setVisible(false);
            stage.setScene(new Scene(webView, 1, 1));
            stage.setTitle("Loading proxy list...");



            webEngine.setUserAgent("Opera/9.80 (Windows NT 6.0) Presto/2.12.388 Version/12.14");
            webEngine.getLoadWorker().stateProperty().addListener(
                    (ov, oldState, newState) -> {
                        if (newState == Worker.State.SUCCEEDED) {
                            String content = webEngine.executeScript("document.getElementById('listable').innerText").toString();

//                        List<ProxyInfo> proxyList = new ArrayList<>();
                            Pattern regex = Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+)\\s+(\\d+)");
                            Matcher regexMatcher = regex.matcher(content);
                            while (regexMatcher.find()) {
                                proxies.add(
                                        new ProxyInfo(
                                                regexMatcher.group(1),
                                                Integer.parseInt(regexMatcher.group(2))));
                            }

//                        onLoadedCallback.accept(proxyList);
                            stage.close();
                            loaded.set(true);
                        }
                    });

            webEngine.load(Urls.HIDE_MY_ASS);

            stage.show();
        });

        while (!loaded.get()) {
            Thread.sleep(100);
        }
        return proxies;
    }
}
