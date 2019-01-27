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

import fr.landel.calc.config.I18n;
import fr.landel.calc.utils.MapUtils;

public enum Operators implements OperatorConstants {
    ADD("+", 3, ALL_EXCEPT_LAST, CHECK_ADD, FUN_ADD),
    SUBSTRACT("-", 3, ALL_EXCEPT_LAST, CHECK_SUBSTRACT, FUN_SUBSTRACT),
    MULTIPLY("*", 2, ALL_EXCEPT_FIRST_AND_LAST, IS_ANY_NUMBER, FUN_MULTIPLY),
    DEVIDE("/", 2, ALL_EXCEPT_FIRST_AND_LAST, IS_LEFT_NOT_UNITY_AND_RIGHT_NUMBER, FUN_DEVIDE),
    MODULO("%", 2, ALL_EXCEPT_FIRST_AND_LAST, IS_LEFT_NOT_UNITY_AND_RIGHT_NUMBER, FUN_MODULO),
    POWER("^", 1, ALL_EXCEPT_FIRST_AND_LAST, IS_LEFT_NOT_UNITY_AND_RIGHT_NUMBER, FUN_POWER),
    CONVERT(">>", 0, ALL_EXCEPT_FIRST_AND_LAST, IS_CONVERTIBLE, FUN_CONVERT),
    VARIABLE("=", 0, ALL_EXCEPT_FIRST_AND_LAST, IS_VARIABLE, FUN_VARIABLE);

    public static final List<Operators> BY_LENGTH_DESC = Arrays.stream(Operators.values())
            .sorted((a, b) -> Integer.compare(b.getLength(), a.getLength())).collect(Collectors.toList());
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
    private final BiPredicate<Integer, Integer> positionChecker;
    private final BiPredicate<Entity, Entity> validator;
    private final BiFunction<Entity, Entity, Entity> processor;

    private Operators(final String operator, final int priority, final BiPredicate<Integer, Integer> positionChecker,
            final BiPredicate<Entity, Entity> validator, final BiFunction<Entity, Entity, Entity> processor) {
        this.operator = operator;
        this.length = operator.length();
        this.priority = priority;
        this.positionChecker = positionChecker;
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
     * @return the position checker
     * @category getter
     */
    public BiPredicate<Integer, Integer> getPositionChecker() {
        return this.positionChecker;
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
            throw new ProcessorException(I18n.ERROR_OPERATOR, left, right, this.operator);
        }
        return this.processor.apply(left, right);
    }

    @Override
    public String toString() {
        return this.getOperator();
    }
}