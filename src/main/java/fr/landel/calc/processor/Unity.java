package fr.landel.calc.processor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public enum Unity {
    NUMBER(Type.NUMBER, "d"),

    DATE_YEARS(Type.DATE, v -> v * Unity.NANO_PER_YEAR, v -> v / Unity.NANO_PER_YEAR, "Y"),
    DATE_YEARS_LEAP(Type.DATE, v -> v * Unity.NANO_PER_YEAR_LEAP, v -> v / Unity.NANO_PER_YEAR_LEAP, "YL"),
    DATE_MONTHS(Type.DATE, v -> v * Unity.NANO_PER_MONTH, v -> v / Unity.NANO_PER_MONTH, "M"),
    DATE_MONTHS_LEAP(Type.DATE, v -> v * Unity.NANO_PER_MONTH_LEAP, v -> v / Unity.NANO_PER_MONTH_LEAP, "ML"),
    DATE_WEEKS(Type.DATE, v -> v * Unity.NANO_PER_WEEK, v -> v / Unity.NANO_PER_WEEK, "W"),
    DATE_DAYS(Type.DATE, v -> v * Unity.NANO_PER_DAY, v -> v / Unity.NANO_PER_DAY, "D"),
    DATE_HOURS(Type.DATE, v -> v * Unity.NANO_PER_HOUR, v -> v / Unity.NANO_PER_HOUR, "h"),
    DATE_MINUTES(Type.DATE, v -> v * Unity.NANO_PER_MINUTE, v -> v / Unity.NANO_PER_MINUTE, "m"),
    DATE_SECONDS(Type.DATE, v -> v * Unity.NANO_PER_SECOND, v -> v / Unity.NANO_PER_SECOND, "s"),
    DATE_MILLISECONDS(Type.DATE, v -> v * Unity.NANO_PER_MILLISECOND, v -> v / Unity.NANO_PER_MILLISECOND, "S"),
    DATE_MICROSECONDS(Type.DATE, v -> v * Unity.NANO_PER_MICROSECOND, v -> v / Unity.NANO_PER_MICROSECOND, "O"),
    DATE_NANOSECONDS(Type.DATE, "N"),

    TEMP_KELVIN(Type.TEMPERATURE, "K"),
    TEMP_CELCIUS(Type.TEMPERATURE, v -> v + Unity.CELCIUS_ZERO_IN_KELVIN, v -> v - Unity.CELCIUS_ZERO_IN_KELVIN, "C"),
    TEMP_FARENHEIT(
            Type.TEMPERATURE,
            v -> (v - Unity.FAHRENHEIT_ZERO) / Unity.FAHRENHEIT_DEVIDER + Unity.CELCIUS_ZERO_IN_KELVIN,
            v -> (v - Unity.CELCIUS_ZERO_IN_KELVIN) * Unity.FAHRENHEIT_DEVIDER + Unity.FAHRENHEIT_ZERO,
            "F");

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

    private static final Map<String, Unity> BY_SYMBOL = Arrays.stream(Unity.values()).collect(HashMap::new, (a, b) -> Arrays.stream(b.symbols).forEach(i -> a.put(i, b)), (a, b) -> a.putAll(b));

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

    public Function<Double, Double> getFromUnity() {
        return this.fromUnity;
    }

    public Function<Double, Double> getToUnity() {
        return this.toUnity;
    }

    public static Optional<Unity> getUnity(final String unity) {
        if (unity == null) {
            return Optional.of(Unity.NUMBER);
        } else {
            return Optional.ofNullable(BY_SYMBOL.get(unity));
        }
    }

    static enum Type {
        NUMBER,
        DATE,
        TEMPERATURE;
    }
}
