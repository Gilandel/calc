package fr.landel.calc.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

import fr.landel.calc.utils.StringUtils;

public enum Unity {

    // TODO creer arbre unite comme fonction pour retrouner l'unite qui
    // correspond le mieux

    NUMBER(Type.NUMBER, "d"),

    DATE_YEARS(Type.DATE, v -> v * Unity.NANO_PER_YEAR, v -> v / Unity.NANO_PER_YEAR, "Y", "year"),
    DATE_YEARS_LEAP(Type.DATE, v -> v * Unity.NANO_PER_YEAR_LEAP, v -> v / Unity.NANO_PER_YEAR_LEAP, "YL", "yearleap"),
    DATE_MONTHS(Type.DATE, v -> v * Unity.NANO_PER_MONTH, v -> v / Unity.NANO_PER_MONTH, "M", "month"),
    DATE_MONTHS_LEAP(Type.DATE, v -> v * Unity.NANO_PER_MONTH_LEAP, v -> v / Unity.NANO_PER_MONTH_LEAP, "ML", "monthleap"),
    DATE_WEEKS(Type.DATE, v -> v * Unity.NANO_PER_WEEK, v -> v / Unity.NANO_PER_WEEK, "W", "week"),
    DATE_DAYS(Type.DATE, v -> v * Unity.NANO_PER_DAY, v -> v / Unity.NANO_PER_DAY, "D", "day"),
    DATE_HOURS(Type.DATE, v -> v * Unity.NANO_PER_HOUR, v -> v / Unity.NANO_PER_HOUR, "h", "hour"),
    DATE_MINUTES(Type.DATE, v -> v * Unity.NANO_PER_MINUTE, v -> v / Unity.NANO_PER_MINUTE, "i", "minute"),
    DATE_SECONDS(Type.DATE, v -> v * Unity.NANO_PER_SECOND, v -> v / Unity.NANO_PER_SECOND, "s", "second"),
    DATE_MILLISECONDS(Type.DATE, v -> v * Unity.NANO_PER_MILLISECOND, v -> v / Unity.NANO_PER_MILLISECOND, "S", "millisecond"),
    DATE_MICROSECONDS(Type.DATE, v -> v * Unity.NANO_PER_MICROSECOND, v -> v / Unity.NANO_PER_MICROSECOND, "O", "microsecond"),
    DATE_NANOSECONDS(Type.DATE, "N", "nanosecond"),

    TEMP_KELVIN(Type.TEMPERATURE, "K", "kelvin"),
    TEMP_CELCIUS(Type.TEMPERATURE, v -> v + Unity.CELCIUS_ZERO_IN_KELVIN, v -> v - Unity.CELCIUS_ZERO_IN_KELVIN, "C", "celcius"),
    TEMP_FARENHEIT(
            Type.TEMPERATURE,
            v -> (v - Unity.FAHRENHEIT_ZERO) / Unity.FAHRENHEIT_DEVIDER + Unity.CELCIUS_ZERO_IN_KELVIN,
            v -> (v - Unity.CELCIUS_ZERO_IN_KELVIN) * Unity.FAHRENHEIT_DEVIDER + Unity.FAHRENHEIT_ZERO,
            "F",
            "farhenheit"),

    LENGTH_METER(Type.LENGTH, "m", "meter"),
    LENGTH_IMPERIAL_LEAGUE(Type.LENGTH, v -> v * Unity.LEAGUE_M, v -> v / Unity.LEAGUE_M, "lea", "league"),
    LENGTH_IMPERIAL_MILE(Type.LENGTH, v -> v * Unity.MILE_M, v -> v / Unity.MILE_M, "mi", "mile"),
    LENGTH_IMPERIAL_FURLONG(Type.LENGTH, v -> v * Unity.FURLONG_M, v -> v / Unity.FURLONG_M, "fur", "furlong"),
    LENGTH_IMPERIAL_CHAIN(Type.LENGTH, v -> v * Unity.CHAIN_M, v -> v / Unity.CHAIN_M, "ch", "chain"),
    LENGTH_IMPERIAL_YARD(Type.LENGTH, v -> v * Unity.YARD_M, v -> v / Unity.YARD_M, "yd", "yard"),
    LENGTH_IMPERIAL_FOOT(Type.LENGTH, v -> v * Unity.FOOT_M, v -> v / Unity.FOOT_M, "ft", "foot"),
    LENGTH_IMPERIAL_INCH(Type.LENGTH, v -> v * Unity.INCH_M, v -> v / Unity.INCH_M, "in", "inch"),
    LENGTH_IMPERIAL_DIGIT(Type.LENGTH, v -> v * Unity.DIGIT_M, v -> v / Unity.DIGIT_M, "di", "digit"),
    LENGTH_IMPERIAL_THOU(Type.LENGTH, v -> v * Unity.THOU_M, v -> v / Unity.THOU_M, "th", "thou");

