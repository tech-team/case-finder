package caseloader.credentials;

import caseloader.ThreadPool;
import caseloader.credentials.websites.RusProfile;
import caseloader.credentials.websites.WebSite;
import exceptions.DataRetrievingError;
import util.MyLogger;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class CredentialsLoader {
    private List<WebSite> webSites;
    private ThreadPool pool = new ThreadPool();
    private Logger logger = MyLogger.getLogger(this.getClass().toString());

    public CredentialsLoader() {
        webSites = new LinkedList<>();
        webSites.add(new RusProfile());
    }

    public CredentialsLoader(List<WebSite> webSites) {
        this.webSites = webSites;
    }

    public Credentials retrieveCredentials(CredentialsSearchRequest request) throws InterruptedException {
        Credentials credentials = new Credentials();
        List<Future<Credentials>> founds = new LinkedList<>();

        for (WebSite webSite : webSites) {
            Future<Credentials> found =
                    pool.submit(new CredentialsWorker(webSite, request, credentials));
            founds.add(found);
        }

        for (Future<Credentials> found : founds) {
            try {
                credentials.merge(found.get());
            } catch (ExecutionException e) {
                e.printStackTrace();
                return null;
            }
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
            logger.info("Working on url: " + webSite.url());

            Credentials found = null;
            try {
                found = webSite.findCredentials(request, credentials);
            } catch (IOException | DataRetrievingError e) {
                throw new RuntimeException(e); // TODO: retrying
            } catch (InterruptedException e) {
                return null;
            }
            logger.info("Finished url: " + webSite.url());
            return found;
        }
    }


    public static void main(String[] args) {
//        CredentialsLoader credentialsLoader = new CredentialsLoader();
//        Credentials creds =
//                credentialsLoader.retrieveCredentials(new CredentialsSearchRequest("test", "test"));
//
//        ThreadPool.instance().waitForFinish();
    }
}
