package caseloader.util;

import util.ConfigReader;
import util.ConfigReaderIOException;
import util.ConfigReaderParseException;

import java.util.List;
import java.util.stream.Collectors;

public class RegionHelper {
    private static final ConfigReader cityToRegion;
    private static final ConfigReader regionToID;

    static {
        try {
            cityToRegion = new ConfigReader("/properties/cityToRegionDB.properties", "UTF-8");
            regionToID = new ConfigReader("/properties/regionsDB.properties", "UTF-8");
        } catch (ConfigReaderIOException | ConfigReaderParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> regionsByCity(String city) {
        return cityToRegion.getList(city);
    }

    public static List<String> regionsIdsByCity(String city) {
        List<String> regions = regionsByCity(city);

        return regions.stream()
                .map(regionToID::getString)
                .collect(Collectors.toList());
    }
}
