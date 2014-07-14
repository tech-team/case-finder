package caseloader.kad;

import caseloader.Urls;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import util.HttpDownloader;
import util.JsonUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KadLoader {
    private Map<String, String> courts = new HashMap<>();

    public KadLoader() {

    }

    public Set<String> retrieveCourts() {
        String kadHtml = HttpDownloader.get(Urls.KAD_HOME);
        Document d = Jsoup.parse(kadHtml);
        Elements courtsDOM = d.child(0).select("#Courts").first().children();
        courtsDOM.stream().filter(c -> c.hasText() && c.hasAttr("value"))
                          .forEach(c -> courts.put(c.text(), c.attr("value")));
        return courts.keySet(); // TODO: should be a callback call perhaps
    }

    public String courtCode(String court) {
        return courts.get(court);
    }

    public KadData retrieveKadData(KadSearchRequest request) {
        KadResponse initial = retrieveKadResponse(request, 1);
        KadData data = null;

        if (initial.isSuccess()) {
            data = new KadData(initial.getPagesCount() * initial.getPageSize(), initial.getTotalCount());

            processKadResponse(initial, data);
            for (int i = 2; i < initial.getPagesCount(); ++i) {
                KadResponse resp = retrieveKadResponse(request, i);
                if (resp.isSuccess()) {
                    processKadResponse(resp, data);
                }
            }
        }
        return data;
    }

    private void processKadResponse(KadResponse resp, KadData outData) {
        List<KadResponseItem> items = resp.getItems();
        for (KadResponseItem item : items) {

            Map<String, String> params = new HashMap<>();
            Map<String, String> headers = new HashMap<>();
            params.put("id", item.getCaseId());
            headers.put("X-Requested-With", "XMLHttpRequest");
            JSONObject fullCard = new JSONObject(HttpDownloader.get(Urls.KAD_FULL_CARD, params, headers));

            if (fullCard.getBoolean("Success")) {
                try {
                    JSONObject result = JsonUtils.getJSONObject(fullCard, "Result");
                    JSONArray instances = JsonUtils.getJSONArray(result, "Instances");
                    JSONArray documents = instances.getJSONObject(instances.length() - 1).getJSONArray("Documents");
                    JSONObject lastDocument = documents.getJSONObject(documents.length() - 1);

                    String additionalInfo = JsonUtils.getString(lastDocument, "AdditionalInfo");

                    if (additionalInfo != null) {
                        String pattern = "(.*)(Сумма[\\D]*)(\\d*[,\\.]\\d*)$";
                        Pattern r = Pattern.compile(pattern);

                        Matcher m = r.matcher(additionalInfo);
                        if (m.find()) {
                            String costStr = m.group(3).replace(',', '.');
                            double cost = Double.parseDouble(costStr);
                            KadDataEntry entry = new KadDataEntry(item, cost);
                            outData.addEntry(entry);
                        }
                    }
                } catch (NullPointerException ignored) {

                }
            }
        }

    }

    private KadResponse retrieveKadResponse(KadSearchRequest request, int page) {
        request.setPage(page);

        String json = request.toString();
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Requested-With", "XMLHttpRequest");
        JSONObject jsonObj = HttpDownloader.post(Urls.KAD_SEARCH, json, headers);
        return KadResponse.fromJSON(jsonObj);
    }


    public static void main(String[] args) {
        KadLoader kl = new KadLoader();
        kl.retrieveKadData(new KadSearchRequest());
    }
}