    public static final List<Unity> DATES = new ArrayList<>(
            Arrays.asList(DATE_YEARS, DATE_MONTHS, DATE_DAYS, DATE_HOURS, DATE_MINUTES, DATE_SECONDS, DATE_MILLISECONDS, DATE_MICROSECONDS, DATE_NANOSECONDS));
    public static final List<Unity> DATES_LEAP = new ArrayList<>(
            Arrays.asList(DATE_YEARS_LEAP, DATE_MONTHS_LEAP, DATE_DAYS, DATE_HOURS, DATE_MINUTES, DATE_SECONDS, DATE_MILLISECONDS, DATE_MICROSECONDS, DATE_NANOSECONDS));

    public static final Comparator<Unity> COMPARATOR_UNITIES = (a, b) -> Integer.compare(a.ordinal(), b.ordinal());

    private static final double NANO_PER_MICROSECOND = 1_000;
    private static final double NANO_PER_MILLISECOND = NANO_PER_MICROSECOND * 1_000;
    private static final double NANO_PER_SECOND = NANO_PER_MILLISECOND * 1_000;
    private static final double NANO_PER_MINUTE = NANO_PER_SECOND * 60;
    private static final double NANO_PER_HOUR = NANO_PER_MINUTE * 60;
    private static final double NANO_PER_DAY = NANO_PER_HOUR * 24;
    private static final double NANO_PER_WEEK = NANO_PER_DAY * 7;
    private static final double NANO_PER_MONTH = 365 / 12 * NANO_PER_DAY;
    private static final double NANO_PER_MONTH_LEAP = 366 / 12 * NANO_PER_DAY;
    private static final double NANO_PER_YEAR = 365 * NANO_PER_DAY;
    private static final double NANO_PER_YEAR_LEAP = 366 * NANO_PER_DAY;

    private static final double CELCIUS_ZERO_IN_KELVIN = 273.15;
    private static final double FAHRENHEIT_ZERO = 32;
    private static final double FAHRENHEIT_DEVIDER = 1.8;

    private static final double FOOT_M = 1_200d / 3_937d;
    private static final double INCH_M = FOOT_M / 12;
    private static final double DIGIT_M = FOOT_M / 16;
    private static final double THOU_M = FOOT_M / 12_000;
    private static final double YARD_M = FOOT_M * 3;
    private static final double CHAIN_M = FOOT_M * 66;
    private static final double FURLONG_M = FOOT_M * 660;
    private static final double MILE_M = FOOT_M * 5_280;
    private static final double LEAGUE_M = FOOT_M * 15_840;

    private static final Supplier<SortedSet<String>> SUPPLIER_SORTED_SET = () -> new TreeSet<>((a, b) -> {
        int c = Integer.compare(b.length(), a.length());
        if (c != 0) {
            return c;
        } else {
            return a.compareTo(b);
        }
    });
    private static final SortedSet<String> SYMBOLS = Arrays.stream(Unity.values()).collect(SUPPLIER_SORTED_SET, (a, b) -> a.addAll(Arrays.asList(b.symbols)), (a, b) -> a.addAll(b));
    private static final Map<String, Unity> BY_SYMBOL = Arrays.stream(Unity.values()).collect(HashMap::new, (a, b) -> Arrays.stream(b.symbols).forEach(i -> a.put(i, b)), (a, b) -> a.putAll(b));

    private static final SortedMap<Unity, Double> UNITIES;
    static {
        final SortedMap<Unity, Double> map = new TreeMap<>();
        map.put(Unity.DATE_YEARS, NANO_PER_YEAR);
        map.put(Unity.DATE_YEARS_LEAP, NANO_PER_YEAR_LEAP);
        map.put(Unity.DATE_MONTHS, NANO_PER_MONTH);
        map.put(Unity.DATE_MONTHS_LEAP, NANO_PER_MONTH_LEAP);
        map.put(Unity.DATE_WEEKS, NANO_PER_WEEK);
        map.put(Unity.DATE_DAYS, NANO_PER_DAY);
        map.put(Unity.DATE_HOURS, NANO_PER_HOUR);
        map.put(Unity.DATE_MINUTES, NANO_PER_MINUTE);
        map.put(Unity.DATE_SECONDS, NANO_PER_SECOND);
        map.put(Unity.DATE_MILLISECONDS, NANO_PER_MILLISECOND);
        map.put(Unity.DATE_MICROSECONDS, NANO_PER_MICROSECOND);
        UNITIES = Collections.unmodifiableSortedMap(map);
    }

