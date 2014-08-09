package caseloader.credentials.websites;

import caseloader.credentials.Credentials;
import caseloader.credentials.CredentialsSearchRequest;
import exceptions.DataRetrievingError;
import util.StringUtils;

import java.util.Map;

public abstract class WebSite implements Comparable<WebSite> {
    public abstract String url();
    public abstract Credentials findCredentials(final CredentialsSearchRequest request, final Credentials credentials) throws DataRetrievingError, InterruptedException;
    public abstract int getPriority();
    private static final double RELEVANCE_THRESHOLD = 0.5;

    @Override
    @SuppressWarnings("NullableProblems")
    public int compareTo(WebSite site) {
        assert (site != null);
        return site.getPriority() - this.getPriority();
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

        if (reqInn != null && reqInn.equals(foundInn)
                || reqOgrn != null && reqOgrn.equals(foundOgrn))
            return 1;

        double[] similarities = {
                similarity(reqName, foundName),
                similarity(reqAddress, foundAddress)
        };

        double[] weights = { 0.6, 0.4 }; // TODO: Still think it around

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

    public static double similarity(String s1, String s2) {
        if (s1 == null || s2 == null)
            return 0.0;

        String[] tokens1 = s1.split("\\s+");
        String[] tokens2 = s2.split("\\s+");
        int maxSize = Math.max(tokens1.length, tokens2.length);


        // TODO: can be optimized
        double[][] simMatrix = new double[tokens1.length][tokens2.length];

        for (int i = 0; i < tokens1.length; ++i) {
            for (int j = 0; j < tokens2.length; ++j) {
                double d = StringUtils.levensteinDistance(tokens1[i], tokens2[j]);
                simMatrix[i][j] = 0.5 / (d + 0.5);
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

        public RelevanceInput(Credentials credentials, String name, String address, CredentialsSearchRequest request) {
            this.credentials = credentials;
            this.name = name;
            this.address = address;
            this.request = request;
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
    }
}
