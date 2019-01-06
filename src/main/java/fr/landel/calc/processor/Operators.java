package fr.landel.calc.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import fr.landel.calc.utils.MapUtils;

public enum Operators {
    ADD("+", 3, (a, b) -> new Entity(a.getIndex(), b.getIndex(), a.getValue() + b.getValue())),
    SUBSTRACT("-", 3, (a, b) -> new Entity(a.getIndex(), b.getIndex(), a.getValue() - b.getValue())),
    MULTIPLY("*", 2, (a, b) -> new Entity(a.getIndex(), b.getIndex(), a.getValue() * b.getValue())),
    DEVIDE("/", 2, (a, b) -> new Entity(a.getIndex(), b.getIndex(), a.getValue() / b.getValue())),
    MODULO("%", 2, (a, b) -> new Entity(a.getIndex(), b.getIndex(), a.getValue() % b.getValue())),
    POWER("^", 1, (a, b) -> new Entity(a.getIndex(), b.getIndex(), Math.pow(a.getValue(), b.getValue()))),
    CONVERT(">>", 0, (a, b) -> new Entity(a.getIndex(), b.getIndex(), 0d)); // TODO

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
    private final BiFunction<Entity, Entity, Entity> processor;

    private Operators(final String operator, final int priority, final BiFunction<Entity, Entity, Entity> processor) {
        this.operator = operator;
        this.length = operator.length();
        this.priority = priority;
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
     */
    public Entity process(final Entity left, final Entity right) {
        return this.processor.apply(left, right);
    }

    @Override
    public String toString() {
        return this.getOperator();
    }
}