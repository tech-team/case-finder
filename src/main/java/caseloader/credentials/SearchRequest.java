package caseloader.credentials;

public class SearchRequest {
    private String companyName = null;
    private String address = null;
    private String inn = null;
    private String ogrn = null;

    public SearchRequest(String companyName, String address, String inn, String ogrn) {
        this.companyName = companyName;
        this.address = address;
        this.inn = inn;
        this.ogrn = ogrn;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getAddress() {
        return address;
    }

    public String getInn() {
        return inn;
    }

    public String getOgrn() {
        return ogrn;
    }
}
