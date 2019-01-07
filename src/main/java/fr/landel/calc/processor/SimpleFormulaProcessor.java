package fr.landel.calc.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Supplier;

import fr.landel.calc.utils.MapUtils;
import fr.landel.calc.utils.StringUtils;

public class SimpleFormulaProcessor implements Processor {

    // private static final Logger LOGGER =
    // new Logger(SimpleFormulaProcessor.class);

    private static final char[] POW_10 = "eE".toCharArray();
    static {
        Arrays.sort(POW_10);
    }
    private static final Supplier<SortedSet<Integer>> SUPPLIER_SORTED_SET = () -> new TreeSet<>(Integer::compareTo);

    private final String formula;
    private final char[] chars;

    private final List<Integer> positions = new ArrayList<>();
    private final Map<Integer, Entity> segments = new HashMap<>();
    private SortedMap<Integer, Operators> sortedOperators;

    public SimpleFormulaProcessor(final String formula) {
        this.formula = StringUtils.requireNonBlank(formula);
        this.chars = this.formula.toCharArray();
    }

    @Override
    public Entity process() throws ProcessorException {
        loadOperatorsAndSegments();

        final Optional<Entity> result = calculate();
        if (result.isPresent()) {
            return result.get();
        } else {
            return new Entity(0, this.formula);
        }
    }

    private void loadOperatorsAndSegments() throws ProcessorException {
        final SortedMap<Integer, Operators> positions = new TreeMap<>(Integer::compareTo);
        final SortedMap<Integer, SortedSet<Integer>> sortedOperatorsByPriority = new TreeMap<>(Integer::compareTo);
        final Map<Integer, Operators> realOperatorsByPosition = new HashMap<>();

        // get operator's position
        for (Operators o : Operators.BY_LENGTH_DESC) {
            final String operator = o.getOperator();
            final int length = o.getLength();
            int pos = -length;
            while ((pos = this.formula.indexOf(operator, pos + length)) > -1) {
                positions.put(pos, o);
            }
        }

        if (!positions.isEmpty()) {

            // exclude false operators

            int pos, lastPos = -1, lastRealPos = -1;
            Operators operator, lastOperator = null, lastRealOperator = null;

            for (Entry<Integer, Operators> entry : positions.entrySet()) {
                pos = entry.getKey();
                operator = entry.getValue();

                // remove false operators = +1, ++1, E+1, e+1
                if (pos > 0 && (lastPos == -1 || lastPos + lastOperator.getLength() < pos) && Arrays.binarySearch(POW_10, this.chars[pos - 1]) < 0) {
                    this.positions.add(pos);
                    if (lastRealPos > -1) {
                        this.segments.put(lastRealPos, new Entity(lastRealPos, this.formula.substring(lastRealPos + lastRealOperator.getLength(), pos)));
                    } else {
                        this.segments.put(0, new Entity(0, this.formula.substring(0, pos)));
                    }
                    realOperatorsByPosition.put(pos, operator);
                    MapUtils.getOrPutIfAbsent(sortedOperatorsByPriority, operator.getPriority(), SUPPLIER_SORTED_SET).add(pos);

                    lastRealPos = pos;
                    lastRealOperator = operator;
                }

                lastPos = pos;
                lastOperator = operator;
            }
            if (lastRealPos > -1 && lastRealPos + lastRealOperator.getLength() < this.chars.length) {
                this.segments.put(lastRealPos, new Entity(lastRealPos, this.formula.substring(lastRealPos + lastRealOperator.getLength(), this.chars.length)));
            }

            // merge sorted operators into a list following priority and
            // position
            final List<Integer> sortedOperators = sortedOperatorsByPriority.values().stream().collect(ArrayList::new, (a, b) -> a.addAll(b), (a, b) -> a.addAll(b));

            // rebuild map following sorted operators
            this.sortedOperators = new TreeMap<>((a, b) -> Integer.compare(sortedOperators.indexOf(a), sortedOperators.indexOf(b)));
            this.sortedOperators.putAll(realOperatorsByPosition);
        } else {
            this.sortedOperators = Collections.emptySortedMap();
        }
    }

    private Optional<Entity> calculate() throws ProcessorException {
        Entity result = null;

        if (!this.segments.isEmpty()) {

            Map<Integer, Entity> used = new HashMap<>();

            int index;
            Entity left, right;
            Entity leftResult, rightResult;

            for (Entry<Integer, Operators> entry : sortedOperators.entrySet()) {
                index = this.positions.indexOf(entry.getKey());
                if (index > 0) {
                    left = this.segments.get(this.positions.get(index - 1));
                } else {
                    left = this.segments.get(0);
                }
                right = this.segments.get(entry.getKey());

                leftResult = used.get(left.getIndex());
                rightResult = used.get(right.getIndex());

                if (leftResult == null && rightResult == null) {
                    result = entry.getValue().process(left, right);
                    used.put(left.getIndex(), result);
                    used.put(right.getIndex(), result);

                } else if (leftResult == null) {
                    result = entry.getValue().process(left, rightResult);
                    used.put(left.getIndex(), result);

                } else {
                    result = entry.getValue().process(leftResult, right);
                    used.put(right.getIndex(), result);
                }

                // System.out.println(left + " " + entry.getValue() + " " +
                // right + " = " + result);
            }
        }

        return Optional.ofNullable(result);
    }

    public static void main(String[] args) throws ProcessorException {
        Processor processor = new SimpleFormulaProcessor("15");

        System.out.println(processor.process());
    }
}
