package fr.landel.calc.processor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import fr.landel.calc.config.I18n;
import fr.landel.calc.utils.DateUtils;
import fr.landel.calc.utils.MapUtils;
import fr.landel.calc.utils.StringUtils;

public enum Unity {

    VARIABLE(0, UnityType.VARIABLE),

    NUMBER(0, UnityType.NUMBER, "d"),

    DATE_YEAR(0, UnityType.DATE, DateUtils::toZeroNanosecond, DateUtils::fromZeroNanosecond, new int[] {1, 2, 3, 5, 6}, "y", "year"),
    DATE_YEARS(1, UnityType.DATE, v -> v * DateUtils.NANO_PER_YEAR, v -> v / DateUtils.NANO_PER_YEAR, "Y", "year"),
    DATE_YEARS_LEAP(2, UnityType.DATE, v -> v * DateUtils.NANO_PER_YEAR_LEAP, v -> v / DateUtils.NANO_PER_YEAR_LEAP, "YL", "yearLeap"),
    DATE_YEARS_AVG(3, UnityType.DATE, v -> v * DateUtils.NANO_PER_YEAR_AVG, v -> v / DateUtils.NANO_PER_YEAR_AVG, "YA", "yearAverage"),
    DATE_MONTHS(4, UnityType.DATE, v -> v * DateUtils.NANO_PER_MONTH, v -> v / DateUtils.NANO_PER_MONTH, new int[] {5, 6}, "M", "month"),
    DATE_MONTHS_AVG(5, UnityType.DATE, v -> v * DateUtils.NANO_PER_MONTH_AVG, v -> v / DateUtils.NANO_PER_MONTH_AVG, "MA", "monthAverage"),
    DATE_MONTHS_LEAP(6, UnityType.DATE, v -> v * DateUtils.NANO_PER_MONTH_LEAP, v -> v / DateUtils.NANO_PER_MONTH_LEAP, "ML", "monthLeap"),
    DATE_WEEKS(7, UnityType.DATE, v -> v * DateUtils.NANO_PER_WEEK, v -> v / DateUtils.NANO_PER_WEEK, "W", "week"),
    DATE_DAYS(8, UnityType.DATE, v -> v * DateUtils.NANO_PER_DAY, v -> v / DateUtils.NANO_PER_DAY, "D", "day"),
    DATE_HOURS(9, UnityType.DATE, v -> v * DateUtils.NANO_PER_HOUR, v -> v / DateUtils.NANO_PER_HOUR, "h", "hour"),
    DATE_MINUTES(10, UnityType.DATE, v -> v * DateUtils.NANO_PER_MINUTE, v -> v / DateUtils.NANO_PER_MINUTE, "i", "minute"),
    DATE_SECONDS(11, UnityType.DATE, v -> v * DateUtils.NANO_PER_SECOND, v -> v / DateUtils.NANO_PER_SECOND, "s", "second"),
    DATE_MILLISECONDS(12, UnityType.DATE, v -> v * DateUtils.NANO_PER_MILLISECOND, v -> v / DateUtils.NANO_PER_MILLISECOND, "S", "millisecond"),
    DATE_MICROSECONDS(13, UnityType.DATE, v -> v * DateUtils.NANO_PER_MICROSECOND, v -> v / DateUtils.NANO_PER_MICROSECOND, "O", "microsecond"),
    DATE_NANOSECONDS(14, UnityType.DATE, "N", "nanosecond"),

    TEMP_KELVIN(0, UnityType.TEMPERATURE, "K", "kelvin"),
    TEMP_CELSIUS(1, UnityType.TEMPERATURE, v -> v + Unity.CELSIUS_ZERO_IN_KELVIN, v -> v - Unity.CELSIUS_ZERO_IN_KELVIN, "C", "celsius"),
    TEMP_FARENHEIT(
            2,
            UnityType.TEMPERATURE,
            v -> (v - Unity.FAHRENHEIT_ZERO) / Unity.FAHRENHEIT_DEVIDER + Unity.CELSIUS_ZERO_IN_KELVIN,
            v -> (v - Unity.CELSIUS_ZERO_IN_KELVIN) * Unity.FAHRENHEIT_DEVIDER + Unity.FAHRENHEIT_ZERO,
            "F",
            "farhenheit"),
    TEMP_REAUMUR(
            3,
            UnityType.TEMPERATURE,
            v -> v / Unity.REAUMUR_DEVIDER + Unity.CELSIUS_ZERO_IN_KELVIN,
            v -> (v - Unity.CELSIUS_ZERO_IN_KELVIN) * Unity.REAUMUR_DEVIDER,
            "Re",
            "reaumur"),

