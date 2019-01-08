package fr.landel.calc.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import fr.landel.calc.utils.MapUtils;

public enum Operators implements OperatorConstants {
    ADD("+", 3, IS_SAME_TYPE_AND_NOT_UNITY, (a, b) -> new Entity(a.getIndex(), a.getValue() + b.getValue(), Unity.min(a.getUnities(), b.getUnities()))),
    SUBSTRACT("-", 3, IS_SAME_TYPE_AND_NOT_UNITY, (a, b) -> new Entity(a.getIndex(), a.getValue() - b.getValue(), Unity.min(a.getUnities(), b.getUnities()))),
    MULTIPLY("*", 2, IS_ANY_NUMBER, FUN_MULTIPLY),
    DEVIDE("/", 2, IS_LEFT_NOT_UNITY_AND_RIGHT_NUMBER, FUN_DEVIDE),
    MODULO("%", 2, IS_LEFT_NOT_UNITY_AND_RIGHT_NUMBER, (a, b) -> new Entity(a.getIndex(), a.fromUnity(a.toUnity() % b.getValue()), a.getUnities())),
    POWER("^", 1, IS_LEFT_NOT_UNITY_AND_RIGHT_NUMBER, (a, b) -> new Entity(a.getIndex(), a.fromUnity(Math.pow(a.toUnity(), b.getValue())), a.getUnities())),
    CONVERT(">>", 0, IS_CONVERTIBLE, (a, b) -> a.setUnities(b.getUnities()));

    public static final List<Operators> BY_LENGTH_DESC = Arrays.stream(Operators.values()).sorted((a, b) -> Integer.compare(b.getLength(), a.getLength())).collect(Collectors.toList());
    public static final SortedMap<Integer, List<Operators>> BY_PRIORITY;
    public static final List<Integer> PRIORITIES;
    static {
        final SortedMap<Integer, List<Operators>> operatorsByPriority = new TreeMap<>(Integer::compareTo);
        Arrays.stream(Operators.values()).forEach(o -> MapUtils.getOrPutIfAbsent(operatorsByPriority, o.getPriority(), ArrayList::new).add(o));
        operatorsByPriority.entrySet().forEach(e -> operatorsByPriority.put(e.getKey(), Collections.unmodifiableList(e.getValue())));
        BY_PRIORITY = Collections.unmodifiableSortedMap(operatorsByPriority);
        PRIORITIES = Collections.unmodifiableList(new ArrayList<>(BY_PRIORITY.keySet()));
    }
    private final String operator;
    private final int length;
    private final int priority;
    private final BiPredicate<Entity, Entity> validator;
    private final BiFunction<Entity, Entity, Entity> processor;

    private Operators(final String operator, final int priority, final BiPredicate<Entity, Entity> validator, final BiFunction<Entity, Entity, Entity> processor) {
        this.operator = operator;
        this.length = operator.length();
        this.priority = priority;
        this.validator = validator;
        this.processor = processor;
    }

    /**
     * @return the operator
     * @category getter
     */
    public String getOperator() {
        return this.operator;
    }

    /**
     * @return the length
     * @category getter
     */
    public int getLength() {
        return this.length;
    }

    /**
     * @return the priority
     * @category getter
     */
    public int getPriority() {
        return this.priority;
    }

    /**
     * @param left
     *            the left entity
     * @param right
     *            the right entity
     * @return the calculated entity
     * @throws ProcessorException
     *             if inputs don't match the validator
     */
    public Entity process(final Entity left, final Entity right) throws ProcessorException {
        if (!this.validator.test(left, right)) {
            throw new ProcessorException("At least one value ({}, {}) doesn't match the predicate for operator {}", left, right, this.operator);
        }
        return this.processor.apply(left, right);
    }

    @Override
    public String toString() {
        return this.getOperator();
    }
}