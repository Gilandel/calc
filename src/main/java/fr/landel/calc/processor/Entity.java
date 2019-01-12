package fr.landel.calc.processor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.landel.calc.utils.DateUtils;
import fr.landel.calc.utils.Logger;
import fr.landel.calc.utils.MathUtils;

public class Entity {

    private static final Logger LOGGER = new Logger(Entity.class);

    private static final Pattern PATTERN_NUMBER = Pattern.compile("([+-]?(:?[0-9]+(:?\\.[0-9]+)?|\\.[0-9]+)([Ee][+-]?[0-9]+)?)([a-zA-Z]+)?");
    private static final int GROUP_NUMBER_DECIMAL = 1;
    private static final int GROUP_NUMBER_UNITY = 5;

    private static final Pattern PATTERN_UNITY = Pattern.compile("[a-zA-Z]+");

    private static final String ERROR_PARSE = "the following expression cannot be parsed: {}";
    private static final String ERROR_MALFORMED = "the following expression is malformed: {}";
    private static final String ERROR_UNITY = "the following unity is unknown: {}";

    private final int index;
    private Double value;
    private Optional<LocalDateTime> date;
    private SortedSet<Unity> unities = new TreeSet<>(Unity.COMPARATOR_UNITIES);
    private boolean decimal;
    private boolean positive;

    public Entity(final int index, final String input) throws ProcessorException {
        this.index = index;
        parse(input);
        prepare();
    }

    public Entity(final int index, final Double value, final SortedSet<Unity> unities) {
        this.index = index;
        this.value = value;
        this.setUnities(unities);
        prepare();
    }

    public Entity(final int index, final Double value, final Unity... unities) {
        this.index = index;
        this.value = value;
        this.setUnities(unities);
        prepare();
    }

    public Entity(final int index, final Double value) {
        this(index, value, Unity.NUMBER);
    }

    private void parse(final String input) throws ProcessorException {
        Double value;
        SortedSet<Unity> unities;
        Unity unity;

        Matcher matcher = PATTERN_NUMBER.matcher(input);
        while (matcher.find()) {
            if (!this.hasUnity() || this.getUnityType().isAccumulable()) {
                try {
                    value = Double.parseDouble(matcher.group(GROUP_NUMBER_DECIMAL));

                    final String unityGroup = matcher.group(GROUP_NUMBER_UNITY);
                    unities = Unity.getUnities(unityGroup);

                    if (unities.isEmpty()) {
                        this.value = value;

                    } else if (unities.size() > 1) {
                        throw new ProcessorException(ERROR_UNITY, unityGroup);

                    } else {
                        unity = unities.first();

                        if (this.hasUnity() && !Objects.equals(this.getUnityType(), unity.getType())) {
                            throw new ProcessorException(ERROR_MALFORMED, input);
                        }

                        this.getUnities().add(unity);

                        value = unity.fromUnity(value);

                        if (this.value == null) {
                            this.value = value;
                        } else {
                            this.value += value;
                        }
                    }
                } catch (NumberFormatException e) {
                    LOGGER.error(e, ERROR_PARSE, input);
                    throw new ProcessorException(e, ERROR_PARSE, input);
                }
            } else {
                throw new ProcessorException(ERROR_MALFORMED, input);
            }
        }

        if (!this.isNumber() && !this.hasUnity()) {
            loadUnities(input);
            this.date = Optional.empty();

        } else if (this.getUnities().contains(Unity.DATE_YEAR)) {
            loadLocalDateTime();

        } else {
            this.date = Optional.empty();
        }
    }

    private void loadUnities(final String input) throws ProcessorException {
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

    private void loadLocalDateTime() {
        double epochNanoseconds = this.value - DateUtils.NANO_1970;
        long seconds = (long) Math.floor(epochNanoseconds / DateUtils.NANO_PER_SECOND);
        int nanoseconds = (int) Math.round(epochNanoseconds - (seconds * DateUtils.NANO_PER_SECOND));
        this.date = Optional.of(LocalDateTime.ofEpochSecond(seconds, nanoseconds, ZoneOffset.UTC));
        // TODO correct diff 2017y12M = 27 dec 2017
    }

    private void prepare() {
        if (this.isNumber()) {
            positive = this.getValue() >= 0d; // following compute precision
            decimal = Math.abs(this.getValue() - Math.round(this.getValue())) > 1d / MathUtils.pow10(MainProcessor.getPrecision());
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

    public Optional<LocalDateTime> getDate() {
        return this.date;
    }

    public boolean isNumber() {
        return this.value != null && (!this.hasUnity() || Unity.NUMBER.equals(this.firstUnity()));
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
        if (this.hasUnity()) {
            return this.firstUnity().getType();
        } else {
            return Unity.Type.NUMBER;
        }
    }

    public Double toUnityOrValue() {
        if (this.hasUnity()) {
            return this.firstUnity().toUnity(this.value);
        } else {
            return this.value;
        }
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

    public boolean isInteger() {
        return !this.decimal;
    }

    public boolean isPositiveDecimal() {
        return this.positive;
    }

    public boolean isDecimal() {
        return this.decimal;
    }

    public boolean isUnity(final Unity.Type type) {
        return this.isUnity() && type.equals(this.getUnityType());
    }

    @Override
    public String toString() {
        return this.getUnityType().format(this);
    }

    public static void main(String[] args) {
        LocalDateTime.parse("2007-12-03T10:15:30");
        // new Entity("2007y 12M 03D 10h 15i 30s")
    }
}