package caseloader.util;

import org.junit.Test;

import java.util.Arrays;

public class RegionHelperTest {

    @Test
    public void testRegionsByCity() throws Exception {
        assert RegionHelper.regionsByCity("Москва").get(0).equals("Москва");
        assert RegionHelper.regionsByCity("Мирный").size() == 2;
        assert RegionHelper.regionsByCity("Старый Оскол").get(0).equals("Белгородская область");
    }

    @Test
    public void testRegionsIdsByCity() throws Exception {
        assert !RegionHelper.regionsIdsByCity("Мирный")
                .retainAll(Arrays.asList("29", "14"));
    }
}