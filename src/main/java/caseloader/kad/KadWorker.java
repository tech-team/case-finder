package caseloader.kad;

import caseloader.CaseInfo;
import caseloader.CaseSide;
import caseloader.credentials.Credentials;
import caseloader.credentials.CredentialsLoader;
import caseloader.credentials.CredentialsSearchRequest;
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

public class KadWorker <CaseContainerType extends util.Appendable<CaseInfo> > implements Runnable {
    private final int id;
    private final CaseInfo caseInfo;
    private final CaseContainerType data;
    private final int minCost;
    private final CredentialsLoader credentialsLoader;
    private Logger logger = MyLogger.getLogger(this.getClass().toString());

    public KadWorker(int id, CaseInfo caseInfo, int minCost, CaseContainerType data, CredentialsLoader credentialsLoader) {
        this.id = id;
        this.caseInfo = caseInfo;
        this.data = data;
        this.minCost = minCost;
        this.credentialsLoader = credentialsLoader;
    }

    @Override
    public void run() {
        logger.info(String.format("Processing case %d/%d = %s", id, KadLoader.TOTAL_MAX_COUNT, caseInfo.getCaseNumber()));

        List<NameValuePair> params = new ArrayList<>();
        Map<String, String> headers = new HashMap<>();
        params.add(new BasicNameValuePair("number", caseInfo.getCaseNumber()));
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json, text/javascript, */*");
        JSONObject caseJson = null;
        String json = "";
        try {
            json = HttpDownloader.get(Urls.KAD_CARD, params, headers);
            caseJson = new JSONObject(json);
        } catch (IOException | DataRetrievingError | JSONException e) {
            logger.warning("Error retrieving case " + caseInfo.getCaseNumber() + ". Retrying");
            run();
            return;
//            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            System.out.println("INTERRUPTED");
            return;
        }

        try {
            if (JsonUtils.getBoolean(caseJson, "Success")) {
                JSONObject result = JsonUtils.getJSONObject(caseJson, "Result");
                try {
                    Double cost = JsonUtils.getDouble(result, "ClaimSum");
                    if (cost != null && cost != 0 && cost >= minCost) {
                        caseInfo.setCost(cost);

                        caseInfo.splitSides();
                        for (CaseSide defendant : caseInfo.getDefendants()) {
                            CredentialsSearchRequest credentialsSearchRequest =
                                    new CredentialsSearchRequest(defendant.getName(),
                                            defendant.getAddress(),
                                            defendant.getInn(),
                                            defendant.getOgrn());
                            Credentials defendantCredentials =
                                    credentialsLoader.retrieveCredentials(credentialsSearchRequest);
                            defendant.setCredentials(defendantCredentials);
                        }
                        synchronized (data) {
                            data.append(caseInfo);
                        }
                    }
                } catch (NullPointerException e) {
                    logger.severe(e.getMessage());
                }

                logger.info(String.format("Finished case %d/%d = %s", id, KadLoader.TOTAL_MAX_COUNT, this.caseInfo.getCaseNumber()));
            } else {
                logger.info(String.format("Case %d/%d = %s failed", id, KadLoader.TOTAL_MAX_COUNT, this.caseInfo.getCaseNumber()));
            }
        } catch (InterruptedException e) {
            System.out.println("INTERRUPTED");
        }
    }
}
