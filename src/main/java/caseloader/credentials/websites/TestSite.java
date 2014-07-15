package caseloader.credentials.websites;

import caseloader.credentials.Credentials;
import caseloader.credentials.SearchRequest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import util.HttpDownloader;

public class TestSite extends WebSite {
    @Override
    public String url() {
        return null;
    }

    @Override
    public Credentials findCredentials(SearchRequest request, final Credentials credentials) {
        Document page = downloadPage();
        return null;
    }

}
