package caseloader.kad;

import caseloader.CaseInfo;
import caseloader.Urls;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import util.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KadLoader<CaseContainerType extends util.Appendable<CaseInfo>> {
    private Map<String, String> courts = new HashMap<>();
    private ExecutorService executor = null;
    private static final int ITEMS_COUNT_PER_REQUEST = 100;
    private static final int WAIT_TIMEOUT = 5 * 60;

    private ExecutorService getExecutor() {
        return Executors.newFixedThreadPool(1);
    }

    public KadLoader() {

    }

    public Set<String> retrieveCourts() {
        String kadHtml = HttpDownloader.get(Urls.KAD_HOME);
        Document d = Jsoup.parse(kadHtml);
        Elements courtsDOM = d.child(0).select("#Courts").first().children();
        courtsDOM.stream().filter(c -> c.hasText() && c.hasAttr("value"))
                          .forEach(c -> courts.put(c.text(), c.attr("value")));
        return courts.keySet(); // TODO: should be a callback call perhaps
    }

    public String courtCode(String court) {
        return courts.get(court);
    }

    public CaseContainerType retrieveData(KadSearchRequest request, CaseContainerType data) {
//        executor = getExecutor();

        request.setCount(ITEMS_COUNT_PER_REQUEST);
        KadResponse initial = retrieveKadResponse(request, 1);

        if (initial.isSuccess()) {
            int size = initial.getPagesCount() * initial.getPageSize();

            processKadResponse(initial, data);
            int iterationsCount = size / initial.getPageSize();
            for (int i = 2; i < iterationsCount; ++i) {
                KadResponse resp = retrieveKadResponse(request, i);
                if (resp.isSuccess()) {
                    processKadResponse(resp, data);
                }
            }
        }

//        executor.shutdown();
//        while (!executor.isTerminated()) {
//        }
//        try {
//            executor.awaitTermination(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        return data;
    }

    private void processKadResponse(KadResponse resp, CaseContainerType outData) {
        List<CaseInfo> items = resp.getItems();
        for (CaseInfo item : items) {
//            executor.execute(new KadWorker(item, outData));
            (new KadWorker<>(item, outData)).run();
        }
    }

    private KadResponse retrieveKadResponse(KadSearchRequest request, int page) {
        request.setPage(page);

        String json = request.toString();
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json, text/javascript, */*");
        JSONObject jsonObj = HttpDownloader.post(Urls.KAD_SEARCH, json, headers);
        return KadResponse.fromJSON(jsonObj);
    }


    public static void main(String[] args) {
//        KadLoader kl = new KadLoader();
//        KadData data = kl.retrieveData(new KadSearchRequest());
    }
}
