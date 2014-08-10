package caseloader.kad;

import caseloader.CaseInfo;
import caseloader.CaseLoaderEvents;
import caseloader.CaseSearchRequest;
import caseloader.credentials.CredentialsLoader;
import util.DataRetrievingError;
import org.json.JSONException;
import org.json.JSONObject;
import util.HttpDownloader;
import util.MyLogger;
import util.ThreadPool;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class KadLoader<CaseContainerType extends util.Appendable<CaseInfo>> {
    private static final int ITEMS_COUNT_PER_REQUEST = 100;
    public static final int TOTAL_MAX_COUNT = 1000;
    private int retryCount = 1;
    private AtomicInteger casesProgressCount = new AtomicInteger(0);
    private ThreadPool pool = new ThreadPool(4);
    private KadWorkerFactory<CaseContainerType> kadWorkerFactory = null;
    private List<CaseSearchRequest> brokenPages = new LinkedList<>();
    private final CredentialsLoader credentialsLoader = new CredentialsLoader();

    private Logger logger = MyLogger.getLogger(this.getClass().toString());

    public KadLoader() {
    }

    private int countToLoadPerRequest(int limit) {
        return limit != 0
                && limit < ITEMS_COUNT_PER_REQUEST ? limit :
                ITEMS_COUNT_PER_REQUEST;
    }

    public CaseContainerType retrieveData(CaseSearchRequest request, CaseContainerType data) throws DataRetrievingError, InterruptedException {
        long minCost = request.getMinCost();
        int searchLimit = request.getSearchLimit();
        int totalCountToLoad = searchLimit != 0 && searchLimit < TOTAL_MAX_COUNT ? searchLimit :
                                                                                   TOTAL_MAX_COUNT;

        kadWorkerFactory = new KadWorkerFactory<>(totalCountToLoad, minCost, data, credentialsLoader);

        int totalCasesCount = 0;

        int countPerRequest = countToLoadPerRequest(searchLimit);
        searchLimit -= countPerRequest;

        request.setCount(countPerRequest);
        request.setPage(1);
        KadResponse initial = retrieveKadResponse(request);
        if (initial == null)
            return null;

        if (initial.isSuccess()) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            processKadResponse(initial);
            totalCasesCount += initial.getItems().size();

            int iterationsCount = (int) Math.ceil(((double) totalCountToLoad) / initial.getPageSize());
            for (int i = 2; i <= iterationsCount; ++i) {
                countPerRequest = countToLoadPerRequest(searchLimit);
                request.setCount(countPerRequest);

                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException();
                }

                request.setPage(i);
                KadResponse resp = retrieveKadResponse(request);
                if (resp == null) {
                    Thread.sleep(10);
                    logger.severe("Page #" + i + " was not loaded. Skipping.");
                    continue;
                }

                if (resp.isSuccess()) {
                    processKadResponse(resp);
                    totalCasesCount += resp.getItems().size();
                    searchLimit -= countPerRequest;
                } else {
                    logger.warning("Couldn't load page #" + i + ". Retrying.");
                    i -= 1;
                }
            }

            if (brokenPages.size() != 0) {
                logger.info("Retrying broken pages");
                for (int i = 0; i < brokenPages.size(); i++) {
                    CaseSearchRequest r = brokenPages.get(i);
                    KadResponse resp = retrieveKadResponse(r);
                    if (resp == null) {
                        Thread.sleep(10);
                        logger.severe("Page #" + i + " was not loaded. Skipping.");
                        continue;
                    }

                    if (resp.isSuccess()) {
                        processKadResponse(resp);
                        totalCasesCount += resp.getItems().size();
                        searchLimit -= countPerRequest;
                    } else {
                        logger.warning("Couldn't load page #" + i + ". Retrying.");
                        i -= 1;
                    }
                }
                logger.info("Finished broken pages");
            }
            CaseLoaderEvents.instance().totalCasesCountObtained.fire(totalCasesCount);
        }

        pool.waitForFinish();

        return data;
    }

    private void processKadResponse(KadResponse resp) throws InterruptedException {
        List<CaseInfo> items = resp.getItems();
        for (CaseInfo item : items) {
            casesProgressCount.incrementAndGet();
            pool.execute(kadWorkerFactory.buildWorker(casesProgressCount.get(), item));
        }
    }

    private KadResponse retrieveKadResponse(CaseSearchRequest request) throws DataRetrievingError, InterruptedException {
        int page = request.getPage();
        logger.info("Getting page #" + page);

        String json = request.toString();
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json, text/javascript, */*");
        String resp = HttpDownloader.i().post(Urls.KAD_SEARCH, json, headers);
        try {
            JSONObject jsonObj = new JSONObject(resp);
            retryCount = 1;
            logger.info("Got page #" + page);
            return KadResponse.fromJSON(jsonObj);
        } catch (JSONException | NullPointerException e) {
            logger.warning("Retrying #" + retryCount);
            if (retryCount <= 3) {
                retryCount++;
                Thread.sleep(50);
                return retrieveKadResponse(request);
            }
            brokenPages.add(request.copy());
            return null;
        }
    }

    public void stopExecution() {
        pool.stopExecution();
    }


    public static void main(String[] args) {
//        KadLoader kl = new KadLoader();
//        KadData data = kl.retrieveData(new CaseSearchRequest());
    }
}
