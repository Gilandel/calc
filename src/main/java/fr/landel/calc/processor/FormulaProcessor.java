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

import fr.landel.calc.config.I18n;
import fr.landel.calc.utils.MapUtils;
import fr.landel.calc.utils.StringUtils;

public class FormulaProcessor implements Processor {

    // private static final Logger LOGGER =
    // new Logger(SimpleFormulaProcessor.class);

    private static final char[] POW_10 = "eE".toCharArray();
    private static final Operators[] POW_10_OPERATORS = {Operators.ADD, Operators.SUBSTRACT};
    static {
        Arrays.sort(POW_10);
        Arrays.sort(POW_10_OPERATORS);
    }
    private static final Supplier<SortedSet<Integer>> SUPPLIER_SORTED_SET = () -> new TreeSet<>(Integer::compareTo);
    private static final Supplier<List<Integer>> SUPPLIER_LIST = ArrayList::new;
    private static final BiConsumer<List<Integer>, ? super SortedSet<Integer>> ACCUMULATOR = (a, b) -> a.addAll(b);
    private static final BiConsumer<List<Integer>, List<Integer>> COMBINER = (a, b) -> a.addAll(b);

    private final String formula;
    private final int length;
    private final char[] chars;
    private final ResultBuilder result;

    private UnityType type;
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
            final Entity entity = result.get();

            if (!entity.isUnity()) {
                return entity;

            } else if (entity.isVariable()) {
                throw new ProcessorException(I18n.ERROR_VARIABLE_VALUE_MISSING, entity);

            } else {
                throw new ProcessorException(I18n.ERROR_UNITY_VALUE_MISSING, entity);
            }
        } else {
            throw new ProcessorException(I18n.ERROR_FORMULA_PARSE, this.formula);
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
                    throw new ProcessorException(I18n.ERROR_FORMULA_OPERATOR_POSITION, o, pos);
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
                if (pos > 0 && (lastPos == -1 || lastPos + lastOperator.getLength() < pos)
                        && (Arrays.binarySearch(POW_10_OPERATORS, operator) < 0 || Arrays.binarySearch(POW_10, this.chars[pos - 1]) < 0)) {

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
            throw new ProcessorException(I18n.ERROR_FORMULA_OPERATOR_MISSING, this.result);

        } else {
            this.sortedOperators = Collections.emptySortedMap();
        }
    }

    private void addSegment(final int index, final int start, final int end) throws ProcessorException {
        final Optional<Entity> optionalEntity;
        final Entity entity;

        if (result != null) {
            optionalEntity = result.subEntity(start, end);
        } else {
            optionalEntity = Optional.empty();
        }

        if (optionalEntity.isPresent()) {
            entity = optionalEntity.get();
        } else {
            entity = new Entity(index, this.formula.substring(start, end), type);
        }

        if (entity.getUnityType() != null && !UnityType.NUMBER.equals(entity.getUnityType())) {
            this.type = entity.getUnityType();
        }

        this.segments.put(index, entity);
    }

    private Optional<Entity> calculate() throws ProcessorException {
        Entity result = null;

        if (!this.segments.isEmpty()) {

            final Map<Integer, Entity> used = new HashMap<>();

            int index, leftIndex, rightIndex;
            Entity left, right;

            for (Entry<Integer, Operators> entry : sortedOperators.entrySet()) {
                index = this.positions.indexOf(entry.getKey());

                if (index > 0) {
                    leftIndex = this.positions.get(index - 1);
                } else {
                    leftIndex = 0;
                }
                rightIndex = entry.getKey();

                left = Optional.ofNullable(used.get(leftIndex)).orElse(this.segments.get(leftIndex));
                right = Optional.ofNullable(used.get(rightIndex)).orElse(this.segments.get(rightIndex));

                result = entry.getValue().process(left, right);

                used.put(leftIndex, result);
                used.put(rightIndex, result);
            }
        } else if (this.result != null && this.result.hasEntities() && ResultBuilder.FIRST_ID.equals(this.formula)) {
            result = this.result.firstEntity().get();
        } else {
            result = new Entity(0, this.formula);
        }

        return Optional.ofNullable(result);
    }
}
