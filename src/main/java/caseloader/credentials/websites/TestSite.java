package caseloader.credentials.websites;

import caseloader.credentials.Credentials;
import caseloader.credentials.SearchRequest;
import org.jsoup.nodes.Document;

public class TestSite implements WebSite {
    @Override
    public String url() {
        return null;
    }

    @Override
    public String downloadPage() {
        return null;
    }

    @Override
    public Credentials findCredentials(Document document, SearchRequest request, final Credentials credentials) {

        return null;
    }
}