    private static final BinaryOperator<Unity> REDUCER = (a, b) -> {
        if (Math.min(a.ordinal(), b.ordinal()) > -1) {
            return b;
        } else {
            return a;
        }
    };

    private final Type type;
    private final String[] symbols;
    private final Function<Double, Double> fromUnity;
    private final Function<Double, Double> toUnity;

    private Unity(final Type type, final Function<Double, Double> fromUnity, final Function<Double, Double> toUnity, final String... symbols) {
        this.type = type;
        this.symbols = symbols;
        this.fromUnity = fromUnity;
        this.toUnity = toUnity;
    }

    private Unity(final Type type, final String... unities) {
        this(type, Function.identity(), Function.identity(), unities);
    }

    public Type getType() {
        return this.type;
    }

    public Double fromUnity(final Double value) {
        return this.fromUnity.apply(value);
    }

    public Double toUnity(final Double value) {
        return this.toUnity.apply(value);
    }

    public String firstSymbol() {
        return this.symbols[0];
    }

    public static SortedSet<Unity> getUnities(final String unity) {
        final SortedSet<Unity> list = new TreeSet<>(COMPARATOR_UNITIES);
        if (unity == null) {
            list.add(Unity.NUMBER);
        } else {
            String text = unity;
            boolean notFound = false;
            while (!notFound && !text.isEmpty()) {
                notFound = true;
                // cut symbol list by number of characters <=
                for (String symbol : SYMBOLS) {
                    if (text.startsWith(symbol)) {
                        list.add(BY_SYMBOL.get(symbol));
                        if (text.length() > symbol.length()) {
                            text = text.substring(symbol.length());
                        } else {
                            text = StringUtils.EMPTY;
                            break;
                        }
                        notFound = false;
                    }
                }
            }
        }
        return list;
    }

    public static Unity min(final SortedSet<Unity> left, final SortedSet<Unity> right) {
        return REDUCER.apply(left.last(), right.last());
    }

    static enum Type {
        NUMBER(false, v -> Double.toString(v.getValue())),
        DATE(true, v -> {
            final StringBuilder builder = new StringBuilder();
            if (!v.isUnity()) {
                appendDate(builder, v.getValue(), v.getUnities());
                return builder.toString();
            } else {
                return builder.append(v.firstUnity().firstSymbol()).toString();
            }
        }),
        TEMPERATURE(false, v -> {
            final StringBuilder builder = new StringBuilder();
            if (!v.isUnity()) {
                builder.append(Double.toString(v.toUnity())).append(StringUtils.SPACE);
            }
            return builder.append(v.firstUnity().firstSymbol()).toString();
        }),
        LENGTH(true, v -> {
            final StringBuilder builder = new StringBuilder();
            if (!v.isUnity()) {
                builder.append(Double.toString(v.toUnity())).append(StringUtils.SPACE);
            }
            return builder.append(v.firstUnity().firstSymbol()).toString();
        });

        private final boolean accumulable;
        private final Function<Entity, String> formatter;

        private Type(final boolean accumulable, final Function<Entity, String> formatter) {
            this.accumulable = accumulable;
            this.formatter = formatter;
        }

        public boolean isAccumulable() {
            return this.accumulable;
        }

        public Function<Entity, String> getFormatter() {
            return this.formatter;
        }

        public String format(final Entity entity) {
            return this.formatter.apply(entity);
        }

        private static void appendDate(final StringBuilder builder, final double value, final SortedSet<Unity> unities) {
            double v = value;
            double intermediate;
            Double u;
            int i = 0;
            for (Unity unity : unities) {
                u = UNITIES.get(unity);
                if (u != null && v > u) {
                    if (i < unities.size() - 1) {
                        intermediate = Math.floor(v / u);
                        v -= intermediate * u;
                    } else {
                        intermediate = v / u;
                    }
                    builder.append(Double.valueOf(intermediate).longValue()).append(unity.firstSymbol());
                }
                ++i;
            }
        }
    }
}
