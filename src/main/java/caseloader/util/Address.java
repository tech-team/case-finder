package caseloader.util;

import org.apache.commons.lang3.StringUtils;
import util.ResourceControl;

import java.util.ResourceBundle;

public class Address {
    private final String rawAddress;
    private String city;
    private String region;

    public static Address parse(String address) {
        return new Address(address);
    }

    private Address(String address) {
        rawAddress = address;

        parseCity();
        parseRegion();
    }

    private void parseCity() {
        ResourceBundle cityToRegion = ResourceBundle.getBundle("properties.cityToRegionDB", new ResourceControl("UTF-8"));

        for (String city: cityToRegion.keySet()) {
            if (StringUtils.containsIgnoreCase(rawAddress, city)) {
                this.city = city;
                this.region = cityToRegion.getString(city);
                return;
            }
        }
    }

    private void parseRegion() {
        if (region != null) //already recognised in getCity
            return;

        ResourceBundle regions = ResourceBundle.getBundle("properties.regionsDB", new ResourceControl("UTF-8"));

        for (String region: regions.keySet()) {
            if (StringUtils.containsIgnoreCase(rawAddress, region)) {
                this.region = region;
                return;
            }
        }
    }

    public String getCity() {
        return city;
    }

    public String getRegion() {
        return region;
    }

    public String getRaw() {
        return rawAddress;
    }
}
