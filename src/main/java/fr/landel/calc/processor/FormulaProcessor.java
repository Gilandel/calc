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
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import fr.landel.calc.utils.MapUtils;
import fr.landel.calc.utils.StringUtils;

public class FormulaProcessor implements Processor {

    // private static final Logger LOGGER =
    // new Logger(SimpleFormulaProcessor.class);

    private static final char[] POW_10 = "eE".toCharArray();
    static {
        Arrays.sort(POW_10);
    }
    private static final Supplier<SortedSet<Integer>> SUPPLIER_SORTED_SET = () -> new TreeSet<>(Integer::compareTo);
    private static final Supplier<List<Integer>> SUPPLIER_LIST = ArrayList::new;
    private static final BiConsumer<List<Integer>, ? super SortedSet<Integer>> ACCUMULATOR = (a, b) -> a.addAll(b);
    private static final BiConsumer<List<Integer>, List<Integer>> COMBINER = (a, b) -> a.addAll(b);

    private static final String ERROR_PARSE = "the expression cannot be parsed";
    private static final String ERROR_OPERATOR_POSITION = "the operator '{}' cannot be placed at: {}";
    private static final String ERROR_OPERATOR_MISSING = "the operator is missing between: {} and {}";

    private final String formula;
    private final int length;
    private final char[] chars;
    private final ResultBuilder result;

    private final List<Integer> positions = new ArrayList<>();
    private final Map<Integer, Entity> segments = new HashMap<>();
    private SortedMap<Integer, Operators> sortedOperators;

    public FormulaProcessor(final String formula, final ResultBuilder result) {
        this.formula = StringUtils.requireNonBlank(formula);
        this.length = this.formula.length();
        this.chars = this.formula.toCharArray();
        this.result = result;
    }

    public FormulaProcessor(final String formula) {
        this(formula, null);
    }

    public FormulaProcessor(final ResultBuilder result) {
        this(result.getFormula(), result);
    }

    @Override
    public Entity process() throws ProcessorException {
        loadOperatorsAndSegments();

        final Optional<Entity> result = calculate();
        if (result.isPresent()) {
            return result.get();
        } else {
            throw new ProcessorException(ERROR_PARSE);
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
                if (o.getPositionChecker().test(pos, this.length)) {
                    positions.put(pos, o);
                } else {
                    throw new ProcessorException(ERROR_OPERATOR_POSITION, o, pos);
                }
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
                        addSegment(lastRealPos, lastRealPos + lastRealOperator.getLength(), pos);
                    } else {
                        addSegment(0, 0, pos);
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
                addSegment(lastRealPos, lastRealPos + lastRealOperator.getLength(), this.chars.length);
            }

            // merge sorted operators into a list following priority and
            // position
            final List<Integer> sortedOperators = sortedOperatorsByPriority.values().stream().collect(SUPPLIER_LIST, ACCUMULATOR, COMBINER);

            // rebuild map following sorted operators
            this.sortedOperators = new TreeMap<>((a, b) -> Integer.compare(sortedOperators.indexOf(a), sortedOperators.indexOf(b)));
            this.sortedOperators.putAll(realOperatorsByPosition);

        } else if (this.result != null && this.result.innerLength() > 0 && this.result.hasEntities()) {
            final int pos = this.result.indexOf(StringUtils.ID_OPEN);
            final String value1, value2;
            if (pos > 0) {
                value1 = this.result.substring(0, pos);
                value2 = this.result.firstEntity().toString();
            } else {
                value1 = this.result.firstEntity().toString();
                value2 = this.result.substring(pos + value1.length());
            }
            throw new ProcessorException(ERROR_OPERATOR_MISSING, value1, value2);

        } else {
            this.sortedOperators = Collections.emptySortedMap();
        }
    }

    private void addSegment(final int index, final int start, final int end) throws ProcessorException {
        final Optional<Entity> entity;
        if (result != null) {
            entity = result.subEntity(start, end);
        } else {
            entity = Optional.empty();
        }
        if (entity.isPresent()) {
            this.segments.put(index, entity.get());
        } else {
            this.segments.put(index, new Entity(index, this.formula.substring(start, end)));
        }
    }

    private Optional<Entity> calculate() throws ProcessorException {
        Entity result = null;

        if (!this.segments.isEmpty()) {

            final Map<Integer, Entity> used = new HashMap<>();

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
            }
        } else if (this.result != null && this.result.hasEntities() && ResultBuilder.FIRST_ID.equals(this.formula)) {
            result = this.result.firstEntity().get();
        } else {
            result = new Entity(0, this.formula);
        }

        return Optional.ofNullable(result);
    }

    public static void main(String[] args) throws ProcessorException {
        Processor processor = new FormulaProcessor("15");

        System.out.println(processor.process());
    }
}
