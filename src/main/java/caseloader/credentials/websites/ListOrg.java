package caseloader.credentials.websites;

import caseloader.credentials.Credentials;
import caseloader.credentials.CredentialsSearchRequest;
import exceptions.DataRetrievingError;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;
import util.HttpDownloader;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListOrg extends WebSite {
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
    public Credentials findCredentials(CredentialsSearchRequest request, Credentials credentials) throws InterruptedException, IOException, DataRetrievingError {
        if (request.getInn() == null)
            request.setInn(credentials.getInn());
        if (request.getOgrn() == null)
            request.setOgrn(credentials.getOgrn());

        List<Elements> searchResults = new ArrayList<>(2);

        if (request.getInn() != null) {
            searchResults.add(findByInn(request));
        } else if (request.getOgrn() != null) {
            searchResults.add(findByOgrn(request));
        } else {
            searchResults.add(findByAddress(request));
            searchResults.add(findByName(request));
        }

        for (Elements results : searchResults) {
            if (results != null) {
                Credentials creds = parseSearchResults(results, request, credentials);
                if (creds != null)
                    return creds;
            }
        }

        return null;
    }

    private Credentials parseSearchResults(Elements results, CredentialsSearchRequest request, Credentials credentials) throws IOException, InterruptedException, DataRetrievingError {
        Map<Credentials, Double> relevances = new HashMap<>();

        for (Element result : results) {
            String companyUrl = Urls.MAIN_PAGE + result.getElementsByTag("a").attr("href");

            String resp = HttpDownloader.get(companyUrl);
            Element company = Jsoup.parse(resp)
                                   .body()
                                   .select(".main .content")
                                   .first();

            Credentials creds = new Credentials();
            double relevance = 0;

            // Finding director
            String director = company.getElementsByTag("table").first()
                                     .getElementsByTag("tr").first()
                                     .getElementsByTag("td").get(1)
                                     .text();
            creds.addDirector(Urls.MAIN_PAGE, director);

            Elements paragraphs = company.getElementsByTag("p");
            for (Element p : paragraphs) {
                Element i = p.getElementsByTag("i").first();
                if (i != null && i.text().equals("Телефон:")) {
                    if (!p.ownText().equals("")) {
                        String[] telephones = p.ownText().split("\\s+");
                        for (String tel : telephones) {
                            creds.addTelephone(Urls.MAIN_PAGE, tel);
                        }
                    }
                    break;
                }
            }

            relevances.put(creds, relevance);

        }

        Credentials best = null;
        double bestRelevance = 0.0;
        for (Map.Entry<Credentials, Double> entry : relevances.entrySet()) {
            double relevance = entry.getValue();
            if (best == null || relevance > bestRelevance) {
                best = entry.getKey();
                bestRelevance = relevance;
            }
        }

        return best;
    }

    private Elements executeSearch(List<NameValuePair> searchParams) throws InterruptedException, IOException, DataRetrievingError {
        String resp = HttpDownloader.get(Urls.SEARCH, searchParams, null, false);
        return Jsoup.parse(resp)
                    .body()
                    .select(".main .content p");
    }

    private Elements findByName(CredentialsSearchRequest request) throws InterruptedException, IOException, DataRetrievingError {
        String val = request.getCompanyName(); // TODO:  Probably preprocess
        if (val == null)
            return null;
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("type", By.NAME));
        params.add(new BasicNameValuePair("val", val));
        return executeSearch(params);
    }

    private Elements findByAddress(CredentialsSearchRequest request) throws InterruptedException, IOException, DataRetrievingError {
        String val = request.getAddress(); // TODO:  Probably preprocess
        if (val == null)
            return null;
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("type", By.ADDRESS));
        params.add(new BasicNameValuePair("val", val));
        return executeSearch(params);
    }

    private Elements findByInn(CredentialsSearchRequest request) throws InterruptedException, IOException, DataRetrievingError {
        String val = request.getInn();
        if (val == null)
            return null;
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("type", By.INN));
        params.add(new BasicNameValuePair("val", val));
        return executeSearch(params);
    }

    private Elements findByOgrn(CredentialsSearchRequest request) throws InterruptedException, IOException, DataRetrievingError {
        String val = request.getOgrn();
        if (val == null)
            return null;
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("type", By.OGRN));
        params.add(new BasicNameValuePair("val", val));
        return executeSearch(params);
    }

    public static void main(String[] args) throws InterruptedException, IOException, DataRetrievingError {
        String resp = HttpDownloader.get("http://list-org.com/company/292390", false);

        ListOrg lo = new ListOrg();

        CredentialsSearchRequest req = new CredentialsSearchRequest("БОГАТЫРЬ", "unknown", "1102017213", null);
        Credentials creds = new Credentials();
        Credentials res = lo.findCredentials(req, creds);
    }
}
