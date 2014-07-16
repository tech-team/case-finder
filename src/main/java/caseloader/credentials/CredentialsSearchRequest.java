package caseloader.credentials;

public class CredentialsSearchRequest {
    private String companyName = null;
    private String address = null;
    private String inn = null;
    private String ogrn = null;

    public CredentialsSearchRequest(String companyName, String address, String inn, String ogrn) {
        this.companyName = companyName;
        this.address = address;
        this.inn = inn;
        this.ogrn = ogrn;
    }

    public CredentialsSearchRequest(String companyName, String address) {
        this.companyName = companyName;
        this.address = address;
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
