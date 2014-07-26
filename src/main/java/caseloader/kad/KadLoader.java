package caseloader.kad;

import caseloader.CaseInfo;
import caseloader.CaseSearchRequest;
import exceptions.DataRetrievingError;
import org.json.JSONException;
import org.json.JSONObject;
import util.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class KadLoader<CaseContainerType extends util.Appendable<CaseInfo>> {
    private ExecutorService executor = null;
    private static final int ITEMS_COUNT_PER_REQUEST = 100;
    private static final int TOTAL_COUNT = 1000;
    private static final int WAIT_TIMEOUT = 5 * 60;
    private int retryCount = 0;

    private ExecutorService getExecutor() {
        return Executors.newFixedThreadPool(2);
    }

    public KadLoader() {

    }

    public CaseContainerType retrieveData(CaseSearchRequest request, CaseContainerType data) throws IOException, DataRetrievingError {
        executor = getExecutor();

        int minCost = request.getMinCost();
        int searchLimit = request.getSearchLimit();

        int itemsCountToLoad = searchLimit != 0 && searchLimit < ITEMS_COUNT_PER_REQUEST ? searchLimit : ITEMS_COUNT_PER_REQUEST;

        request.setCount(itemsCountToLoad);
        KadResponse initial = retrieveKadResponse(request, 1);

        if (initial.isSuccess()) {
            processKadResponse(initial, minCost, data);
            data.setTotalCount(TOTAL_COUNT);

            int iterationsCount = TOTAL_COUNT / initial.getPageSize();
            for (int i = 2; i <= iterationsCount; ++i) {
                KadResponse resp = retrieveKadResponse(request, i);
                if (resp.isSuccess()) {
                    processKadResponse(resp, minCost, data);
                }
            }
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
            executor.execute(new KadWorker<>(item, minCost, outData));
//            (new KadWorker<>(item, minCost, outData)).run();
        }
    }

    private KadResponse retrieveKadResponse(CaseSearchRequest request, int page) throws IOException, DataRetrievingError {
        System.out.println("=== Getting page #" + page);
        request.setPage(page);

        String json = request.toString();
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json, text/javascript, */*");
        String resp = HttpDownloader.post(Urls.KAD_SEARCH, json, headers);
        try {
            JSONObject jsonObj = new JSONObject(resp);
            retryCount = 0;
            return KadResponse.fromJSON(jsonObj);
        } catch (JSONException e) {
            System.out.println("Retrying #" + retryCount);
            if (retryCount < 3) {
                retryCount++;
                return retrieveKadResponse(request, page);
            }
            throw e;
        }
    }


    public static void main(String[] args) {
//        KadLoader kl = new KadLoader();
//        KadData data = kl.retrieveData(new CaseSearchRequest());
    }
}
