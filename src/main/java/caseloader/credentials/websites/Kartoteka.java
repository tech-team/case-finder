package caseloader.credentials.websites;

import caseloader.credentials.Credentials;
import caseloader.credentials.CredentialsSearchRequest;
import exceptions.DataRetrievingError;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import util.HttpDownloader;
import caseloader.util.RegionHelper;
import util.MyLogger;
import util.StringUtils;

import java.util.*;
import java.util.logging.Logger;

// For INN search
public class Kartoteka extends WebSite {

    abstract class Urls {
        private static final String MAIN_PAGE = "http://www.kartoteka.ru/";
        private static final String SEARCH = "http://www.kartoteka.ru/poisk_po_rekvizitam/";
    }

    abstract class QueryKeys {
        private static final String QUERY = "query";
        private static final String COUNTRY = "oksm[]";
        private static final String REGION = "regions[]";
    }

    abstract class Countries {
        private static final String RUSSIA = "643";
    }

    private static final String ENCODING = "cp1251";
    private static final int COUNT_TO_PARSE = 3;
    private static final int PRIORITY = 2;

    private final Logger logger = MyLogger.getLogger(this.getClass().getName());


    @Override
    public String url() {
        return Urls.MAIN_PAGE;
    }

    @Override
    public Credentials findCredentials(CredentialsSearchRequest request, Credentials credentials) throws DataRetrievingError, InterruptedException {
        String companyName = StringUtils.removeNonLetters(request.getCompanyName());

        String city = request.getAddress().getCity();

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(QueryKeys.QUERY, companyName));
        params.add(new BasicNameValuePair(QueryKeys.COUNTRY, Countries.RUSSIA));
        if (city != null)
            params.add(new BasicNameValuePair(QueryKeys.REGION, RegionHelper.regionIdByCity(city)));

        String resp = HttpDownloader.i().get(Urls.SEARCH, params, null, true, ENCODING);
        Credentials creds = parsePage(resp, request);
        if (creds != null)
            logger.info("<Kartoteka>: Found credentials for company: " + request.getCompanyName());
        else
            logger.warning("<Kartoteka>: Couldn't find credentials for company: " + request.getCompanyName());
        return creds;
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    private Credentials parsePage(String page, CredentialsSearchRequest request) {
        if (page == null) {
            return null;
        }
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
    }


    @SuppressWarnings("UnusedDeclaration")
    public static void main(String[] args) throws DataRetrievingError, InterruptedException {
        Kartoteka k = new Kartoteka();
        CredentialsSearchRequest req = new CredentialsSearchRequest("ОАО Гамма Траст", "");
        Credentials creds = new Credentials();
        Credentials c = k.findCredentials(req, creds);

        System.out.println("hi");
    }
}
