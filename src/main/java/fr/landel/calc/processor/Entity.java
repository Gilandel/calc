package fr.landel.calc.processor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.landel.calc.utils.Logger;

public class Entity {

    private static final Logger LOGGER = new Logger(Entity.class);

    private static final Pattern PATTERN_NUMBER = Pattern.compile("([+-]?[0-9]+(:?\\.[0-9]+)?([Ee][+-]?[0-9]+)?)([a-zA-Z]+)?");
    private static final int GROUP_NUMBER_INTEGER = 1;
    private static final int GROUP_NUMBER_DECIMAL = 2;
    private static final int GROUP_NUMBER_EXPONENT = 3;
    private static final int GROUP_NUMBER_UNITY = 4;

    private static final Pattern PATTERN_DATE = Pattern.compile("([+-]?[0-9]+(:?\\.[0-9]+)?)([YMWDhmsSIN])");

    private static final String ERROR_PARSE = "the following number cannot be parsed: {}";
    private static final String ERROR_MALFORMED = "the following number is malformed: {}";
    private static final String ERROR_UNITY = "the following unity is unknown: {}";

    private final int index;
    private int indexBis = -1;
    private Double value;
    private Unity unity;

    public Entity(final int index, final String input) throws ProcessorException {
        this.index = index;
        parse(input);
    }

    public Entity(final int index, final int indexBis, final Double value, final Unity unity) {
        this.index = index;
        this.indexBis = indexBis;
        this.value = value;
        this.unity = unity;
    }

    public Entity(final int index, final int indexBis, final Double value) {
        this(index, indexBis, value, Unity.NUMBER);
    }

    private void parse(final String input) throws ProcessorException {
        final Matcher matcher = PATTERN_NUMBER.matcher(input);
        while (matcher.find()) {
            if (this.unity == null || Unity.Type.DATE.equals(this.unity.getType())) {
                try {
                    Double value = Double.parseDouble(matcher.group(GROUP_NUMBER_INTEGER));

                    this.unity = Unity.getUnity(matcher.group(GROUP_NUMBER_UNITY)).orElseThrow(() -> new ProcessorException(ERROR_UNITY, matcher.group(GROUP_NUMBER_UNITY)));

                    value = this.unity.getFromUnity().apply(value);

                    if (this.value == null) {
                        this.value = value;
                    } else {
                        this.value += value;
                    }
                } catch (NumberFormatException e) {
                    LOGGER.error(e, ERROR_PARSE, input);
                    throw new ProcessorException(e, ERROR_PARSE, input);
                }
            } else {
                throw new ProcessorException(ERROR_MALFORMED, input);
            }
        }
    }

    public Double getValue() {
        return this.value;
    }

    public Unity getUnity() {
        return this.unity;
    }

    public int getIndex() {
        return this.index;
    }

    public int getIndexBis() {
        return this.indexBis;
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }
}
