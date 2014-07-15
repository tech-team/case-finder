package caseloader.credentials.websites;

import caseloader.credentials.Credentials;
import caseloader.credentials.SearchRequest;
import org.jsoup.nodes.Document;

public interface WebSite {
    String url();
    String downloadPage();
    Credentials findCredentials(Document document, SearchRequest request, final Credentials credentials);
}
