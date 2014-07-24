package caseloader.kad;

import eventsystem.DataEvent;
import exceptions.DataRetrievingError;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import util.HttpDownloader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class CourtsInfo {
    private static Map<String, String> courts = new HashMap<>();

    public static DataEvent<Set<String>> courtsLoadedEvent = new DataEvent<>();

    private static Set<String> retrieveCourts() throws IOException, DataRetrievingError {
        if (courts.size() == 0) {
            String kadHtml = HttpDownloader.get(Urls.KAD_HOME);
            Document d = Jsoup.parse(kadHtml);
            Elements courtsDOM = d.child(0).select("#Courts").first().children();
            courtsDOM.stream().filter(c -> c.hasText() && c.hasAttr("value"))
                     .forEach(c -> courts.put(c.text(), c.attr("value")));
        }
        return courts.keySet();
    }

    public static Thread retrieveCourtsAsync() {
        return new Thread(() -> {
            System.out.println("--- Retrieving courts list ---");
            Set<String> courts = null;
            try {
                courts = retrieveCourts();
            } catch (IOException | DataRetrievingError e) {
                throw new RuntimeException(e);
            }
            courtsLoadedEvent.fire(courts);
            System.out.println("--- Finished retrieving courts list ---");
        });
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
