package caseloader.kad;

import caseloader.CaseInfo;
import caseloader.CaseSide;
import caseloader.credentials.Credentials;
import caseloader.credentials.CredentialsLoader;
import caseloader.credentials.CredentialsSearchRequest;
import eventsystem.Event;
import exceptions.DataRetrievingError;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import util.*;

import java.io.IOException;
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
    private final Event caseProcessed;

    public KadWorkerFactory(int searchLimit, long minCost, CaseContainerType data, CredentialsLoader credentialsLoader, Event caseProcessed) {
        this.searchLimit = searchLimit;
        this.minCost = minCost;
        this.data = data;
        this.credentialsLoader = credentialsLoader;
        this.caseProcessed = caseProcessed;
    }

    public Runnable buildWorker(final int id, final CaseInfo caseInfo) {
        return new Runnable() {
            @Override
            public void run() {
                Logger logger = MyLogger.getLogger(this.getClass().toString());

                for (int retry = 1; retry <= 3; ++retry) {
                    logger.info(String.format("Processing case %d/%d = %s", id, searchLimit, caseInfo.getCaseNumber()));

                    List<NameValuePair> params = new ArrayList<>();
                    Map<String, String> headers = new HashMap<>();
                    params.add(new BasicNameValuePair("number", caseInfo.getCaseNumber()));
                    headers.put("X-Requested-With", "XMLHttpRequest");
                    headers.put("Content-Type", "application/json");
                    headers.put("Accept", "application/json, text/javascript, */*");
                    JSONObject caseJson = null;
                    String json = "";
                    try {
                        json = HttpDownloader.i().get(Urls.KAD_CARD, params, headers);
                        caseJson = new JSONObject(json);
                    } catch (IOException | DataRetrievingError | JSONException | NullPointerException e) {
                        logger.warning("Error retrieving case " + caseInfo.getCaseNumber() + ". Retrying #" + retry);
                        continue;
                    } catch (InterruptedException e) {
                        System.out.println(Thread.currentThread().getName() + ". INTERRUPTED");
                        return;
                    }

                    try {
                        if (JsonUtils.getBoolean(caseJson, "Success")) {
                            JSONObject result = JsonUtils.getJSONObject(caseJson, "Result");
                            try {
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
                            } catch (NullPointerException e) {
                                logger.severe(e.getMessage());
                                throw e;
                            }
                            caseProcessed.fire();
                            logger.info(String.format("Finished case %d/%d = %s", id, searchLimit, caseInfo.getCaseNumber()));
                        } else {
                            logger.info(String.format("Case %d/%d = %s failed", id, searchLimit, caseInfo.getCaseNumber()) + ". Retrying");
                            continue;
                        }
                        return;
                    } catch (InterruptedException e) {
                        System.out.println("INTERRUPTED");
                        return;
                    }
                }
                caseProcessed.fire();
                logger.warning("Couldn't retrieve case " + caseInfo.getCaseNumber() + ". Breaking");
            }
        };
    }
}
