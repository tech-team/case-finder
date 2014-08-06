package caseloader.credentials.websites;

import caseloader.credentials.Credentials;
import caseloader.credentials.CredentialsSearchRequest;
import exceptions.DataRetrievingError;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import util.HttpDownloader;
import util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public abstract class WebSite implements Comparable<WebSite> {
    public abstract String url();
    public abstract Credentials findCredentials(final CredentialsSearchRequest request, final Credentials credentials) throws IOException, DataRetrievingError, InterruptedException;
    public abstract int getPriority();
    private static final double RELEVANCE_THRESHOLD = 0.5;

    @Override
    public int compareTo(WebSite site) {
        assert (site != null);
        return site.getPriority() - this.getPriority();
    }

    protected Document downloadPage(String url) throws IOException, DataRetrievingError, InterruptedException {
        String html = HttpDownloader.get(url);
        return Jsoup.parse(html, url);
    }

    protected Credentials findWithBestRelevance(Map<RelevanceInput, Double> relevances) {
        RelevanceInput best = null;
        double bestRelevance = RELEVANCE_THRESHOLD;
        for (Map.Entry<RelevanceInput, Double> entry : relevances.entrySet()) {
            double relevance = entry.getValue();
            if (best == null || relevance > bestRelevance) {
                best = entry.getKey();
                bestRelevance = relevance;
            }
        }

        return best == null ? null : best.getCredentials();
    }

    protected double countRelevance(RelevanceInput input) {

        String reqInn = input.getRequest().getInn();
        String reqOgrn = input.getRequest().getOgrn();
        String reqName = StringUtils.removeNonLetters(input.getRequest().getCompanyName());
        String reqAddress = StringUtils.removeNonLetters(input.getRequest().getAddress().getRaw());

        String foundName = StringUtils.removeNonLetters(input.getName());
        String foundAddress = StringUtils.removeNonLetters(input.getAddress());
        String foundInn = input.getCredentials().getInn();
        String foundOgrn = input.getCredentials().getOgrn();
        List<String> foundDirectors = input.getCredentials().getDirectors(url()).stream().map(StringUtils::removeNonLetters).collect(Collectors.toCollection(LinkedList::new));
        List<String> foundTelephones = input.getCredentials().getTelephones(url()).stream().map(StringUtils::removeNonLetters).collect(Collectors.toCollection(LinkedList::new));

        String globalInn = input.getTotalCreds().getInn();
        String globalOgrn = input.getTotalCreds().getOgrn();
        List<String> globalDirectors = input.getTotalCreds().getDirectors(url()).stream().map(StringUtils::removeNonLetters).collect(Collectors.toCollection(LinkedList::new));
        List<String> globalTelephones = input.getTotalCreds().getTelephones(url()).stream().map(StringUtils::removeNonLetters).collect(Collectors.toCollection(LinkedList::new));

        if (reqInn != null && reqInn.equals(foundInn)
                || reqOgrn != null && reqOgrn.equals(foundOgrn))
            return 1;

        double[] similarities = {
                similarity(reqName, foundName),
                similarity(reqAddress, foundAddress),
                similarity(globalDirectors, foundDirectors),
                similarity(globalTelephones, foundTelephones)
        };

        double[] weights = { 0.3, 0.2, 0.2, 0.3 }; // TODO: Still think it around

        return weightedAverage(similarities, weights);
    }

    private static double weightedAverage(double[] values, double[] weights) {
        if (values.length != weights.length)
            throw new IllegalArgumentException("Values length and weights length should be the same.");

        {
            // weights check
            double weightsSum = 0.0;
            for (double w : weights)
                weightsSum += w;
            double eps = 0.001;
            if (Math.abs(weightsSum - 1) >= eps)
                throw new IllegalArgumentException("Sum of the weights should be 1");
        }

        double sum = 0.0;
        for (int i = 0; i < values.length; ++i) {
            sum += weights[i] * values[i];
        }
        return sum / values.length;
    }

    private static double similarity(List<String> list1, List<String> list2) {

        return 0;
    }

    private static double similarity(String s1, String s2) {
        if (s1 == null || s2 == null)
            return 0.0;

        String[] tokens1 = s1.split("\\s+");
        String[] tokens2 = s2.split("\\s+");
        int minSize = Math.min(tokens1.length, tokens2.length);
        int maxSize = Math.max(tokens1.length, tokens2.length);

        // TODO: not sure
//        Arrays.sort(tokens1);
//        Arrays.sort(tokens2);

        int distanceThreshold = 3; // TODO: Why wouldn't it be 3?

        // TODO: can be optimized
        double[][] simMatrix = new double[tokens1.length][tokens2.length];

        // TODO
        for (int i = 0; i < tokens1.length; ++i) {
            String t1 = tokens1[i];
            for (int j = 0; j < tokens2.length; ++j) {
                String t2 = tokens2[j];
                double d = StringUtils.levensteinDistance(t1, t2);
                double maxLength = Math.max(t1.length(), t2.length());
                simMatrix[i][j] = 1.0 - d / maxLength;
            }
        }

        double avg = 0.0;
        if (tokens1.length > tokens2.length) {
            for (int i = 0; i < tokens1.length; ++i) {
                double max = 0.0;
                for (int j = 0; j < tokens2.length; ++j) {
                    double s = simMatrix[i][j];
                    if (s > max)
                        max = s;
                }
                avg += max;
            }
        } else {
            for (int j = 0; j < tokens2.length; ++j) {
                double max = 0.0;
                for (int i = 0; i < tokens1.length; ++i) {
                    double s = simMatrix[i][j];
                    if (s > max)
                        max = s;
                }
                avg += max;
            }
        }
        avg /= maxSize;

        return avg;
    }


    class RelevanceInput {
        private Credentials credentials;
        private String name;
        private String address;
        private final CredentialsSearchRequest request;
        private final Credentials totalCreds;

        public RelevanceInput(Credentials credentials, String name, String address, CredentialsSearchRequest request, Credentials totalCreds) {
            this.credentials = credentials;
            this.name = name;
            this.address = address;
            this.request = request;
            this.totalCreds = totalCreds;
        }

        public Credentials getCredentials() {
            return credentials;
        }

        public String getName() {
            return name;
        }

        public String getAddress() {
            return address;
        }

        public CredentialsSearchRequest getRequest() {
            return request;
        }

        public Credentials getTotalCreds() {
            return totalCreds;
        }
    }
}
