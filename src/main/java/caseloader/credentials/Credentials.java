package caseloader.credentials;

import java.util.HashSet;
import java.util.Set;

public class Credentials {
    private Set<String> directors = new HashSet<>();
    private Set<String> telephones = new HashSet<>();
    private String inn = null;
    private String ogrn = null;

    public void addDirector(String director) {
        directors.add(director);
    }

    public void addTelephone(String telephone) {
        telephones.add(telephone);
    }

    public boolean hasDirector() {
        return directors.size() != 0;
    }

    public boolean hasTelephone() {
        return telephones.size() != 0;
    }

    public boolean hasInn() {
        return inn != null;
    }

    public String getInn() {
        return inn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public boolean hasOgrn() {
        return ogrn != null;
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
