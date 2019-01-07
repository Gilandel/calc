package fr.landel.calc.processor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.landel.calc.utils.Logger;

public class Entity {

    private static final Logger LOGGER = new Logger(Entity.class);

    private static final Pattern PATTERN_NUMBER = Pattern.compile("([+-]?(:?[0-9]+(:?\\.[0-9]+)?|\\.[0-9]+)([Ee][+-]?[0-9]+)?)([a-zA-Z]+)?");
    private static final int GROUP_NUMBER_INTEGER = 1;
    // private static final int GROUP_NUMBER_DOUBLE = 2;
    // private static final int GROUP_NUMBER_DECIMAL = 3;
    // private static final int GROUP_NUMBER_EXPONENT = 4;
    private static final int GROUP_NUMBER_UNITY = 5;

    private static final Pattern PATTERN_UNITY = Pattern.compile("[a-zA-Z]+");

    private static final String ERROR_PARSE = "the following number cannot be parsed: {}";
    private static final String ERROR_MALFORMED = "the following number is malformed: {}";
    private static final String ERROR_UNITY = "the following unity is unknown: {}";

    private final int index;
    private Double value;
    private Unity unity;

    public Entity(final int index, final String input) throws ProcessorException {
        this.index = index;
        parse(input);
    }

    public Entity(final int index, final Double value, final Unity unity) {
        this.index = index;
        this.value = value;
        this.unity = unity;
    }

    public Entity(final int index, final Double value) {
        this(index, value, Unity.NUMBER);
    }

    private void parse(final String input) throws ProcessorException {
        Double value;
        Unity unity;
        int index, indexLeap;

        final Matcher matcher = PATTERN_NUMBER.matcher(input);
        while (matcher.find()) {
            if (this.unity == null || Unity.Type.DATE.equals(this.getUnityType())) {
                try {
                    value = Double.parseDouble(matcher.group(GROUP_NUMBER_INTEGER));

                    final String unityGroup = matcher.group(GROUP_NUMBER_UNITY);
                    unity = Unity.getUnity(unityGroup).orElseThrow(() -> new ProcessorException(ERROR_UNITY, unityGroup));

                    if (this.unity != null) {
                        boolean isNumber = Unity.Type.NUMBER.equals(unity.getType());
                        index = Unity.DATES.indexOf(this.unity);
                        indexLeap = Unity.DATES_LEAP.indexOf(this.unity);

                        if (isNumber && index > -1 && index < Unity.DATES.size() - 1) {
                            unity = Unity.DATES.get(index + 1);

                        } else if (isNumber && indexLeap > -1 && indexLeap < Unity.DATES.size() - 1) {
                            unity = Unity.DATES_LEAP.get(indexLeap + 1);

                        } else if (isNumber || !Unity.Type.DATE.equals(unity.getType())) {
                            throw new ProcessorException(ERROR_MALFORMED, input);
                        }
                    }

                    if (unity != null) {
                        this.unity = unity;
                    }

                    value = this.unity.fromUnity(value);

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

        if (this.unity == null) {
            if (PATTERN_UNITY.matcher(input).matches()) {
                this.unity = Unity.getUnity(input).orElseThrow(() -> new ProcessorException(ERROR_UNITY, input));
            } else {
                LOGGER.error(ERROR_PARSE, input);
                throw new ProcessorException(ERROR_PARSE, input);
            }
        }
    }

    public Double getValue() {
        return this.value;
    }

    public boolean isNumber() {
        return this.value != null && Unity.NUMBER.equals(this.getUnity());
    }

    public boolean isUnity() {
        return this.value == null;
    }

    public Unity getUnity() {
        return this.unity;
    }

    public Entity setUnity(final Unity unity) {
        this.unity = unity;
        return this;
    }

    public Unity.Type getUnityType() {
        return this.getUnity().getType();
    }

    public Double toUnity() {
        return this.getUnity().toUnity(this.value);
    }

    public int getIndex() {
        return this.index;
    }

    @Override
    public String toString() {
        return this.getUnityType().format(this);
    }
}
