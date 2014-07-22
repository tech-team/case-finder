package caseloader.credentials;

import caseloader.credentials.websites.RusProfile;
import caseloader.credentials.websites.WebSite;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CredentialsLoader {
    private List<WebSite> webSites;
    private static final int WAIT_TIMEOUT = 5 * 60;

    private ExecutorService getExecutor() {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    }

    public CredentialsLoader() {
        webSites = new LinkedList<>();
        webSites.add(new RusProfile());
    }

    public CredentialsLoader(List<WebSite> webSites) {
        this.webSites = webSites;
    }

    public Credentials retrieveCredentials(CredentialsSearchRequest request) {
        Credentials credentials = new Credentials();

        ExecutorService executor = getExecutor();

        for (WebSite webSite : webSites) {
            executor.execute(new CredentialsWorker(webSite, request, credentials));
        }

        executor.shutdown();

        while (!executor.isTerminated()) {
        }
        try {
            executor.awaitTermination(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return credentials;
    }


    private class CredentialsWorker implements Runnable {
        private final WebSite webSite;
        private final CredentialsSearchRequest request;
        private final Credentials credentials;

        public CredentialsWorker(WebSite webSite, CredentialsSearchRequest request, Credentials credentials) {
            this.webSite = webSite;
            this.request = request;
            this.credentials = credentials;
        }

        @Override
        public void run() {
            System.out.println("[" + Thread.currentThread().getName() + "] Working on: " + webSite.url());
            Credentials found = webSite.findCredentials(request, credentials);
            if (found != null) {
                synchronized (credentials) {
                    credentials.merge(found);
                }
            }
            System.out.println("[" + Thread.currentThread().getName() + "] Finished: " + webSite.url());
        }
    }
}
