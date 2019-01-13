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
    NUMBER(false, v -> stringify(v.getValue())),
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
        double v = value;
        double intermediate;
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
                if (builder.length() > 0) {
                    builder.append(StringUtils.SPACE);
                }
                builder.append(intermediate).append(StringUtils.SPACE).append(unity.firstSymbol());
            }
            ++i;
        }
    }

    private static String stringify(final double d) {
        double r = MathUtils.round(d, MainProcessor.getPrecision());

        final String value = Double.toString(r);
        final int dot = value.indexOf('.');
        int length = dot + 1 + MainProcessor.getPrecision();
        final String result;

        if (length > value.length()) {
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