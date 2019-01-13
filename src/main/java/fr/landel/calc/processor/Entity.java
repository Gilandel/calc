package fr.landel.calc.processor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.landel.calc.utils.DateUtils;
import fr.landel.calc.utils.Interval;
import fr.landel.calc.utils.Logger;
import fr.landel.calc.utils.MathUtils;

public class Entity {

    private static final Logger LOGGER = new Logger(Entity.class);

    private static final Pattern PATTERN_NUMBER = Pattern.compile("([+-]?(:?[0-9]+(:?\\.[0-9]+)?|\\.[0-9]+)([Ee][+-]?[0-9]+)?)([a-zA-Z]+)?");
    private static final int GROUP_NUMBER_DECIMAL = 1;
    private static final int GROUP_NUMBER_UNITY = 5;

    private static final Pattern PATTERN_UNITY = Pattern.compile("[a-zA-Z]+");

    private static final String ERROR_PARSE = "the following expression cannot be parsed: {}";
    private static final String ERROR_BAD_FORMAT = "the following expression is not formatted correctly: {}";
    private static final String ERROR_INCOMPATIBLE_UNITIES = "the following expression contains incompatible unities: {}";
    private static final String ERROR_UNITY = "the following unity is unknown: {}";

    private final int index;
    private Double value;
    private Optional<LocalDateTime> date;
    private Optional<Interval> interval;
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
        UnityType unityType = null;

        final SortedMap<Unity, Double> inputs = new TreeMap<>(Unity.COMPARATOR_UNITIES);

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
                        unityType = unity.getType();

                        if (this.hasUnity() && !Objects.equals(this.getUnityType(), unityType)) {
                            throw new ProcessorException(ERROR_BAD_FORMAT, input);

                        } else if (inputs.containsKey(unity) || Unity.INCOMPATIBLE_UNITIES.get(unity).stream().anyMatch(u -> inputs.containsKey(u))) {
                            throw new ProcessorException(ERROR_INCOMPATIBLE_UNITIES, input);

                        } else {
                            inputs.put(unity, value);
                        }

                        if (!UnityType.DATE.equals(unityType)) {
                            this.getUnities().add(unity);
                            value = unity.fromUnity(value);

                            if (this.value == null) {
                                this.value = value;
                            } else {
                                this.value += value;
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    LOGGER.error(e, ERROR_PARSE, input);
                    throw new ProcessorException(e, ERROR_PARSE, input);
                }
            } else {
                throw new ProcessorException(ERROR_BAD_FORMAT, input);
            }
        }

        if (UnityType.DATE.equals(unityType)) {
            loadInterval(inputs);
            loadLocalDateTime(inputs);

        } else if (!this.isNumber() && !this.hasUnity()) {
            loadUnities(input);
        }

        if (this.date == null) {
            this.date = Optional.empty();
        }
        if (this.interval == null) {
            this.interval = Optional.empty();
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

    private void loadInterval(final SortedMap<Unity, Double> inputs) throws ProcessorException {
        if (inputs.containsKey(Unity.DATE_YEAR)) {
            return;
        }

        final Interval interval = Unity.mapToInterval(inputs, this.getUnities());

        this.value = interval.getValue();
        this.interval = Optional.of(interval);
    }

    private void loadLocalDateTime(final SortedMap<Unity, Double> inputs) throws ProcessorException {
        if (!inputs.containsKey(Unity.DATE_YEAR)) {
            return;
        }

        final LocalDateTime date = Unity.mapToLocalDateTime(inputs, this.getUnities());

        this.value = DateUtils.toZeroNanosecond(date);
        this.date = Optional.of(date);
    }

    private void prepare() {
        if (this.isNumber()) {
            positive = MathUtils.isEqualOrGreater(this.getValue(), 0d, MainProcessor.getPrecision());
            decimal = MathUtils.isEqual(this.getValue(), Math.round(this.getValue()), MainProcessor.getPrecision());
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

    public UnityType getUnityType() {
        if (this.hasUnity()) {
            return this.firstUnity().getType();
        } else {
            return UnityType.NUMBER;
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

    public boolean isUnity(final UnityType type) {
        return this.isUnity() && type.equals(this.getUnityType());
    }

    @Override
    public String toString() {
        return this.getUnityType().format(this);
    }

    public static void main(String[] args) throws ProcessorException {
        // LocalDateTime.parse("2007-12-03T10:15:30");
        Entity entity1 = new Entity(0, "2007y 1M");
        Entity entity2 = new Entity(0, "2007y 3M");

        System.out.println((entity2.getValue() - entity1.getValue()) + " : " + DateUtils.NANO_PER_MONTHS_SUM.get(1));

        double annee = DateUtils.toZeroNanosecond(2007) + DateUtils.NANO_PER_MONTHS_SUM.get(11);
        double annee1 = LocalDateTime.of(2017, 1, 1, 0, 0).toEpochSecond(ZoneOffset.UTC) * 1_000_0000_000d + DateUtils.NANO_1970;
        double annee2 = LocalDateTime.of(2000, 1, 1, 0, 0).toEpochSecond(ZoneOffset.UTC) * 1_000_0000_000d + DateUtils.NANO_1970;

        Double y = (annee1 - DateUtils.NANO_1970) / 1_000_0000_000d;
        Double y1 = (annee1 - annee2) / 1_000_0000_000d;
        // long s = y ;
        LocalDateTime date = LocalDateTime.ofEpochSecond(y.longValue(), 0, ZoneOffset.UTC);

        System.out.printf("%,.6f%n%,.6f%n%,.6f%n%,.6f%n%s", entity1.getValue(), annee, annee1, annee2, date);
    }
}