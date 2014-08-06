package caseloader.util;

import util.ResourceControl;

import java.util.ResourceBundle;

public class RegionHelper {
    private static ResourceBundle cityToRegion = ResourceBundle.getBundle("properties.cityToRegion", new ResourceControl("UTF-8"));
    private static ResourceBundle regionToID = ResourceBundle.getBundle("properties.regionsDB", new ResourceControl("UTF-8"));

    public static String regionByCity(String city) {
        return cityToRegion.getString(city);
    }

    public static String regionIdByCity(String city) {
        String region = regionByCity(city);
        return regionToID.getString(region);
    }
}
