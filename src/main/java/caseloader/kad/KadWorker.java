package caseloader.kad;

import caseloader.CaseInfo;
import caseloader.CaseSide;
import caseloader.Urls;
import caseloader.credentials.Credentials;
import caseloader.credentials.CredentialsLoader;
import caseloader.credentials.CredentialsSearchRequest;
import org.json.JSONObject;
import util.*;

import java.util.HashMap;
import java.util.Map;

public class KadWorker <OutputType extends util.Appendable<CaseInfo> > implements Runnable {
    private static final CredentialsLoader CREDENTIALS_LOADER = new CredentialsLoader();
    private final CaseInfo caseInfo;
    private final OutputType data;

    public KadWorker(CaseInfo caseInfo, OutputType data) {
        this.caseInfo = caseInfo;
        this.data = data;
    }

    @Override
    public void run() {
        System.out.println("[" + Thread.currentThread().getName() + "] Processing case = " + caseInfo.getCaseId());

        Map<String, String> params = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        params.put("number", caseInfo.getCaseNumber());
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json, text/javascript, */*");
        JSONObject caseInfo = new JSONObject(HttpDownloader.get(Urls.KAD_CARD, params, headers));

        if (JsonUtils.getBoolean(caseInfo, "Success")) {
            JSONObject result = JsonUtils.getJSONObject(caseInfo, "Result");
            try {
                Double cost = JsonUtils.getDouble(result, "ClaimSum");
                if (cost != null && cost != 0) {
                    this.caseInfo.setCost(cost);

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
            } catch (NullPointerException ignored) {

            }
        }

        System.out.println("[" + Thread.currentThread().getName() + "] Finished case = " + this.caseInfo.getCaseId());
    }
}
