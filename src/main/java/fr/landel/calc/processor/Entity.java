package fr.landel.calc.processor;

import java.time.Duration;
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
    private Optional<Duration> duration;
    private SortedSet<Unity> unities = new TreeSet<>(Unity.COMPARATOR_UNITIES);
    private boolean decimal;
    private boolean positive;

    public Entity(final int index, final String input) throws ProcessorException {
        this.index = index;
        parse(input);
        prepare();
    }

    public Entity(final int index, final Double value, final Duration duration, final SortedSet<Unity> unities) {
        this.index = index;
        this.value = value;
        this.date = Optional.empty();
        this.duration = Optional.ofNullable(duration);
        this.setUnities(unities);
        prepare();
    }

    public Entity(final int index, final Double value, final Duration duration, final Unity... unities) {
        this.index = index;
        this.value = value;
        this.date = Optional.empty();
        this.duration = Optional.ofNullable(duration);
        this.setUnities(unities);
        prepare();
    }

    public Entity(final int index, final Double value, final LocalDateTime date, final SortedSet<Unity> unities) {
        this.index = index;
        this.value = value;
        this.date = Optional.ofNullable(date);
        this.duration = Optional.empty();
        this.setUnities(unities);
        prepare();
    }

    public Entity(final int index, final Double value, final LocalDateTime date, final Unity... unities) {
        this.index = index;
        this.value = value;
        this.date = Optional.ofNullable(date);
        this.duration = Optional.empty();
        this.setUnities(unities);
        prepare();
    }

    public Entity(final int index, final Double value, final SortedSet<Unity> unities) {
        this(index, value, (Duration) null, unities);
    }

    public Entity(final int index, final Double value, final Unity... unities) {
        this(index, value, (Duration) null, unities);
    }

    public Entity(final int index, final Double value) {
        this(index, value, Unity.NUMBER);
    }

    private void parse(final String input) throws ProcessorException {
        final EntityTmp entity = new EntityTmp();
        final SortedMap<Unity, Double> inputs = new TreeMap<>(Unity.COMPARATOR_UNITIES);

        final Matcher matcher = PATTERN_NUMBER.matcher(input);
        while (matcher.find()) {
            if (entity.unity == null || entity.accumulable) {
                try {
                    entity.value = Double.parseDouble(matcher.group(GROUP_NUMBER_DECIMAL));

                    final String unityGroup = matcher.group(GROUP_NUMBER_UNITY);
                    entity.unities = Unity.getUnities(unityGroup);

                    if (entity.unities.isEmpty() && !entity.accumulable) {
                        this.value = entity.value;

                    } else if (entity.unities.size() > 1) {
                        throw new ProcessorException(ERROR_UNITY, unityGroup);

                    } else {
                        this.check(entity, input, inputs);
                    }
                } catch (NumberFormatException e) {
                    LOGGER.error(e, ERROR_PARSE, input);
                    throw new ProcessorException(e, ERROR_PARSE, input);
                }
            } else {
                throw new ProcessorException(ERROR_BAD_FORMAT, input);
            }
        }

        if (UnityType.DATE.equals(entity.unityType)) {
            loadDuration(inputs);
            loadLocalDateTime(inputs);

        } else if (!this.isNumber() && !this.hasUnity()) {
            loadUnities(input);
        }

        if (this.date == null) {
            this.date = Optional.empty();
        }
        if (this.duration == null) {
            this.duration = Optional.empty();
        }
    }

    private void check(final EntityTmp entity, final String input, final SortedMap<Unity, Double> inputs) throws ProcessorException {

        if (entity.unities.isEmpty()) {
            entity.unity = entity.unity.next().orElseThrow(() -> new ProcessorException(ERROR_BAD_FORMAT, input));
        } else {
            entity.unity = entity.unities.first();
        }

        entity.unityType = entity.unity.getType();
        entity.accumulable = entity.unityType.isAccumulable();

        if (this.hasUnity() && !Objects.equals(this.getUnityType(), entity.unityType)) {
            throw new ProcessorException(ERROR_BAD_FORMAT, input);

        } else if (inputs.containsKey(entity.unity) || (Unity.INCOMPATIBLE_UNITIES.containsKey(entity.unity)
                && Unity.INCOMPATIBLE_UNITIES.get(entity.unity).stream().anyMatch(inputs::containsKey))) {
            throw new ProcessorException(ERROR_INCOMPATIBLE_UNITIES, input);

        } else {
            inputs.put(entity.unity, entity.value);
        }

        if (!UnityType.DATE.equals(entity.unityType)) {
            this.getUnities().add(entity.unity);
            entity.value = entity.unity.fromUnity(entity.value);

            if (this.value == null) {
                this.value = entity.value;
            } else {
                this.value += entity.value;
            }
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

    private void loadDuration(final SortedMap<Unity, Double> inputs) throws ProcessorException {
        if (inputs.containsKey(Unity.DATE_YEAR)) {
            return;
        }

        final Duration duration = Unity.mapToDuration(inputs, this.getUnities());

        this.value = Double.valueOf(duration.toNanos());
        this.duration = Optional.of(duration);
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
            decimal = MathUtils.isNotEqual(this.getValue(), Math.round(this.getValue()), MainProcessor.getPrecision());
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

    public Optional<Duration> getDuration() {
        return this.duration;
    }

    public boolean isNumber() {
        return this.value != null && (!this.hasUnity() || Unity.NUMBER.equals(this.firstUnity()));
    }

    public boolean isUnity() {
        return this.value == null;
    }

    public boolean isPositive() {
        return this.positive;
    }

    public boolean isDate() {
        return this.date.isPresent();
    }

    public boolean isDuration() {
        return this.duration.isPresent();
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

    private class EntityTmp {
        Double value;
        Unity unity;
        UnityType unityType;
        boolean accumulable;
        SortedSet<Unity> unities;
    }

    public static void main(String[] args) throws ProcessorException {
        // LocalDateTime.parse("2007-12-03T10:15:30");
        Entity entity1 = new Entity(0, "2007y 1M");
        Entity entity2 = new Entity(0, "2007y 4M");

        System.out.println((entity2.getValue() - entity1.getValue()) + " : " + DateUtils.NANO_PER_MONTHS_SUM.get(2));

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