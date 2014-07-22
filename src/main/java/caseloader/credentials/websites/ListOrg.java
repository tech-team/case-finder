package caseloader.credentials.websites;

import caseloader.credentials.Credentials;
import caseloader.credentials.CredentialsSearchRequest;
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
    public Credentials findCredentials(CredentialsSearchRequest request, Credentials credentials) {
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
                Credentials creds = null;
                try {
                    creds = parseSearchResults(results, request, credentials);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1); // TODO
                }
                if (creds != null)
                    return creds;
            }
        }

        return null;
    }

    private Credentials parseSearchResults(Elements results, CredentialsSearchRequest request, Credentials credentials) throws IOException {
        Map<Credentials, Double> relevances = new HashMap<>();

        for (Element result : results) {
            String companyUrl = Urls.MAIN_PAGE + result.getElementsByTag("a").attr("href");


            Element company = Jsoup.connect(companyUrl)
                                   .userAgent(HttpDownloader.USER_AGENT)
                                   .get()
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
        for (Credentials creds : relevances.keySet()) {
            double relevance = relevances.get(creds);
            if (best == null || relevance > bestRelevance) {
                best = creds;
                bestRelevance = relevance;
            }
        }

        return best;
    }

    private Elements executeSearch(Map<String, String> searchParams) {
        try {
            return Jsoup.connect(Urls.SEARCH)
                        .data(searchParams)
                        .userAgent(HttpDownloader.USER_AGENT)
                        .get()
                        .body()
                        .select(".main .content p");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1); // TODO
        }
        return null;
    }

    private Elements findByName(CredentialsSearchRequest request) {
        String val = request.getCompanyName(); // TODO:  Probably preprocess
        if (val == null)
            return null;
        Map<String, String> params = new HashMap<>();
        params.put("type", By.NAME);
        params.put("val", val);
        return executeSearch(params);
    }

    private Elements findByAddress(CredentialsSearchRequest request) {
        String val = request.getAddress(); // TODO:  Probably preprocess
        if (val == null)
            return null;
        Map<String, String> params = new HashMap<>();
        params.put("type", By.ADDRESS);
        params.put("val", val);
        return executeSearch(params);
    }

    private Elements findByInn(CredentialsSearchRequest request) {
        String val = request.getInn();
        if (val == null)
            return null;
        Map<String, String> params = new HashMap<>();
        params.put("type", By.INN);
        params.put("val", val);
        return executeSearch(params);
    }

    private Elements findByOgrn(CredentialsSearchRequest request) {
        String val = request.getOgrn();
        if (val == null)
            return null;
        Map<String, String> params = new HashMap<>();
        params.put("type", By.OGRN);
        params.put("val", val);
        return executeSearch(params);
    }

    public static void main(String[] args) {
        ListOrg lo = new ListOrg();

        CredentialsSearchRequest req = new CredentialsSearchRequest("БОГАТЫРЬ", "unknown", "1102017213", null);
        Credentials creds = new Credentials();
        lo.findCredentials(req, creds);
    }
}
