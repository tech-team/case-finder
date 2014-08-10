package caseloader.kad;

import caseloader.CaseLoaderEvents;
import caseloader.errors.CaseLoaderError;
import caseloader.errors.ErrorReason;
import eventsystem.DataEvent;
import util.DataRetrievingError;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import util.HttpDownloader;
import util.MyLogger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class CourtsInfo {
    private static Map<String, String> courts = new LinkedHashMap<>();
    public static DataEvent<Set<String>> courtsLoadedEvent = new DataEvent<>();

    private static Logger logger = MyLogger.getLogger(CourtsInfo.class.toString());

    private static Set<String> retrieveCourts() throws DataRetrievingError, InterruptedException {
        if (courts.size() == 0) {
            int maxRetries = 3;
            for (int retry = 1; retry <= maxRetries + 1; ++retry) {
                try {
                    String kadHtml = HttpDownloader.i().get(Urls.KAD_HOME);
                    if (kadHtml == null) {
                        throw new NullPointerException();
                    }
                    Document d = Jsoup.parse(kadHtml);
                    Elements courtsDOM = d.body().select("#Courts").first().children();
                    courtsDOM.stream().filter(c -> c.hasText() && c.hasAttr("value"))
                                      .forEach(c -> courts.put(c.text(), c.attr("value")));
                    return courts.keySet();
                } catch (NullPointerException e) {
                    if (retry <= maxRetries)
                        logger.warning("Error retrieving courts. Retry #" + retry);
                    else
                        break;
                }
            }
            logger.severe("Couldn't retrieve courts");
            CaseLoaderEvents.instance().onError.fire(new CaseLoaderError(ErrorReason.COURTS_RETRIEVAL_ERROR));
        }
        return courts.keySet();
    }

    public static void retrieveCourtsAsync() {
        if (courts.size() == 0) {
            Thread th = new Thread(() -> {
                logger.info("Retrieving courts list");
                Set<String> courts = null;
                try {
                    courts = retrieveCourts();
                } catch (InterruptedException e) {
                    logger.info("Retrieving courts has been interrupted");
                    return;
                } catch (DataRetrievingError e) {
                    logger.log(Level.SEVERE, "Exception happened", e);
                    CaseLoaderEvents.instance().onError.fire(new CaseLoaderError(ErrorReason.UNEXPECTED_ERROR));
                    return;
                }

                courtsLoadedEvent.fire(courts);
                logger.info("Finished retrieving courts list");
            });
            th.setDaemon(true);
            th.start();
        } else {
            try {
                courtsLoadedEvent.fire(retrieveCourts());
            } catch (InterruptedException e) {
                logger.info("Retrieving courts has been interrupted");
            } catch (DataRetrievingError e) {
                logger.log(Level.WARNING, "Exception happened", e);
            }
        }
    }

    public static String getCourtCode(String court) {
        return courts.get(court);
    }

    public static String getCourtName(String courtId) {
        Set<String> courtsSet = courts.keySet();
        for (String courtName : courtsSet) {
            if (courts.get(courtName).equals(courtId)) {
                return courtName;
            }
        }
        return null;
    }

    public static boolean courtsLoaded() {
        return courts.size() != 0;
    }
}
