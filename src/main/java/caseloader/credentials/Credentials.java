package caseloader.credentials;

import java.util.LinkedList;
import java.util.List;

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
        directors.add(new Pair(website, director));
    }

    public void addTelephone(String website, String telephone) {
        telephones.add(new Pair(website, telephone));
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
        this.inn = inn;
    }

    public boolean hasOgrn() {
        return ogrn != null && !ogrn.equals("");
    }

    public String getOgrn() {
        return ogrn;
    }

    public void setOgrn(String ogrn) {
        this.ogrn = ogrn;
    }

    public void merge(final Credentials found) {
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
