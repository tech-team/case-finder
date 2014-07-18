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
        if (courts.size() == 0) {
            String kadHtml = HttpDownloader.get(Urls.KAD_HOME);
            Document d = Jsoup.parse(kadHtml);
            Elements courtsDOM = d.child(0).select("#Courts").first().children();
            courtsDOM.stream().filter(c -> c.hasText() && c.hasAttr("value"))
                              .forEach(c -> courts.put(c.text(), c.attr("value")));
        }
        return courts.keySet();
    }

    public String courtCode(String court) {
        return courts.get(court);
    }

    public CaseContainerType retrieveData(KadSearchRequest request, int minCost, int searchLimit, CaseContainerType data) {
//        executor = getExecutor();

        // TODO: uncomment this after all debugging
//        if (minCost <= 0) {
//            throw new RuntimeException("Min cost is wrong. Should be greater than 0");
//        }
//        if (!(searchLimit > 0 && searchLimit <= 1000)) {
//            throw new RuntimeException("Search limit is wrong. Should be in (0; 1000]");
//        }

        int itemsCountToLoad = searchLimit != 0 && searchLimit < ITEMS_COUNT_PER_REQUEST ? searchLimit : ITEMS_COUNT_PER_REQUEST;

        request.setCount(itemsCountToLoad);
        KadResponse initial = retrieveKadResponse(request, 1);

        if (initial.isSuccess()) {
            processKadResponse(initial, minCost, data);

            int size = initial.getPagesCount() * initial.getPageSize();

            int iterationsCount = size / initial.getPageSize();
            for (int i = 2; i <= iterationsCount; ++i) {
                KadResponse resp = retrieveKadResponse(request, i);
                if (resp.isSuccess()) {
                    processKadResponse(resp, minCost, data);
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

    private void processKadResponse(KadResponse resp, int minCost, CaseContainerType outData) {
        List<CaseInfo> items = resp.getItems();
        for (CaseInfo item : items) {
//            executor.execute(new KadWorker(item, outData));
            (new KadWorker<>(item, minCost, outData)).run();
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
