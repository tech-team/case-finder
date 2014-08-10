package caseloader.credentials.websites;

import caseloader.credentials.Credentials;
import caseloader.credentials.CredentialsSearchRequest;
import util.DataRetrievingError;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import util.HttpDownloader;
import util.MyLogger;
import util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ListOrg extends WebSite {
    private static final int THRESHOLD = 5;
    private static final int PRIORITY = 1;
    private Logger logger = MyLogger.getLogger(this.getClass().toString());

    abstract class Urls {
        private static final String MAIN_PAGE = "http://www.list-org.com";
        private static final String SEARCH = "http://www.list-org.com/search.php";
    }

    abstract class By {
        private static final String NAME = "name";
        private static final String ADDRESS = "address";
        private static final String INN = "inn";
        private static final String OGRN = "ogrn";
    }

    @Override
    public String url() {
        return Urls.MAIN_PAGE;
    }

    @Override
    public Credentials findCredentials(CredentialsSearchRequest request, Credentials credentials) throws InterruptedException, DataRetrievingError {
        List<Elements> searchResults = new ArrayList<>(2);

        if (request.getInn() != null) {
            searchResults.add(findByInn(request));
        } else if (request.getOgrn() != null) {
            searchResults.add(findByOgrn(request));
        } else {
//            searchResults.add(findByAddress(request));
//            searchResults.add(findByName(request));
        }

        for (Elements results : searchResults) {
            if (results != null && results.size() != 0) {
                Credentials creds = parseSearchResults(results, request, credentials);
                if (creds != null)
                    return creds;
            }
        }

        return null;
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @SuppressWarnings("UnusedParameters")
    private Credentials parseSearchResults(Elements results, CredentialsSearchRequest request, Credentials credentials) throws InterruptedException, DataRetrievingError {

        if (results.size() >= THRESHOLD) {
            logger.warning("Too many results. Skipping.");
            return null;
        }

        Map<RelevanceInput, Double> relevances = new HashMap<>();

        for (Element result : results) {
            String companyUrl = Urls.MAIN_PAGE + result.getElementsByTag("a").attr("href");

            String resp = HttpDownloader.i().get(companyUrl, false);
            Element company = Jsoup.parse(resp)
                                   .body()
                                   .select(".main .content")
                                   .first();

            Credentials creds = new Credentials();

            // Finding director
            String director = company.getElementsByTag("table").first()
                                     .getElementsByTag("tr").first()
                                     .getElementsByTag("td").get(1)
                                     .text();
            creds.addDirector(Urls.MAIN_PAGE, director);

            // Finding other stuff
            String name = "";
            String address = "";
            Elements paragraphs = company.getElementsByTag("p");
            for (Element p : paragraphs) {
                Element i = p.getElementsByTag("i").first();
                if (i != null) {
                    switch (i.text()) {
                        case "Полное юридическое наименование:":
                            name = p.getElementsByTag("a").first().text();
                            break;
                        case "Телефон:":
                            if (!p.ownText().equals("")) {
                                String telephones = p.ownText();
                                creds.addTelephone(Urls.MAIN_PAGE, telephones);
                            }
                            break;
                        case "Адрес:":
                            if (!p.ownText().equals("")) {
                                address = p.ownText();
                            }
                        default:
                            break;
                    }
                }
            }

            RelevanceInput input = new RelevanceInput(creds, name, address, request);
            relevances.put(input, countRelevance(input));
        }

        return findWithBestRelevance(relevances);
    }



    private Elements executeSearch(List<NameValuePair> searchParams) throws InterruptedException, DataRetrievingError {
        String resp = HttpDownloader.i().get(Urls.SEARCH, searchParams, null, false);
        return Jsoup.parse(resp)
                    .body()
                    .select(".main .content p");
    }

    private Elements findByName(CredentialsSearchRequest request) throws InterruptedException, DataRetrievingError {
        String val = StringUtils.removeNonLetters(request.getCompanyName());
        if (val == null)
            return null;
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("type", By.NAME));
        params.add(new BasicNameValuePair("val", val));
        return executeSearch(params);
    }

    private Elements findByAddress(CredentialsSearchRequest request) throws InterruptedException, DataRetrievingError {
        String val = preprocessAddress(request.getAddress().getRaw());
        if (val == null)
            return null;
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("type", By.ADDRESS));
        params.add(new BasicNameValuePair("val", val));
        return executeSearch(params);
    }

    // TODO
    private String preprocessAddress(String address) {
        return StringUtils.removeNonLetters(address);
    }

    private Elements findByInn(CredentialsSearchRequest request) throws InterruptedException, DataRetrievingError {
        String val = request.getInn();
        if (val == null)
            return null;
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("type", By.INN));
        params.add(new BasicNameValuePair("val", val));
        return executeSearch(params);
    }

    private Elements findByOgrn(CredentialsSearchRequest request) throws InterruptedException, DataRetrievingError {
        String val = request.getOgrn();
        if (val == null)
            return null;
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("type", By.OGRN));
        params.add(new BasicNameValuePair("val", val));
        return executeSearch(params);
    }



    @SuppressWarnings("UnusedDeclaration")
    public static void main(String[] args) throws InterruptedException, DataRetrievingError {
        ListOrg lo = new ListOrg();

        CredentialsSearchRequest req = new CredentialsSearchRequest("ООО СТРОИТЕЛЬНАЯ КОМПАНИЯ НОВОСТРОЙ ИНЖИНИРИНГ", "unknown", null, null);
        Credentials creds = new Credentials();
        Credentials res = lo.findCredentials(req, creds);

    }
}
