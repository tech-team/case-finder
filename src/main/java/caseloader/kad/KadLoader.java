package caseloader.kad;

import caseloader.CaseInfo;
import caseloader.CaseLoaderEvents;
import caseloader.CaseSearchRequest;
import caseloader.credentials.CredentialsLoader;
import util.net.MalformedUrlException;
import org.json.JSONException;
import org.json.JSONObject;
import util.net.HttpDownloader;
import util.MyLogger;
import util.ThreadPool;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class KadLoader<CaseContainerType extends util.Appendable<CaseInfo>> {
    private static final int ITEMS_COUNT_PER_REQUEST = 25;
    private static final int TOTAL_MAX_COUNT = 1000;
    private int retryCount = 1;
    private final AtomicInteger casesProgressCount = new AtomicInteger(0);
    private final ThreadPool pool = new ThreadPool(1);
    private KadWorkerFactory<CaseContainerType> kadWorkerFactory = null;
    private final List<CaseSearchRequest> brokenPages = new LinkedList<>();
    private final CredentialsLoader credentialsLoader = new CredentialsLoader();

    private final Logger logger = MyLogger.getLogger(this.getClass().toString());

    public KadLoader() {
    }

    private int countToLoadPerRequest(int limit) {
        return limit != 0
                && limit < ITEMS_COUNT_PER_REQUEST ? limit :
                ITEMS_COUNT_PER_REQUEST;
    }

    public CaseContainerType retrieveData(CaseSearchRequest request, CaseContainerType data) throws MalformedUrlException, InterruptedException {
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
                    logger.severe("Page_" + i + " was not loaded. Skipping.");
                    continue;
                }

                if (resp.isSuccess()) {
                    processKadResponse(resp);
                    totalCasesCount += resp.getItems().size();
                    searchLimit -= countPerRequest;
                } else {
                    logger.warning("Couldn't load page_" + i + ". Retrying.");
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
                        logger.severe("Page_" + i + " was not loaded. Skipping.");
                        continue;
                    }

                    if (resp.isSuccess()) {
                        processKadResponse(resp);
                        totalCasesCount += resp.getItems().size();
                        searchLimit -= countPerRequest;
                    } else {
                        logger.warning("Couldn't load page_" + i + ". Retrying.");
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
            Runnable w = kadWorkerFactory.buildWorker(casesProgressCount.get(), item);
            w.run();
//            pool.execute(kadWorkerFactory.buildWorker(casesProgressCount.get(), item));
        }
    }

    private KadResponse retrieveKadResponse(CaseSearchRequest request) throws MalformedUrlException, InterruptedException {
        int page = request.getPage();
        logger.info("Getting page_" + page);

        String json = request.toString();
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("x-date-format", "iso");
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json, text/javascript, */*");
        String resp = HttpDownloader.i().post(Urls.KAD_SEARCH, json, headers, false);
        try {
            JSONObject jsonObj = new JSONObject(resp);
            retryCount = 1;
            logger.info("Got page #" + page);
            return KadResponse.fromJSON(jsonObj);
        } catch (JSONException | NullPointerException e) {
            if (retryCount <= 3) {
                logger.warning("Retrying #" + retryCount);
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

}
