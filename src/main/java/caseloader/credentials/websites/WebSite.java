package caseloader.credentials.websites;

import caseloader.credentials.Credentials;
import caseloader.credentials.SearchRequest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import util.HttpDownloader;

public abstract class WebSite {
    public abstract String url();
    public abstract Credentials findCredentials(SearchRequest request, final Credentials credentials);

    protected Document downloadPage() {
        String html = HttpDownloader.get(url());
        return Jsoup.parse(html, url());
    }
}
