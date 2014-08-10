package caseloader.kad;

import caseloader.CaseInfo;
import caseloader.CaseLoaderEvents;
import caseloader.CaseSide;
import caseloader.credentials.Credentials;
import caseloader.credentials.CredentialsLoader;
import caseloader.credentials.CredentialsSearchRequest;
import util.DataRetrievingError;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import util.HttpDownloader;
import util.JsonUtils;
import util.MyLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class KadWorkerFactory<CaseContainerType extends util.Appendable<CaseInfo> > {
    private final CaseContainerType data;
    private static final Object dataLock = new Object();
    private final int searchLimit;
    private final long minCost;
    private final CredentialsLoader credentialsLoader;

    public KadWorkerFactory(int searchLimit, long minCost, CaseContainerType data, CredentialsLoader credentialsLoader) {
        this.searchLimit = searchLimit;
        this.minCost = minCost;
        this.data = data;
        this.credentialsLoader = credentialsLoader;
    }

    public Runnable buildWorker(final int id, final CaseInfo caseInfo) {
        return new Runnable() {
            @Override
            public void run() {
                Logger logger = MyLogger.getLogger(this.getClass().toString());

                int maxRetries = 3;
                for (int retry = 0; retry <= maxRetries + 1; ++retry) {
                    logger.info(String.format("-- Processing case %d/%d = %s", id, searchLimit, caseInfo.getCaseNumber()));

                    List<NameValuePair> params = new ArrayList<>();
                    Map<String, String> headers = new HashMap<>();
                    params.add(new BasicNameValuePair("number", caseInfo.getCaseNumber()));
                    headers.put("X-Requested-With", "XMLHttpRequest");
                    headers.put("Content-Type", "application/json");
                    headers.put("Accept", "application/json, text/javascript, */*");
                    JSONObject caseJson;
                    String json;

                    try {
                        json = HttpDownloader.i().get(Urls.KAD_CARD, params, headers);
                        caseJson = new JSONObject(json);

                        if (JsonUtils.getBoolean(caseJson, "Success")) {
                            JSONObject result = JsonUtils.getJSONObject(caseJson, "Result");
                            Double cost = JsonUtils.getDouble(result, "ClaimSum");
                            if (minCost == 0 || (cost != null && cost >= minCost)) {
                                caseInfo.setCost(cost);

                                caseInfo.splitSides();
                                for (CaseSide defendant : caseInfo.getDefendants()) {
                                    if (!defendant.isPhysical()) {
                                        CredentialsSearchRequest credentialsSearchRequest =
                                                new CredentialsSearchRequest(defendant.getName(),
                                                        defendant.getAddress(),
                                                        defendant.getInn(),
                                                        defendant.getOgrn());
                                        Credentials defendantCredentials =
                                                credentialsLoader.retrieveCredentials(credentialsSearchRequest);
                                        defendant.setCredentials(defendantCredentials);
                                    }
                                }

                                if (Thread.currentThread().isInterrupted()) {
                                    throw new InterruptedException();
                                }
                                synchronized (dataLock) {
                                    data.append(caseInfo);
                                }
                            }
                            CaseLoaderEvents.instance().caseProcessed.fire();
                            logger.info(String.format("-- Finished case %d/%d = %s", id, searchLimit, caseInfo.getCaseNumber()));
                        } else {
                            if (retry > maxRetries) {
                                break;
                            }
                            logger.info(String.format("-- Case %d/%d = %s failed", id, searchLimit, caseInfo.getCaseNumber()) + ". Retry #" + (retry + 1));
                            Thread.sleep(50);
                            continue;
                        }
                        return;

                    } catch (DataRetrievingError | JSONException | NullPointerException e) {
                        if (retry > maxRetries) {
                            break;
                        }
                        logger.warning("-- Error retrieving case " + caseInfo.getCaseNumber() + ". Retry #" + (retry + 1));
                        continue;
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                CaseLoaderEvents.instance().caseProcessed.fire();
                logger.warning("-- Couldn't retrieve case " + caseInfo.getCaseNumber() + ". Breaking");
            }
        };
    }
}
