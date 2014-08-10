package caseloader.credentials;

import caseloader.credentials.websites.Kartoteka;
import caseloader.credentials.websites.RusProfile;
import caseloader.credentials.websites.WebSite;
import util.net.MalformedUrlException;
import util.MyLogger;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

public class CredentialsLoader {
    private class WebSiteBulk {
        private final List<WebSite> sites;

        public WebSiteBulk() {
            sites = new LinkedList<>();
        }

        public WebSiteBulk addSite(WebSite site) {
            sites.add(site);
            return this;
        }

        public List<WebSite> getSites() {
            return sites;
        }
    }

    private final List<WebSiteBulk> webSites;
    private final Logger logger = MyLogger.getLogger(this.getClass().toString());

    public CredentialsLoader() {
        webSites = new LinkedList<>();
        webSites.add(new WebSiteBulk().addSite(new RusProfile()));
        webSites.add(new WebSiteBulk().addSite(new Kartoteka()).addSite(new RusProfile()));
    }

    public Credentials retrieveCredentials(CredentialsSearchRequest request) throws InterruptedException {
        Credentials credentials = new Credentials();

        for (WebSiteBulk bulk : webSites) {
            Credentials found = null;

            for (WebSite site : bulk.getSites()) {
                if (request.getInn() == null || request.getInn().equals(""))
                    request.setInn(credentials.getInn());
                if (request.getOgrn() == null || request.getOgrn().equals(""))
                    request.setOgrn(credentials.getOgrn());

                found = new CredentialsWorker(site, request, credentials).call();
                credentials.merge(found);
            }

            if (found != null) {
                break;
            }
        }

        return credentials;
    }

    private class CredentialsWorker implements Callable<Credentials> {
        private final WebSite webSite;
        private final CredentialsSearchRequest request;
        private final Credentials credentials;

        public CredentialsWorker(WebSite webSite, CredentialsSearchRequest request, Credentials credentials) {
            this.webSite = webSite;
            this.request = request;
            this.credentials = credentials;
        }

        @Override
        public Credentials call() throws InterruptedException {
            int maxRetries = 3;
            for (int retry = 1; retry <= maxRetries + 1; ++retry) {
                logger.info("Working on company: " + request.getCompanyName() + ". url: " + webSite.url());

                Credentials found;
                try {
                    found = webSite.findCredentials(request, credentials);
                } catch (MalformedUrlException e) {
                    if (retry <= maxRetries) {
                        Thread.sleep(50);
                        logger.warning("Error retrieving credentials. Retry #" + retry);
                        continue;
                    } else {
                        break;
                    }
                }
                logger.info("Finished company: " + request.getCompanyName() + ". url: " + webSite.url());
                return found;
            }
            logger.severe("Couldn't retrieve credentials. Breaking");
            return null;
        }
    }


    @SuppressWarnings("UnusedDeclaration")
    public static void main(String[] args) throws InterruptedException {
        CredentialsLoader credentialsLoader = new CredentialsLoader();
        Credentials creds =
                credentialsLoader.retrieveCredentials(new CredentialsSearchRequest("ОАО Гамма Траст", "test"));
    }
}
