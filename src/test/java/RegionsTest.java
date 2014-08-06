import org.junit.Test;
import util.ResourceControl;

import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class RegionsTest {
    @Test
    public void test() {
        ResourceBundle regionsDB = ResourceBundle.getBundle("properties.regionsDB", new ResourceControl("UTF-8"));
        ResourceBundle cityToRegionDB = ResourceBundle.getBundle("properties.cityToRegionDB", new ResourceControl("UTF-8"));

        Set<String> regions1 = regionsDB.keySet();
        Set<String> cities = cityToRegionDB.keySet();

        Set<String> regions2 = new HashSet<>();

        //test ->
        for(String city: cities) {
            String region = cityToRegionDB.getString(city);
            regions2.add(region);

            assert regionsDB.containsKey(region)
                    : "Region not found in regionsDB: " + region;
        }

        //test <-
        assert regions1.size() == regions2.size()
                : "region sets' sizes not equal";

        regions1.retainAll(regions2);
        assert regions1.size() == regions2.size()
                : "region sets' sizes not equal after intersection";
    }
}