    LENGTH_METER(0, UnityType.LENGTH, new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9}, "m", "meter"),
    LENGTH_IMPERIAL_LEAGUE(1, UnityType.LENGTH, v -> v * Unity.LEAGUE_M, v -> v / Unity.LEAGUE_M, "lea", "league"),
    LENGTH_IMPERIAL_MILE(2, UnityType.LENGTH, v -> v * Unity.MILE_M, v -> v / Unity.MILE_M, "mi", "mile"),
    LENGTH_IMPERIAL_FURLONG(3, UnityType.LENGTH, v -> v * Unity.FURLONG_M, v -> v / Unity.FURLONG_M, "fur", "furlong"),
    LENGTH_IMPERIAL_CHAIN(4, UnityType.LENGTH, v -> v * Unity.CHAIN_M, v -> v / Unity.CHAIN_M, "ch", "chain"),
    LENGTH_IMPERIAL_YARD(5, UnityType.LENGTH, v -> v * Unity.YARD_M, v -> v / Unity.YARD_M, "yd", "yard"),
    LENGTH_IMPERIAL_FOOT(6, UnityType.LENGTH, v -> v * Unity.FOOT_M, v -> v / Unity.FOOT_M, "ft", "foot"),
    LENGTH_IMPERIAL_INCH(7, UnityType.LENGTH, v -> v * Unity.INCH_M, v -> v / Unity.INCH_M, "in", "inch"),
    LENGTH_IMPERIAL_DIGIT(8, UnityType.LENGTH, v -> v * Unity.DIGIT_M, v -> v / Unity.DIGIT_M, "di", "digit"),
    LENGTH_IMPERIAL_THOU(9, UnityType.LENGTH, v -> v * Unity.THOU_M, v -> v / Unity.THOU_M, "th", "thou"),

    ;

    public static final Comparator<Unity> COMPARATOR_UNITIES = (a, b) -> Integer.compare(a.index, b.index);

    public static final double CELSIUS_ZERO_IN_KELVIN = 273.15;
    public static final double FAHRENHEIT_ZERO = 32;
    public static final double FAHRENHEIT_DEVIDER = 1.8;
    public static final double REAUMUR_DEVIDER = 0.8;

    public static final double FOOT_M = 1_200d / 3_937d;
    public static final double INCH_M = FOOT_M / 12;
    public static final double DIGIT_M = FOOT_M / 16;
    public static final double THOU_M = FOOT_M / 12_000;
    public static final double YARD_M = FOOT_M * 3;
    public static final double CHAIN_M = FOOT_M * 66;
    public static final double FURLONG_M = FOOT_M * 660;
    public static final double MILE_M = FOOT_M * 5_280;
    public static final double LEAGUE_M = FOOT_M * 15_840;

    static final SortedSet<Unity> DATES_AVG;
    static {
        final SortedSet<Unity> unities = new TreeSet<>(COMPARATOR_UNITIES);
        unities.addAll(Arrays.asList(DATE_YEARS_AVG, DATE_MONTHS_AVG, DATE_WEEKS, DATE_DAYS, DATE_HOURS, DATE_MINUTES, DATE_SECONDS,
                DATE_MILLISECONDS, DATE_MICROSECONDS, DATE_NANOSECONDS));
        DATES_AVG = Collections.unmodifiableSortedSet(unities);
    }

    static final SortedMap<Unity, Double> UNITIES_DATE;
    static {
        final SortedMap<Unity, Double> map = new TreeMap<>();
        map.put(DATE_YEARS, DateUtils.NANO_PER_YEAR);
        map.put(DATE_YEARS_AVG, DateUtils.NANO_PER_YEAR_AVG);
        map.put(DATE_YEARS_LEAP, DateUtils.NANO_PER_YEAR_LEAP);
        map.put(DATE_MONTHS, DateUtils.NANO_PER_MONTH);
        map.put(DATE_MONTHS_AVG, DateUtils.NANO_PER_MONTH_AVG);
        map.put(DATE_MONTHS_LEAP, DateUtils.NANO_PER_MONTH_LEAP);
        map.put(DATE_WEEKS, DateUtils.NANO_PER_WEEK);
        map.put(DATE_DAYS, DateUtils.NANO_PER_DAY);
        map.put(DATE_HOURS, DateUtils.NANO_PER_HOUR);
        map.put(DATE_MINUTES, DateUtils.NANO_PER_MINUTE);
        map.put(DATE_SECONDS, DateUtils.NANO_PER_SECOND);
        map.put(DATE_MILLISECONDS, DateUtils.NANO_PER_MILLISECOND);
        map.put(DATE_MICROSECONDS, DateUtils.NANO_PER_MICROSECOND);
        map.put(DATE_NANOSECONDS, 1d);
        UNITIES_DATE = Collections.unmodifiableSortedMap(map);
    }

    private static final String ERROR_BOUNDS_SUB_SECONDS = Arrays.asList(DATE_MILLISECONDS, DATE_MICROSECONDS, DATE_NANOSECONDS).stream()
            .map(String::valueOf).collect(Collectors.joining("' or '"));

    static final BinaryOperator<Unity> REDUCER = (a, b) -> {
        if (COMPARATOR_UNITIES.compare(a, b) > -1) {
            return a;
        } else {
            return b;
        }
    };

    public static final Map<Unity, Unity> NEXT_UNITY;
    public static final Map<UnityType, Map<Integer, Unity>> INDEX_BY_TYPE;
    public static final Map<Unity, List<Unity>> INCOMPATIBLE_UNITIES;
    static {
        final Map<Unity, Unity> nextUnity = new HashMap<>();
        Unity previous = null;
        for (Unity unity : Unity.values()) {
            if (previous != null && unity.getType().equals(previous.getType())) {
                nextUnity.put(previous, unity);
            }
            previous = unity;
        }
        NEXT_UNITY = Collections.unmodifiableMap(nextUnity);

        final Map<UnityType, Map<Integer, Unity>> indexByType = new HashMap<>();
        for (Unity unity : Unity.values()) {
            Map<Integer, Unity> unities = MapUtils.getOrPutIfAbsent(indexByType, unity.type, HashMap::new);
            unities.put(unity.index, unity);
        }
        INDEX_BY_TYPE = Collections.unmodifiableMap(indexByType);

        final Map<Unity, List<Unity>> incompatibleUnities = new HashMap<>();
        for (Unity unity : Unity.values()) {
            if (unity.incompatibleUnities.length > 0) {
                List<Unity> unities = MapUtils.getOrPutIfAbsent(incompatibleUnities, unity, ArrayList::new);
                Map<Integer, Unity> unitiesByIndex = INDEX_BY_TYPE.get(unity.type);
                for (int incompatibleUnityIndex : unity.incompatibleUnities) {
                    Unity incompatibleUnity = unitiesByIndex.get(incompatibleUnityIndex);
                    unities.add(incompatibleUnity);
                    MapUtils.getOrPutIfAbsent(incompatibleUnities, incompatibleUnity, ArrayList::new).add(unity);
                }
            }
        }
        INCOMPATIBLE_UNITIES = Collections.unmodifiableMap(incompatibleUnities);
    }

    private final UnityType type;
    private final int index;
    private final String[] symbols;
    private final Function<Double, Double> fromUnity;
    private final Function<Double, Double> toUnity;
    private final int[] incompatibleUnities;

    private Unity(final int index, final UnityType type, final Function<Double, Double> fromUnity, final Function<Double, Double> toUnity,
            final int[] incompatibleUnities, final String... symbols) {
        this.index = index;
        this.type = type;
        this.fromUnity = fromUnity;
        this.toUnity = toUnity;
        this.incompatibleUnities = incompatibleUnities;
        this.symbols = symbols;
        Arrays.sort(this.symbols, StringUtils.COMPARATOR_LENGTH_DESC);
        this.type.add(this);
    }

    private Unity(final int index, final UnityType type, final Function<Double, Double> fromUnity, final Function<Double, Double> toUnity,
            final String... symbols) {
        this(index, type, fromUnity, toUnity, new int[0], symbols);
    }

    private Unity(final int index, final UnityType type, final int[] incompatibleUnities, final String... symbols) {
        this(index, type, Function.identity(), Function.identity(), incompatibleUnities, symbols);
    }

    private Unity(final int index, final UnityType type, final String... symbols) {
        this(index, type, Function.identity(), Function.identity(), symbols);
    }

    public UnityType getType() {
        return this.type;
    }

    public Double fromUnity(final Double value) {
        return this.fromUnity.apply(value);
    }

    public Double toUnity(final Double value) {
        return this.toUnity.apply(value);
    }

    public String longestSymbol() {
        return this.symbols[0];
    }

    public String shortestSymbol() {
        return this.symbols[this.symbols.length - 1];
    }

    public String getSymbol(final boolean abbrev) {
        if (abbrev) {
            return this.shortestSymbol();
        } else {
            return this.longestSymbol();
        }
    }

    public String[] getSymbols() {
        return Arrays.copyOf(this.symbols, this.symbols.length);
    }

    public Optional<Unity> next() {
        return Optional.ofNullable(NEXT_UNITY.get(this));
    }

    public static SortedSet<Unity> merge(final SortedSet<Unity> left, final SortedSet<Unity> right) {
        final SortedSet<Unity> unities = new TreeSet<>(Unity.COMPARATOR_UNITIES);
        unities.addAll(left);
        unities.addAll(right);
        return unities;
    }

    public static SortedSet<Unity> getUnities(final String text) throws ProcessorException {
        return getUnities(text, null);
    }

    public static SortedSet<Unity> getUnities(final String input, final UnityType requiredType) throws ProcessorException {
        final SortedSet<Unity> unities = new TreeSet<>(COMPARATOR_UNITIES);

        if (input == null || input.isEmpty()) {
            return unities;
        }

        String text = input;
        char[] data = input.toCharArray();

        Optional<Unity> unityOptional;
        Unity unity;
        UnityType type = null;

        while ((unityOptional = UnityTree.check(data, 0, requiredType)).isPresent()) {
            unity = unityOptional.get();
            for (String s : unity.getSymbols()) {
                if (text.startsWith(s)) {
                    if (type != null && !unity.getType().equals(type)) {
                        throw new ProcessorException(I18n.ERROR_UNITY_TYPE, input, type, unity.getType());
                    }
                    text = text.substring(s.length());
                    data = text.toCharArray();
                    unities.add(unity);
                    type = unity.getType();
                    break;
                }
            }
        }

        if (data.length > 0) {
            if (requiredType == null) {
                throw new ProcessorException(I18n.ERROR_UNITY_PARSE, text);
            } else {
                throw new ProcessorException(I18n.ERROR_UNITY_PARSE_TYPE, text, requiredType);
            }
        }

        return unities;
    }

    public static Duration mapToDuration(final SortedMap<Unity, Double> inputs, final SortedSet<Unity> unities) throws ProcessorException {
        Double amount = 0d;

        for (Entry<Unity, Double> input : inputs.entrySet()) {
            Unity unity = input.getKey();
            int value = input.getValue().intValue();

            unities.add(unity);
            amount += value * UNITIES_DATE.get(unity);
        }

        return Duration.of(amount.longValue(), ChronoUnit.NANOS);
    }

    public static LocalDateTime mapToLocalDateTime(final SortedMap<Unity, Double> inputs, final SortedSet<Unity> unities) throws ProcessorException {
        int year = 0, month = 1, dayOfMonth = 1, hour = 0, minute = 0, second = 0, nanoOfSecond = 0;

        for (Entry<Unity, Double> input : inputs.entrySet()) {
            Unity unity = input.getKey();
            int value = input.getValue().intValue();

            unities.add(unity);

            switch (unity) {
            case DATE_YEAR:
                year = value;
                break;
            case DATE_MONTHS:
                if (value > 0 && value < 13) {
                    month = value;
                } else {
                    throw new ProcessorException(I18n.ERROR_UNITY_BOUNDS, unity, value);
                }
                break;
            case DATE_DAYS:
                if (value > 0 && value < 32) {
                    dayOfMonth = value;
                } else {
                    throw new ProcessorException(I18n.ERROR_UNITY_BOUNDS, unity, value);
                }
                break;
            case DATE_HOURS:
                if (value > -1 && value < 24) {
                    hour = value;
                } else {
                    throw new ProcessorException(I18n.ERROR_UNITY_BOUNDS, unity, value);
                }
                break;
            case DATE_MINUTES:
                if (value > -1 && value < 60) {
                    minute = value;
                } else {
                    throw new ProcessorException(I18n.ERROR_UNITY_BOUNDS, unity, value);
                }
                break;
            case DATE_MILLISECONDS:
                nanoOfSecond += Double.valueOf(value * DateUtils.NANO_PER_MILLISECOND).intValue();
                break;
            case DATE_MICROSECONDS:
                nanoOfSecond += Double.valueOf(value * DateUtils.NANO_PER_MICROSECOND).intValue();
                break;
            case DATE_NANOSECONDS:
                nanoOfSecond += value;
                break;
            default:
            }
        }

        if (nanoOfSecond < 0 && nanoOfSecond > DateUtils.NANO_PER_SECOND) {
            throw new ProcessorException(I18n.ERROR_UNITY_BOUNDS, ERROR_BOUNDS_SUB_SECONDS, nanoOfSecond);
        }

        return LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond);
    }
}
