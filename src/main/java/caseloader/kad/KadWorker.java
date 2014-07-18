package caseloader.kad;

import caseloader.Urls;
import caseloader.credentials.Credentials;
import caseloader.credentials.CredentialsLoader;
import caseloader.credentials.CredentialsSearchRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import util.HttpDownloader;
import util.JsonUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KadWorker implements Runnable {
    private static final CredentialsLoader CREDENTIALS_LOADER = new CredentialsLoader();
    private KadResponseItem item;
    private final KadData data;

    public KadWorker(KadResponseItem item, KadData data) {
        this.item = item;
        this.data = data;
    }

    private void processSucceeded(KadDataEntry entry) {
// TODO
//        for (KadResponseSide defendant : entry.getItem().getDefendants()) {
//            CredentialsSearchRequest credentialsSearchRequest =
//                    new CredentialsSearchRequest(defendant.getName(),
//                            defendant.getAddress(),
//                            defendant.getInn(),
//                            defendant.getOgrn());
//            Credentials defendantCredentials = CREDENTIALS_LOADER.retrieveCredentials(credentialsSearchRequest);
//            defendant.setCredentials(defendantCredentials);
//        }
        synchronized (data) {
            data.addEntry(entry);
        }
    }

    @Override
    public void run() {
        System.out.println("[" + Thread.currentThread().getName() + "] Processing case = " + item.getCaseId());

        Map<String, String> params = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        params.put("number", item.getCaseNumber());
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json, text/javascript, */*");
        JSONObject caseInfo = new JSONObject(HttpDownloader.get(Urls.KAD_CARD, params, headers));

        if (JsonUtils.getBoolean(caseInfo, "Success")) {
            JSONObject result = JsonUtils.getJSONObject(caseInfo, "Result");
            try {
                Double cost = JsonUtils.getDouble(result, "ClaimSum");
                if (cost != null && cost != 0) {
                    KadDataEntry entry = new KadDataEntry(this.item, cost);
                    synchronized (data) {
                        data.addEntry(entry);
                    }
                }
            } catch (NullPointerException ignored) {

            }
        }


//        String card = HttpDownloader.get(Urls.KAD_CARD + item.getCaseId());
//        Document d = Jsoup.parse(card);
//        Element lastInstance = d.child(0).select("#chrono_list_content").first().children().last();
//        String instanceId = lastInstance.attr("data-id");
//
//        try {
//            Map<String, String> params = new HashMap<>();
//            Map<String, String> headers = new HashMap<>();
//            params.put("id", instanceId);
//            params.put("withProtocols", "false");
//            params.put("perPage", "0");
//            params.put("page", "0");
//            headers.put("X-Requested-With", "XMLHttpRequest");
//            headers.put("Content-Type", "application/json");
//            headers.put("Accept", "application/json, text/javascript, */*");
//            JSONObject instanceDocuments = new JSONObject(HttpDownloader.get(Urls.KAD_INSTANCE_DOCUMENTS, params, headers));
//
//            if (JsonUtils.getBoolean(instanceDocuments, "Success")) {
//
//                Integer pagesCount = instanceDocuments.getJSONObject("Result").getInt("TotalCount");
//
//                params.put("perPage", "1");
//                params.put("page", pagesCount.toString());
//                JSONObject document = new JSONObject(HttpDownloader.get(Urls.KAD_INSTANCE_DOCUMENTS, params, headers));
//
//                JSONObject result = JsonUtils.getJSONObject(document, "Result");
//                JSONObject item = JsonUtils.getJSONArray(result, "Items").getJSONObject(0);
//                String additionalInfo = JsonUtils.getString(item, "AdditionalInfo");
//
//                if (additionalInfo != null) {
//                    String pattern = "(.*)(Сумма[\\D]*)(\\d*[,\\.]\\d*)$";
//                    Pattern r = Pattern.compile(pattern);
//
//                    Matcher m = r.matcher(additionalInfo);
//                    if (m.find()) {
//                        String costStr = m.group(3).replace(',', '.');
//                        double cost = Double.parseDouble(costStr);
//                        KadDataEntry entry = new KadDataEntry(this.item, cost);
//                        synchronized (data) {
//                            data.addEntry(entry);
//                        }
//                    }
//                }
//            }
//        } catch (NullPointerException ignored) {
//
//        }

        System.out.println("[" + Thread.currentThread().getName() + "] Finished case = " + item.getCaseId());
    }
}
