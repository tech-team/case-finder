package caseloader.credentials;

import caseloader.ThreadPool;
import caseloader.credentials.websites.Kartoteka;
import caseloader.credentials.websites.ListOrg;
import caseloader.credentials.websites.RusProfile;
import caseloader.credentials.websites.WebSite;
import exceptions.DataRetrievingError;
import util.MyLogger;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class CredentialsLoader {
    private class WebSiteBulk {
        private List<WebSite> sites;

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

    private List<WebSiteBulk> webSites;
    private ThreadPool pool = new ThreadPool();
    private Logger logger = MyLogger.getLogger(this.getClass().toString());
    private int count = 0;

    public CredentialsLoader() {
        webSites = new LinkedList<>();
        webSites.add(new WebSiteBulk().addSite(new RusProfile()));
        webSites.add(new WebSiteBulk().addSite(new Kartoteka()).addSite(new RusProfile()));
    }

    public Credentials retrieveCredentials(CredentialsSearchRequest request) throws InterruptedException {
        Credentials credentials = new Credentials();
        boolean foundAnything = false;

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
                foundAnything = true;
                System.out.println(++count + ") " + request.getCompanyName() + " found creds. Inn = " + found.getInn() + ". tels count = " + found.getTelephones().size());
                break;
            }
        }

        if (!foundAnything) {
            System.out.println("CREDENTIALS ARE NULL");
        }

        return credentials;
    }

    public void stopExecution() {
        pool.stopExecution();
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
        public Credentials call() {
            for (int retry = 1; retry <= 3; ++retry) {
                logger.info("Working on company: " + request.getCompanyName() + ". url: " + webSite.url());

                Credentials found = null;
                try {
                    found = webSite.findCredentials(request, credentials);
                } catch (IOException | DataRetrievingError e) {
                    logger.warning("Error retrieving credentials. Retrying");
                    continue;
                } catch (InterruptedException e) {
                    return null;
                }
                logger.info("Finished company: " + request.getCompanyName() + ". url: " + webSite.url());
                return found;
            }
            logger.severe("Couldn't retrieve credentials. Breaking");
            return null;
        }
    }


    public static void main(String[] args) throws InterruptedException {
        CredentialsLoader credentialsLoader = new CredentialsLoader();
        Credentials creds =
                credentialsLoader.retrieveCredentials(new CredentialsSearchRequest("ОАО Гамма Траст", "test"));
    }
}
