package fr.landel.calc.processor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.function.Function;

import fr.landel.calc.utils.MathUtils;
import fr.landel.calc.utils.StringUtils;

public enum UnityType {
    // TODO manage output conf (exact, scientific, precision) remove
    // stringify
    VARIABLE(false, v -> v.getVariable()),
    NUMBER(false, v -> stringify(v.getValue())),
    DATE(true, v -> {
        final StringBuilder builder = new StringBuilder();
        if (!v.isUnity()) {
            appendDate(builder, v.getValue(), v.getUnities());
        } else {
            builder.append(v.firstUnity().getSymbol(MainProcessor.isUnityAbbrev()));
        }
        return builder.toString();
    }),
    TEMPERATURE(false, v -> {
        final StringBuilder builder = new StringBuilder();
        if (!v.isUnity()) {
            builder.append(stringify(v.toUnity())).append(StringUtils.SPACE);
        }
        return builder.append(v.firstUnity().getSymbol(MainProcessor.isUnityAbbrev())).toString();
    }),
    LENGTH(true, v -> {
        final StringBuilder builder = new StringBuilder();
        if (!v.isUnity()) {
            builder.append(stringify(v.toUnity())).append(StringUtils.SPACE);
        }
        return builder.append(v.firstUnity().getSymbol(MainProcessor.isUnityAbbrev())).toString();
    });

    private final boolean accumulable;
    private final Function<Entity, String> formatter;
    private final Map<String, Unity> unities = new HashMap<>();

    private UnityType(final boolean accumulable, final Function<Entity, String> formatter) {
        this.accumulable = accumulable;
        this.formatter = formatter;
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

    private static void appendDate(final StringBuilder builder, final double value, final SortedSet<Unity> unities) {
        final int nanosPrecision = 9;
        if (!MathUtils.isEqualOrGreater(value, 0d, nanosPrecision)) {
            builder.append('-');
        }
        final int size = unities.size();
        double v = Math.abs(value);
        boolean appended = false;
        Double intermediate = null;
        Double u;
        int i = 0;
        for (Unity unity : unities) {
            u = Unity.UNITIES.get(unity);
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

        if (intermediate == null && !Unity.DATES_AVG.equals(unities)) {
            appendDate(builder, value, Unity.DATES_AVG);
        }
    }

    private static String stringify(final double input) {
        return stringify(input, false);
    }

    private static String stringify(final double input, boolean intermediate) {
        double rounded = MathUtils.round(input, MainProcessor.getPrecision());

        final String value = Double.toString(rounded);
        final int dot = value.indexOf('.');
        final int exp = Math.max(value.indexOf('E'), value.indexOf('e'));
        if (exp > -1) {
            final int nbExp = Integer.parseInt(value.substring(exp + 1));
            char[] chars = new char[value.length() + nbExp];
            Arrays.fill(chars, '0');

            // FIXME manage 8.4E6
        }
        int length = dot + 1 + MainProcessor.getPrecision();
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
}