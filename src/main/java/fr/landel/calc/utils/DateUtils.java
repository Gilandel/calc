package fr.landel.calc.utils;

import java.time.LocalDateTime;
import java.util.function.Predicate;

public final class DateUtils {

    public static final double NANO_PER_MICROSECOND = 1_000;
    public static final double NANO_PER_MILLISECOND = NANO_PER_MICROSECOND * 1_000;
    public static final double NANO_PER_SECOND = NANO_PER_MILLISECOND * 1_000;
    public static final double NANO_PER_MINUTE = NANO_PER_SECOND * 60;
    public static final double NANO_PER_HOUR = NANO_PER_MINUTE * 60;
    public static final double NANO_PER_DAY = NANO_PER_HOUR * 24;
    public static final double NANO_PER_WEEK = NANO_PER_DAY * 7;
    public static final double NANO_PER_MONTH = 365 / 12 * NANO_PER_DAY;
    public static final double NANO_PER_MONTH_LEAP = 366 / 12 * NANO_PER_DAY;
    public static final double NANO_PER_YEAR = 365 * NANO_PER_DAY;
    public static final double NANO_PER_YEAR_LEAP = 366 * NANO_PER_DAY;
    public static final double NANO_PER_YEAR_AVG = 365.2425;
    public static final double NANO_1970 = toZeroNanosecond(1970);

    private static final Predicate<Integer> IS_LEAP_YEAR = year -> (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);

    private static final int FEBRUARY_28 = 31 + 28;

    private DateUtils() {
        throw new UnsupportedOperationException();
    }

    public static double fromZeroNanosecond(final Double date) {
        int leap = 0;
        int year = 0;
        for (int i = 0; NANO_PER_YEAR * (i - leap) + NANO_PER_YEAR_LEAP * leap < date; i = nextLeapYear(i)) {
            ++leap;
            year = i;
        }
        double diff = date - (NANO_PER_YEAR * (year - leap) + NANO_PER_YEAR_LEAP * leap);
        if (diff > NANO_PER_YEAR) {
            return year + Math.round(diff / NANO_PER_YEAR);
        } else {
            return year;
        }
    }

    public static double toZeroNanosecond(final LocalDateTime date) {
        final int year = date.getYear();
        final int day = date.getDayOfYear();

        double time = toZeroNanosecond(year);

        time += day * NANO_PER_DAY;

        if (IS_LEAP_YEAR.test(year) && day > FEBRUARY_28) {
            time += NANO_PER_DAY;
        }

        time += date.getHour() * NANO_PER_HOUR;
        time += date.getMinute() * NANO_PER_MINUTE;
        time += date.getSecond() * NANO_PER_SECOND;
        time += date.getNano();

        return time;
    }

    public static double toZeroNanosecond(final Double year) {
        return toZeroNanosecond(year.intValue());
    }

    public static double toZeroNanosecond(final int year) {
        int leap = 0;
        for (int i = 0; i < year; i = nextLeapYear(i)) {
            ++leap;
        }

        return NANO_PER_YEAR * (year - leap) + NANO_PER_YEAR_LEAP * leap;
    }

    private static int nextLeapYear(final int leapYear) {
        final int next = leapYear + 4;
        if (next % 100 != 0 || next % 400 == 0) {
            return next;
        }
        return nextLeapYear(next);
    }

    public static void main(String[] args) {
        System.out.printf("%.6f", toZeroNanosecond(1000_000) / (24 * 60 * 60 * 1_000_000_000_000_000d));
    }
}
