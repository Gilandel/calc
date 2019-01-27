package fr.landel.calc.utils;

import org.junit.jupiter.api.Test;

/**
 * Test {@link DateUtils}
 *
 * @since Jan 27, 2019
 * @author Gilles
 *
 */
public class DateUtilsTest {

    @Test
    public void testConvert() {
        // https://en.wikipedia.org/wiki/Proleptic_Julian_calendar
        // https://en.wikipedia.org/wiki/Proleptic_Gregorian_calendar
        // https://en.wikipedia.org/wiki/ISO_8601
        // https://en.wikipedia.org/wiki/Astronomical_year_numbering

        // System.out.printf("%.6f%n", toZeroNanosecond(0) / (24 * 60 * 60 * 1_000_000_000d));
        // System.out.printf("%.6f%n", toZeroNanosecond(-4) / (24 * 60 * 60 * 1_000_000_000d));
        // System.out.printf("%.6f%n", toZeroNanosecond(4) / (24 * 60 * 60 * 1_000_000_000d));
        System.out.printf("%.6f%n", DateUtils.toZeroNanosecond(2018) / (24 * 60 * 60 * 1_000_000_000d) / 365);
    }
}
