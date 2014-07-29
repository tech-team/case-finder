package caseloader.kad;

import caseloader.CaseInfo;
import caseloader.CaseSearchRequest;
import caseloader.ThreadPool;
import caseloader.credentials.CredentialsLoader;
import eventsystem.DataEvent;
import eventsystem.Event;
import exceptions.DataRetrievingError;
import org.json.JSONException;
import org.json.JSONObject;
import util.HttpDownloader;
import util.MyLogger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class KadLoader<CaseContainerType extends util.Appendable<CaseInfo>> {
    private static final int ITEMS_COUNT_PER_REQUEST = 100;
    public static final int TOTAL_MAX_COUNT = 1000;
    private int retryCount = 1;
    private int casesProgressCount = 0;
    private ThreadPool pool = new ThreadPool();
    private final CredentialsLoader credentialsLoader = new CredentialsLoader();

    private final DataEvent<Integer> totalCasesCountObtained;
    private final Event caseProcessed;

    private Logger logger = MyLogger.getLogger(this.getClass().toString());

    public KadLoader(DataEvent<Integer> totalCasesCountObtained, Event caseProcessed) {
        this.totalCasesCountObtained = totalCasesCountObtained;
        this.caseProcessed = caseProcessed;
    }

    public CaseContainerType retrieveData(CaseSearchRequest request, CaseContainerType data) throws IOException, DataRetrievingError {
        int minCost = request.getMinCost();
        int searchLimit = request.getSearchLimit();

        int itemsCountToLoad = searchLimit != 0 && searchLimit < TOTAL_MAX_COUNT ? searchLimit :
                                                                                   TOTAL_MAX_COUNT;

        try {
            int totalCasesCount = 0;
            request.setCount(ITEMS_COUNT_PER_REQUEST);
            KadResponse initial = retrieveKadResponse(request, 1);

            if (initial.isSuccess()) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                processKadResponse(initial, minCost, data);
                totalCasesCount += initial.getItems().size();
                request.setCount(initial.getPageSize());

                int iterationsCount = itemsCountToLoad / initial.getPageSize();
                for (int i = 2; i <= iterationsCount; ++i) {
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }

                    KadResponse resp = retrieveKadResponse(request, i);
                    if (resp.isSuccess()) {
                        processKadResponse(resp, minCost, data);
                        totalCasesCount += resp.getItems().size();
                    } else {
                        logger.severe("Couldn't load page #" + i);
                    }
                }
                totalCasesCountObtained.fire(totalCasesCount);
            }

            pool.waitForFinish();
        } catch (InterruptedException e) {
            System.out.println("We are stopped");
        }

        return data;
    }

    private void processKadResponse(KadResponse resp, int minCost, CaseContainerType outData) throws InterruptedException {
        List<CaseInfo> items = resp.getItems();
        for (CaseInfo item : items) {
            pool.execute(new KadWorker<>(++casesProgressCount, item, minCost, outData, credentialsLoader, caseProcessed));
        }
    }

    private KadResponse retrieveKadResponse(CaseSearchRequest request, int page) throws IOException, DataRetrievingError, InterruptedException {
        logger.info("Getting page #" + page);
        request.setPage(page);

        String json = request.toString();
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json, text/javascript, */*");
        String resp = HttpDownloader.post(Urls.KAD_SEARCH, json, headers);
        try {
            JSONObject jsonObj = new JSONObject(resp);
            retryCount = 1;
            logger.info("Got page #" + page);
            return KadResponse.fromJSON(jsonObj);
        } catch (JSONException e) {
            logger.warning("Retrying #" + retryCount);
            if (retryCount < 3) {
                retryCount++;
                return retrieveKadResponse(request, page);
            }
            throw e;
        }
    }

    public void stopExecution() {
        credentialsLoader.stopExecution();
        pool.stopExecution();
    }


    public static void main(String[] args) {
//        KadLoader kl = new KadLoader();
//        KadData data = kl.retrieveData(new CaseSearchRequest());
    }
}
