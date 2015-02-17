package caseloader.credentials.websites;

import caseloader.credentials.Credentials;
import caseloader.credentials.CredentialsSearchRequest;
import caseloader.util.RegionHelper;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import util.MyLogger;
import util.StringUtils;
import util.net.HttpDownloader;
import util.net.MalformedUrlException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

// For INN search
public class Kartoteka extends WebSite {

    abstract class Urls {
        private static final String MAIN_PAGE = "http://www.kartoteka.ru/";
        private static final String SEARCH_FORM = "http://www.kartoteka.ru/poisk_po_rekvizitam/";
        private static final String SEARCH_REQUEST = "http://www.kartoteka.ru/poisk_po_rekvizitam//?action=Hash";
    }

    abstract class QueryKeys {
        private static final String QUERY = "query";
        private static final String COUNTRY = "oksm[]";
        private static final String REGION = "regions[]";
        private static final String VALIDATE = "validate";
    }

    abstract class Countries {
        private static final String RUSSIA = "643";
    }

    private static final String ENCODING = "cp1251";
    private static final int COUNT_TO_PARSE = 3;

    private final Logger logger = MyLogger.getLogger(this.getClass().getName());


    @Override
    public String url() {
        return Urls.MAIN_PAGE;
    }

    @Override
    public Credentials findCredentials(CredentialsSearchRequest request, Credentials credentials) throws MalformedUrlException, InterruptedException {
        // make request to get "validate" value from HTML
        String validate = getValidateValue();

        // make search request to get "hash" from JSON
        String hash = getHashValue(request, validate);

        String resultsUrl = Urls.SEARCH_FORM + hash + "/";
        String resp = HttpDownloader.i().get(Urls.SEARCH_FORM, null, null, true, ENCODING);
        Credentials creds = parsePage(resp, request);
        if (creds != null)
            logger.info("<Kartoteka>: Found credentials for company: " + request.getCompanyName());
        else
            logger.warning("<Kartoteka>: Couldn't find credentials for company: " + request.getCompanyName());
        return creds;
    }

    private String getValidateValue()
            throws MalformedUrlException, InterruptedException {

        String page = HttpDownloader.i().get(Urls.SEARCH_FORM, null, null, true, ENCODING);

        Elements input = Jsoup.parse(page)
                .body()
                .getElementsByAttributeValue("name", "validate");

        if (!input.isEmpty())
            return input.get(0).val();
        else
            return null;
    }

    private String getHashValue(CredentialsSearchRequest request, String validate)
            throws MalformedUrlException, InterruptedException {

        String companyName = StringUtils.removeNonLetters(request.getCompanyName());

        String city = request.getAddress().getCity();

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(QueryKeys.QUERY, companyName));
        params.add(new BasicNameValuePair(QueryKeys.VALIDATE, validate));

        params.add(new BasicNameValuePair(QueryKeys.COUNTRY, Countries.RUSSIA));
        if (city != null) {
            List<String> possibleRegions = RegionHelper.regionsIdsByCity(city);
            if (possibleRegions.size() != 1) {
                System.out.println("Several regions found");
            }
            params.addAll(possibleRegions.stream()
                    .map(region -> new BasicNameValuePair(QueryKeys.REGION, region))
                    .collect(Collectors.toList()));
        }


        // post as formData
        String resp = HttpDownloader.i().post(Urls.SEARCH_REQUEST, params, null, true);

        JSONObject json = new JSONObject(resp);
        String hash = json.getString("hash");

        return hash;
    }

    private Credentials parsePage(String page, CredentialsSearchRequest request) {
        if (page == null) {
            return null;
        }

        try {
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


                RelevanceInput input = new RelevanceInput(creds, name, address, request);
                relevances.put(input, countRelevance(input));
            }

            return findWithBestRelevance(relevances);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Unexpected exception.", e);
            return null;
        }
    }


    @SuppressWarnings("UnusedDeclaration")
    public static void main(String[] args) throws MalformedUrlException, InterruptedException {
        Kartoteka k = new Kartoteka();
        CredentialsSearchRequest req = new CredentialsSearchRequest("ОАО Гамма Траст", "");
        Credentials creds = new Credentials();
        Credentials c = k.findCredentials(req, creds);

        System.out.println("hi");
    }
}
