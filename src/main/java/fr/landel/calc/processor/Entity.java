package fr.landel.calc.processor;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;
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
    private SortedSet<Unity> unities = new TreeSet<>(Unity.COMPARATOR_UNITIES);

    public Entity(final int index, final String input) throws ProcessorException {
        this.index = index;
        parse(input);
    }

    public Entity(final int index, final Double value, final SortedSet<Unity> unities) {
        this.index = index;
        this.value = value;
        this.setUnities(unities);
    }

    public Entity(final int index, final Double value, final Unity... unities) {
        this.index = index;
        this.value = value;
        this.setUnities(unities);
    }

    public Entity(final int index, final Double value) {
        this(index, value, Unity.NUMBER);
    }

    private void parse(final String input) throws ProcessorException {
        Double value;
        SortedSet<Unity> unities;
        Unity unity;
        int index, indexLeap;

        Matcher matcher = PATTERN_NUMBER.matcher(input);
        while (matcher.find()) {
            if (!this.hasUnity() || this.getUnityType().isAccumulable()) {
                try {
                    value = Double.parseDouble(matcher.group(GROUP_NUMBER_INTEGER));

                    final String unityGroup = matcher.group(GROUP_NUMBER_UNITY);
                    unities = Unity.getUnities(unityGroup);
                    if (unities.isEmpty() || unities.size() > 1) {
                        throw new ProcessorException(ERROR_UNITY, unityGroup);
                    }
                    unity = unities.first();

                    if (this.hasUnity()) { // XXX manage all conversions
                        boolean isNumber = Unity.Type.NUMBER.equals(unity.getType());
                        index = Unity.DATES.indexOf(this.firstUnity());
                        indexLeap = Unity.DATES_LEAP.indexOf(this.firstUnity());

                        if (isNumber && index > -1 && index < Unity.DATES.size() - 1) {
                            unity = Unity.DATES.get(index + 1);

                        } else if (isNumber && indexLeap > -1 && indexLeap < Unity.DATES.size() - 1) {
                            unity = Unity.DATES_LEAP.get(indexLeap + 1);

                        } else if (isNumber || !Unity.Type.DATE.equals(unity.getType())) {
                            throw new ProcessorException(ERROR_MALFORMED, input);
                        }
                    }

                    if (unity != null) {
                        this.getUnities().add(unity);
                    }

                    value = this.fromUnity(value);

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

        if (!this.hasUnity()) {
            if (PATTERN_UNITY.matcher(input).matches()) {
                final SortedSet<Unity> list = Unity.getUnities(input);
                if (!list.isEmpty() && !this.hasUnity()) {
                    this.setUnities(list.toArray(Unity[]::new));
                }
            }
            if (!this.hasUnity()) {
                LOGGER.error(ERROR_PARSE, input);
                throw new ProcessorException(ERROR_PARSE, input);
            }
        }
    }

    public Unity firstUnity() {
        return this.unities.first();
    }

    public boolean hasUnity() {
        return !this.unities.isEmpty();
    }

    public Double getValue() {
        return this.value;
    }

    public boolean isNumber() {
        return this.value != null && Unity.NUMBER.equals(this.firstUnity());
    }

    public boolean isUnity() {
        return this.value == null;
    }

    public SortedSet<Unity> getUnities() {
        return this.unities;
    }

    public Entity setUnities(final SortedSet<Unity> unities) {
        this.unities = unities;
        return this;
    }

    public Entity setUnities(final Unity... unities) {
        this.unities = new TreeSet<>(Unity.COMPARATOR_UNITIES);
        this.unities.addAll(Arrays.asList(unities));
        return this;
    }

    public Unity.Type getUnityType() {
        return this.firstUnity().getType();
    }

    public Double toUnity() {
        return this.firstUnity().toUnity(this.value);
    }

    public Double toUnity(final Double value) {
        return this.firstUnity().toUnity(value);
    }

    public Double fromUnity(final Double value) {
        return this.firstUnity().fromUnity(value);
    }

    public int getIndex() {
        return this.index;
    }

    @Override
    public String toString() {
        return this.getUnityType().format(this);
    }
}