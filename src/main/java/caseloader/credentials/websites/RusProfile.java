package caseloader.credentials.websites;

import caseloader.credentials.Credentials;
import caseloader.credentials.CredentialsSearchRequest;
import exceptions.DataRetrievingError;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import util.HttpDownloader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class RusProfile extends WebSite {
    private static final int PRIORITY = 0;

    abstract class Urls {
        private static final String MAIN_PAGE = "http://www.rusprofile.ru/";
        private static final String GOOGLE_SITE_URL = "rusprofile.ru";
        private static final String SEARCH = "https://www.google.ru/search";
    }

    @Override
    public String url() {
        return Urls.MAIN_PAGE;
    }

    @Override
    public Credentials findCredentials(final CredentialsSearchRequest request, final Credentials credentials) throws IOException, DataRetrievingError, InterruptedException {
        if (request.getInn() != null) {
            return findByInn(request);
        } else if (request.getOgrn() != null) {
            return findByOgrn(request);
        } else {
            return findByNameAndAddress(request);
        }
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    private Credentials findByInn(CredentialsSearchRequest request) throws IOException, DataRetrievingError, InterruptedException {
        String searchRequest = createGoogleRequest(request.getInn());
        String companyUrl = getCompanyUrl(searchRequest);
        return parseCompanyPage(companyUrl);
    }

    private Credentials findByOgrn(CredentialsSearchRequest request) throws InterruptedException, DataRetrievingError, IOException {
        String searchRequest = createGoogleRequest(request.getOgrn());
        String companyUrl = getCompanyUrl(searchRequest);
        return parseCompanyPage(companyUrl);
    }

    private Credentials findByNameAndAddress(CredentialsSearchRequest request) throws InterruptedException, DataRetrievingError, IOException {
        String searchRequest = createGoogleRequest(request.getCompanyName() + " " + request.getAddress().getRaw());
        String companyUrl = getCompanyUrl(searchRequest);
        return parseCompanyPage(companyUrl);
    }

    private Credentials parseCompanyPage(String companyUrl) throws InterruptedException, DataRetrievingError, IOException {
        if (companyUrl == null)
            return null;
        try {
            String resp = HttpDownloader.get(companyUrl);
            Element content = Jsoup.parse(resp)
                    .body()
                    .getElementById("content");

            Elements generalInfo = content.getElementById("general-info").getElementsByTag("tr");
            String director = null;
            String[] telephones = null;

            for (Element tr : generalInfo) {
                String key = tr.getElementsByTag("td").first().text();
                String value = tr.getElementsByTag("td").last().text();
                if (key.contains("тел")) {
                    telephones = value.split("\\s+");
                } else if (key.contains("комп")) {
                    director = value;
                }
            }

            String inn = null;
            String ogrn = null;

            Elements companyDetails = content.getElementById("company-details").getElementsByTag("tr");
            for (Element tr : companyDetails) {
                String key = tr.getElementsByTag("td").first().text();
                String value = tr.getElementsByTag("td").last().text();
                if (key.contains("ОГРН")) {
                    ogrn = value;
                } else if (key.contains("ИНН")) {
                    inn = value;
                }
            }

            Credentials creds = new Credentials();
            creds.addDirector(Urls.MAIN_PAGE, director);
            creds.addTelephones(Urls.MAIN_PAGE, telephones);

            if (creds.getInn() == null) {
                creds.setInn(inn);
            }

            if (creds.getOgrn() == null) {
                creds.setOgrn(ogrn);
            }

            return creds;
        } catch (NullPointerException e) {
            System.out.println("Something is null");
        }
        return null;
    }

    private String getCompanyUrl(String searchQuery) throws InterruptedException, DataRetrievingError, IOException {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("q", searchQuery));
        String googleResp = HttpDownloader.get(Urls.SEARCH, params, null, false);
        Elements results = Jsoup.parse(googleResp)
                                .body()
                                .getElementsByClass("g");
        try {
            String googleCompanyUrl = results.first()
                                             .getElementsByTag("h3").first()
                                             .getElementsByTag("a").first()
                                             .attr("href");


            List<NameValuePair> queryParams = new URIBuilder(Urls.SEARCH + googleCompanyUrl).getQueryParams();
            for (NameValuePair pair : queryParams) {
                if (pair.getName().equals("q")) {
                    return pair.getValue();
                }
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.out.println("Something is null");
        }
        return null;
    }

    private String createGoogleRequest(String query) {
        return query + " site:" + Urls.GOOGLE_SITE_URL;
    }

    public static void main(String[] args) throws IOException, DataRetrievingError, InterruptedException {
        RusProfile rp = new RusProfile();

        CredentialsSearchRequest req = new CredentialsSearchRequest(null, null, "1102017213", null);
        Credentials credentials = new Credentials();
        Credentials creds = rp.findCredentials(req, credentials);
    }
}
