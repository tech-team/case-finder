package caseloader.credentials;

import caseloader.util.Address;

public class CredentialsSearchRequest {
    private String companyName = null;
    private Address address = null;
    private String inn = null;
    private String ogrn = null;

    public CredentialsSearchRequest(String companyName, String address, String inn, String ogrn) {
        this.companyName = companyName;
        this.address = Address.parse(address);
        this.inn = inn;
        this.ogrn = ogrn;
    }

    public CredentialsSearchRequest(String companyName, String address) {
        this.companyName = companyName;
        this.address = Address.parse(address);
    }

    public String getCompanyName() {
        return companyName;
    }

    public Address getAddress() {
        return address;
    }

    public String getInn() {
        return inn;
    }

    public String getOgrn() {
        return ogrn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public void setOgrn(String ogrn) {
        this.ogrn = ogrn;
    }
}
