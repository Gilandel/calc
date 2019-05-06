package fr.landel.calc.processor;

import java.time.LocalDateTime;
import java.time.temporal.TemporalField;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.function.Function;

import fr.landel.calc.utils.DateUtils;
import fr.landel.calc.utils.MathUtils;
import fr.landel.calc.utils.StringUtils;

public enum UnityType {
    // TODO manage output conf (exact, scientific, precision)

    VARIABLE(false, v -> v.getVariable()),
    NUMBER(false, v -> stringify(v.getValue())),

    DATE(true, v -> {
        final StringBuilder builder = new StringBuilder();
        if (!v.isUnity()) {
            appendDate(builder, v);
        } else {
            builder.append(v.firstUnity().getSymbol(MainProcessor.isUnityAbbrev()));
        }
        return builder.toString();
    }),
    TEMPERATURE(false),
    LENGTH(true),
    DATA(true),
    VOLUME(true);

    public static final List<UnityType> TYPED_UNITIES = Arrays.asList(DATE, TEMPERATURE, LENGTH);

    private final boolean accumulable;
    private final Function<Entity, String> formatter;
    private final Map<String, Unity> unities = new HashMap<>();

    private UnityType(final boolean accumulable, final Function<Entity, String> formatter) {
        this.accumulable = accumulable;
        this.formatter = formatter;
    }

    private UnityType(final boolean accumulable) {
        this(accumulable, v -> {
            final StringBuilder builder = new StringBuilder();
            if (!v.isUnity()) {
                builder.append(stringify(v.toUnity()));
                if (MainProcessor.isUnitiesSpace()) {
                    builder.append(StringUtils.SPACE);
                }
            }
            return builder.append(v.firstUnity().getSymbol(MainProcessor.isUnityAbbrev())).toString();
        });
    }

    public boolean isAccumulable() {
        return this.accumulable;
    }

    void add(final Unity unity) {
        for (String symbol : unity.getSymbols()) {
            this.unities.put(symbol, unity);
        }
    }

    public Map<String, Unity> getUnities() {
        return this.unities;
    }

    public Function<Entity, String> getFormatter() {
        return this.formatter;
    }

    public String format(final Entity entity) {
        return this.formatter.apply(entity);
    }

    private static void appendDate(final StringBuilder builder, final Entity entity) {
        if (entity.isDate() && entity.getUnities().contains(Unity.DATE_YEAR)) {
            final LocalDateTime date = entity.getDate().get();

            int value;
            TemporalField field;
            Unity previous = null;
            for (Unity unity : entity.getUnities()) {

                field = Unity.UNITIES_DATE_TEMPORAL.get(unity);
                if (field != null) {

                    if (previous != null && MainProcessor.isValuesSpace()) {
                        builder.append(StringUtils.SPACE);
                    }

                    previous = unity;

                    value = date.get(field);
                    if (Unity.DATE_MICROSECONDS.equals(unity)) {
                        builder.append(value % DateUtils.NANO_PER_MILLISECOND);

                    } else if (Unity.DATE_NANOSECONDS.equals(unity)) {
                        builder.append(value % DateUtils.NANO_PER_MICROSECOND);

                    } else {
                        builder.append(value);
                    }

                    if (MainProcessor.isUnitiesSpace()) {
                        builder.append(StringUtils.SPACE);
                    }

                    builder.append(unity.getSymbol(MainProcessor.isUnityAbbrev()));
                }
            }
        } else {
            append(builder, entity.getValue(), entity.getUnities(), Unity.UNITIES_DATE, Unity.DATES_AVG, 9);
        }
    }

    private static void append(final StringBuilder builder, final double value, final SortedSet<Unity> unities,
            final SortedMap<Unity, Double> valuesByUnity, final SortedSet<Unity> sortedUnities, final int maxPrecision) {

        if (!MathUtils.isEqualOrGreater(value, 0d, maxPrecision)) {
            builder.append('-');
        }
        final int size = unities.size();
        double v = Math.abs(value);
        boolean appended = false;
        Double intermediate = null;
        Double u;
        int i = 0;
        for (Unity unity : unities) {
            u = valuesByUnity.get(unity);
            if (u != null && v > u) {
                if (i < unities.size() - 1) {
                    intermediate = Math.floor(v / u);
                    v -= intermediate * u;
                } else {
                    intermediate = v / u;
                }
                if (appended && MainProcessor.isValuesSpace()) {
                    builder.append(StringUtils.SPACE);
                }
                builder.append(stringify(intermediate, i < size));
                if (MainProcessor.isUnitiesSpace()) {
                    builder.append(StringUtils.SPACE);
                }
                builder.append(unity.getSymbol(MainProcessor.isUnityAbbrev()));
                appended = true;
            }
            ++i;
        }

        if (intermediate == null && !sortedUnities.equals(unities)) {
            append(builder, value, sortedUnities, valuesByUnity, sortedUnities, maxPrecision);
        }
    }

    private static String stringify(final double input) {
        return stringify(input, false);
    }

    private static String stringify(final double input, boolean intermediate) {
        double rounded = MathUtils.round(input, MainProcessor.getPrecision());

        final String value = removeExponent(Double.toString(rounded));
        final int dot = value.indexOf('.');
        final int length = dot + 1 + MainProcessor.getPrecision();
        final String result;

        if (intermediate && !value.substring(dot + 1).chars().anyMatch(v -> v != '0')) {
            result = value.substring(0, dot);

        } else if (length > value.length()) {
            char[] chars = new char[length - value.length()];
            Arrays.fill(chars, '0');
            result = value + new String(chars);

        } else if (MainProcessor.getPrecision() > 0) {
            result = value.substring(0, length);

        } else if (dot > -1) {
            result = value.substring(0, dot);

        } else {
            result = value;
        }

        return result;
    }

    private static String removeExponent(String input) {

        final int exp = Math.max(input.indexOf('E'), input.indexOf('e'));

        if (exp > -1) {
            int dot = input.indexOf('.');

            if (dot < 0) {
                dot = exp;
            }

            final boolean positive = input.indexOf('-') != 0;
            final int nbExp = Integer.parseInt(input.substring(exp + 1));
            final boolean posExp = nbExp > 0;

            char[] in = input.toCharArray();
            char[] out;

            if (posExp) { // prepare array
                out = new char[in.length + nbExp];
            } else {
                out = new char[in.length + -1 * nbExp];
            }

            Arrays.fill(out, '0');

            int pos = 0;

            if (posExp) { // fill digits in front of dot
                System.arraycopy(in, 0, out, pos, dot);
            } else {
                pos = -1 * nbExp + 2;
                if (positive) {
                    System.arraycopy(in, 0, out, pos - 1, dot);
                } else {
                    System.arraycopy(in, 1, out, pos, dot - 1);
                }
            }

            if (exp > dot) { // fill digits after dot
                if (posExp) {
                    pos = Math.min(exp - dot - 1, nbExp);
                    System.arraycopy(in, dot + 1, out, dot, pos);
                } else {
                    pos += dot - 1;
                    System.arraycopy(in, dot + 1, out, pos, exp - dot - 1);
                }
            }

            if (posExp && exp - dot > nbExp) { // append digit after dot
                pos = dot + pos + 1;
                System.arraycopy(in, pos, out, nbExp + dot + 1, exp - pos);
            }

            if (posExp) { // set dot
                out[nbExp + dot] = '.';
            } else if (positive) {
                out[1] = '.';
            } else {
                out[0] = '-';
                out[2] = '.';
            }

            return new String(out);
        }
        return input;
    }
}