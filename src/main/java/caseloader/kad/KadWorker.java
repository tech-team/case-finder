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

public class KadWorker <CaseContainerType extends util.Appendable<CaseInfo> > implements Runnable {
    private static final CredentialsLoader CREDENTIALS_LOADER = new CredentialsLoader();
    private final CaseInfo caseInfo;
    private final CaseContainerType data;
    private final int minCost;

    public KadWorker(CaseInfo caseInfo, int minCost, CaseContainerType data) {
        this.caseInfo = caseInfo;
        this.data = data;
        this.minCost = minCost;
    }

    @Override
    public void run() {
        System.out.println("[" + Thread.currentThread().getName() + "] Processing case = " + caseInfo.getCaseNumber());

        List<NameValuePair> params = new ArrayList<>();
        Map<String, String> headers = new HashMap<>();
        params.add(new BasicNameValuePair("number", caseInfo.getCaseNumber()));
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json, text/javascript, */*");
        JSONObject caseInfo = null;
        String json = "";
        try {
            json = HttpDownloader.get(Urls.KAD_CARD, params, headers);
            caseInfo = new JSONObject(json);
        } catch (IOException | DataRetrievingError | JSONException e) {
            System.out.println("[" + Thread.currentThread().getName() + "] json=" + json);
            System.out.println("[" + Thread.currentThread().getName() + "] Retrying");
            run();
            return;
//            throw new RuntimeException(e);
        }

        if (JsonUtils.getBoolean(caseInfo, "Success")) {
            JSONObject result = JsonUtils.getJSONObject(caseInfo, "Result");
            try {
                Double cost = JsonUtils.getDouble(result, "ClaimSum");
                if (cost != null && cost != 0 && cost >= minCost) {
                    this.caseInfo.setCost(cost);

                    this.caseInfo.splitSides();
                    for (CaseSide defendant : this.caseInfo.getDefendants()) {
                        CredentialsSearchRequest credentialsSearchRequest =
                                new CredentialsSearchRequest(defendant.getName(),
                                        defendant.getAddress(),
                                        defendant.getInn(),
                                        defendant.getOgrn());
                        Credentials defendantCredentials = CREDENTIALS_LOADER.retrieveCredentials(credentialsSearchRequest);
                        defendant.setCredentials(defendantCredentials);
                    }
                    synchronized (data) {
                        data.append(this.caseInfo);
                    }
                }
            } catch (NullPointerException e) {
                System.err.println(e.getMessage());
            }

            System.out.println("[" + Thread.currentThread().getName() + "] Finished case = " + this.caseInfo.getCaseNumber());
        } else {
            System.out.println("[" + Thread.currentThread().getName() + "] Case #" + this.caseInfo.getCaseNumber() + " failed");
        }
    }
}
