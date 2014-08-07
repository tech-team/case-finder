package caseloader.credentials;

import java.util.*;
import java.util.stream.Collectors;

public class Credentials {
    private Map<String, List<String>> directors = new HashMap<>();
    private Map<String, List<String>> telephones = new HashMap<>();
    private String inn = null;
    private String ogrn = null;
    private String link = null;

    public void addDirector(String website, String director) {
        if (director != null) {
            List<String> directors = this.directors.get(website);
            if (directors == null) {
                this.directors.put(website, new LinkedList<>());
            }
            this.directors.get(website).add(director);
        }
    }

    public void addTelephone(String website, String telephone) {
        if (telephone != null) {
            List<String> telephones = this.telephones.get(website);
            if (telephones == null) {
                this.telephones.put(website, new LinkedList<>());
            }
            this.telephones.get(website).add(telephone);
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

    public Map<String, List<String>> getDirectors() {
        return directors;
    }

    public List<String> getDirectors(String website) {
        return directors.get(website);
    }

    public List<String> getAllDirectors() {
        List<String> allDirectors = new LinkedList<>();
        for (Map.Entry<String, List<String>> directors : this.directors.entrySet()) {
            allDirectors.addAll(directors.getValue());
        }
        return allDirectors;
    }

    public Map<String, List<String>> getTelephones() {
        return telephones;
    }

    public List<String> getAllTelephones() {
        List<String> allTelephones = new LinkedList<>();
        for (Map.Entry<String, List<String>> telephones : this.telephones.entrySet()) {
            allTelephones.addAll(telephones.getValue());
        }
        return allTelephones;
    }

    public List<String> getTelephones(String website) {
        return telephones.get(website);
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void merge(final Credentials found) {
        if (found != null) {
            for (Map.Entry<String, List<String>> directors : found.directors.entrySet()) {
                for (String d : directors.getValue()) {
                    addDirector(directors.getKey(), d);
                }
            }
            for (Map.Entry<String, List<String>> telephones : found.telephones.entrySet()) {
                for (String t : telephones.getValue()) {
                    addTelephone(telephones.getKey(), t);
                }
            }

            if (!hasInn()) {
                setInn(found.getInn());
            }

            if (!hasOgrn()) {
                setOgrn(found.getOgrn());
            }
        }
    }
}
