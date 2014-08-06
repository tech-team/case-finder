package caseloader.util;

public class Address {
    private String rawAddress;
    private String city;

    public static Address parse(String address) {
        Address a = new Address();
        a.rawAddress = address;

        // TODO

        return a;
    }

    public String getCity() {
        return city;
    }

    public String getRaw() {
        return rawAddress;
    }
}
