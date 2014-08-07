package caseloader.credentials;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Credentials {
    private class Pair {
        private String website;
        private String value;

        private Pair(String website, String value) {
            this.website = website;
            this.value = value;
        }

        public String getWebsite() {
            return website;
        }

        public String getValue() {
            return value;
        }
    }

    private List<Pair> directors = new LinkedList<>();
    private List<Pair> telephones = new LinkedList<>();
    private String inn = null;
    private String ogrn = null;

    public void addDirector(String website, String director) {
        if (director != null) {
            directors.add(new Pair(website, director));
        }
    }

    public void addTelephone(String website, String telephone) {
        if (telephone != null) {
            telephones.add(new Pair(website, telephone));
        }
    }

    public void addTelephones(String website, String[] telephones) {
        if (telephones != null) {
            for (String t : telephones) {
                addTelephone(website, t);
            }
        }
    }

    public boolean hasDirector() {
        return directors.size() != 0;
    }

    public boolean hasTelephone() {
        return telephones.size() != 0;
    }

    public boolean hasInn() {
        return inn != null && !inn.equals("");
    }

    public String getInn() {
        return inn;
    }

    public void setInn(String inn) {
        if (inn != null && inn.length() >= 3) {
            this.inn = inn;
        }
    }

    public boolean hasOgrn() {
        return ogrn != null && !ogrn.equals("");
    }

    public String getOgrn() {
        return ogrn;
    }

    public void setOgrn(String ogrn) {
        if (ogrn != null && ogrn.length() >= 3) {
            this.ogrn = ogrn;
        }
    }

    public List<String> getDirectors(String website) {
        List<String> websiteDirectors
                            = directors.stream()
                                       .filter(d -> d.getWebsite().equals(website))
                                       .map(Pair::getValue)
                                       .collect(Collectors.toCollection(LinkedList::new));
//        String[] res = new String[websiteDirectors.size()];
//        return websiteDirectors.toArray(res);
        return websiteDirectors;
    }

    public List<Pair> getTelephones() {
        return telephones;
    }

    public List<String> getTelephones(String website) {
        List<String> websiteTelephones
                = telephones.stream()
                .filter(d -> d.getWebsite().equals(website))
                .map(Pair::getValue)
                .collect(Collectors.toCollection(LinkedList::new));
//        String[] res = new String[websiteTelephones.size()];
//        return websiteTelephones.toArray(res);
        return websiteTelephones;
    }

    public void merge(final Credentials found) {
        if (found != null) {
            directors.addAll(found.directors);
            telephones.addAll(found.telephones);

            if (!hasInn()) {
                setInn(found.getInn());
            }

            if (!hasOgrn()) {
                setOgrn(found.getOgrn());
            }
        }
    }
}
