package caseloader.util;

import org.junit.Test;

public class AddressTest {

    @Test
    public void testParse() throws Exception {
        String[] rawStrings = {
                "117049, ул. Житная, 16, г. Москва, Московская область",
                "150000, Россия, г. Ярославль, Ярославская область, ул. Революционная, д. 34 В",
                "692904, Россия, г. Находка, Приморский край, ул. Портовая,17",
                "684000, Россия, г. Елизово, Камчатский край, ул. Ленина, 26",
                "683031, Россия, Петропавловск-Камчатский, Камчатский край, пр. К. Маркса, 1/1",
                "МОСКВА г, БАСМАННАЯ НОВ. ул, д.2",
                "620026, СВЕРДЛОВСКАЯ обл, ЕКАТЕРИНБУРГ г, НАРОДНОЙ ВОЛИ ул, д.69, кв.КВАРТИРА 1",
                "620028, СВЕРДЛОВСКАЯ обл, ЕКАТЕРИНБУРГ г, ФРОЛОВА ул, д.29, кв.ОФ. 7"
        };

        for (String rawAddress: rawStrings) {
            Address address = Address.parse(rawAddress);

            assert address.getCity() != null : "City not recognised";
            assert address.getRegion() != null : "Region not recognised";
        }
    }
}