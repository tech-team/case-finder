package caseloader.credentials.websites;

import caseloader.CaseSearchRequest;
import caseloader.credentials.Credentials;
import caseloader.credentials.CredentialsSearchRequest;
import exceptions.DataRetrievingError;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import util.HttpDownloader;
import util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// For INN search
public class Kartoteka extends WebSite {

    abstract class Urls {
        private static final String MAIN_PAGE = "http://www.kartoteka.ru/";
        private static final String SEARCH = "http://www.kartoteka.ru/poisk_po_rekvizitam/";
    }

    private static final String ENCODING = "cp1251";
    private static final int COUNT_TO_PARSE = 3;
    private static final int PRIORITY = 2;


    @Override
    public String url() {
        return Urls.MAIN_PAGE;
    }

    @Override
    public Credentials findCredentials(CredentialsSearchRequest request, Credentials credentials) throws IOException, DataRetrievingError, InterruptedException {
        String companyName = StringUtils.removeNonLetters(request.getCompanyName());

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("query", companyName));

        String resp = HttpDownloader.get(Urls.SEARCH, params, null, true, ENCODING);
        return parsePage(resp, request, credentials);
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    private Credentials parsePage(String page, CredentialsSearchRequest request, Credentials totalCreds) {
        Elements items = Jsoup.parse(page)
                              .body()
                              .getElementsByClass("page-content-company");

        int size = Math.min(COUNT_TO_PARSE, items.size());
        Map<RelevanceInput, Double> relevances = new HashMap<>(size);

        for (int i = 0; i < size; ++i) {
            Element item = items.get(i);
            Credentials creds = new Credentials();
            String name = item.getElementsByTag("h2").first().text();
            String address = "";

            Elements tableRow = item.getElementsByTag("table").first()
                                    .getElementsByTag("tr").first()
                                    .getElementsByTag("td");

            Elements firstCol = tableRow.first().children();
            int addressIndex = -1;
            for (int j = 0; j < firstCol.size(); ++j) {
                Element elem = firstCol.get(j);
                if (elem.tagName().equals("i")
                        && elem.className().equals("fa-map-marker")
                        && j + 1 < firstCol.size()) {
                    addressIndex = j + 1;
                    break;
                }
            }

            if (addressIndex != -1) {
                address = firstCol.get(addressIndex).ownText();
            }

            Elements secondCol = tableRow.last().children();
            for (Element div : secondCol) {
                String header = div.getElementsByClass("page-content-company-information-rekvizity")
                                   .first()
                                   .text();
                String value = div.getElementsByClass("page-content-company-information-rekvizity-date")
                                  .first()
                                  .text();
                if (header.startsWith("ОГРН")) {
                    creds.setOgrn(value);
                } else if (header.startsWith("ИНН")) {
                    creds.setInn(value);
                }
            }


            RelevanceInput input = new RelevanceInput(creds, name, address, request, totalCreds);
            relevances.put(input, countRelevance(input));
        }

        return findWithBestRelevance(relevances);
    }


    public static void main(String[] args) throws DataRetrievingError, IOException, InterruptedException {
        Kartoteka k = new Kartoteka();
        CredentialsSearchRequest req = new CredentialsSearchRequest("ОАО Гамма Траст", "");
        Credentials creds = new Credentials();
        Credentials c = k.findCredentials(req, creds);

        System.out.println("hi");
    }
}
