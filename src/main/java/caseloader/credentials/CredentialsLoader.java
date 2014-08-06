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
    private PriorityQueue<WebSite> webSites;
    private ThreadPool pool = new ThreadPool();
    private Logger logger = MyLogger.getLogger(this.getClass().toString());
    private int count = 0;

    public CredentialsLoader() {
        webSites = new PriorityQueue<>();
        webSites.add(new RusProfile());
//        webSites.add(new ListOrg());
    }

    public Credentials retrieveCredentials(CredentialsSearchRequest request) throws InterruptedException {
        Credentials credentials = new Credentials();
        List<Future<Credentials>> founds = new LinkedList<>();


        for (WebSite webSite : webSites) {
            if (request.getInn() == null)
                request.setInn(credentials.getInn());
            if (request.getOgrn() == null)
                request.setOgrn(credentials.getOgrn());

            Future<Credentials> found =
                    pool.submit(new CredentialsWorker(webSite, request, credentials));
            try {
                credentials.merge(found.get());
                System.out.println(++count + ") " + request.getCompanyName() + " found creds. Inn = " + (found.get() == null ? null : found.get().getInn()));
            } catch (ExecutionException e) {
                e.printStackTrace();
                return null;
            }
//            founds.add(found);
        }

//        for (Future<Credentials> found : founds) {
//            try {
//                credentials.merge(found.get());
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//                return null;
//            }
//        }

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
