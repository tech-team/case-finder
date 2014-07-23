package caseloader.credentials.websites;

import caseloader.credentials.Credentials;
import caseloader.credentials.CredentialsSearchRequest;
import exceptions.DataRetrievingError;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import util.HttpDownloader;

import java.io.IOException;

public abstract class WebSite {
    public abstract String url();
    public abstract Credentials findCredentials(final CredentialsSearchRequest request, final Credentials credentials) throws IOException, DataRetrievingError;

    protected Document downloadPage(String url) throws IOException, DataRetrievingError {
        String html = HttpDownloader.get(url);
        return Jsoup.parse(html, url);
    }
}
