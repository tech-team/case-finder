package caseloader.credentials.websites;

import caseloader.credentials.Credentials;
import caseloader.credentials.CredentialsSearchRequest;
import org.jsoup.nodes.Document;

public class TestSite extends WebSite {
    @Override
    public String url() {
        return null;
    }

    @Override
    public Credentials findCredentials(final CredentialsSearchRequest request, final Credentials credentials) {
        Document page = downloadPage();
        return null;
    }

}
