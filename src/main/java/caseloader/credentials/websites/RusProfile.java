package caseloader.credentials.websites;

import caseloader.credentials.Credentials;
import caseloader.credentials.CredentialsSearchRequest;
import exceptions.DataRetrievingError;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class RusProfile extends WebSite {
    abstract class Urls {
        private static final String MAIN_PAGE = "http://www.rusprofile.ru/";
        private static final String SEARCH = "http://www.google.ru/cse?cx=partner-pub-2023889461799885%3A1217393034&cof=FORID%3A10&ie=UTF-8&ad=n9&num=10&gsc.page=1&q=";
    }

    @Override
    public String url() {
        return Urls.MAIN_PAGE;
    }

    @Override
    public Credentials findCredentials(final CredentialsSearchRequest request, final Credentials credentials) throws IOException, DataRetrievingError, InterruptedException {
//        if (request.getInn() != null) {
//            return findByInn(request, credentials);
//        } else if (request.getOgrn() != null) {
//            return findByOgrn(request, credentials);
//        }

        Credentials res = new Credentials();

        return res;
    }

    private Credentials findByInn(CredentialsSearchRequest request, Credentials credentials) throws IOException, DataRetrievingError, InterruptedException {
        Element searchPage = downloadPage(Urls.SEARCH + request.getInn()).getElementsByTag("html").first();
        Element googleResults = searchPage.select(".gs-webResult .gs-result").first();
        return null;
    }

    private Credentials findByOgrn(CredentialsSearchRequest request, Credentials credentials) {
        return null;
    }

    public static void main(String[] args) throws IOException, DataRetrievingError, InterruptedException {
        RusProfile rp = new RusProfile();

        CredentialsSearchRequest req = new CredentialsSearchRequest("БОГАТЫРЬ", "unknown", "1102017213", null);
        Credentials creds = new Credentials();
        rp.findCredentials(req, creds);
    }
}
