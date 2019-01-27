package fr.landel.calc.processor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import fr.landel.calc.config.Formula;
import fr.landel.calc.config.I18n;
import fr.landel.calc.function.FunctionThrowable;
import fr.landel.calc.utils.Logger;
import fr.landel.calc.utils.StringUtils;

public class MainProcessor {

    // TODO gerer radian / exact / scientific

    private static final Logger LOGGER = new Logger(MainProcessor.class);

    private static final List<String> RESTRICTED_LIST = Arrays.asList(StringUtils.ID_OPEN, StringUtils.ID_CLOSE);
    private static final String RESTRICTED_TEXT = RESTRICTED_LIST.stream().collect(StringUtils.COMMA_JOINING_COLLECTOR);
    private static final char[] KNOWN_ARRAY = ("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_°'\"" + StringUtils.COMMA
            + StringUtils.DOT + StringUtils.SEMICOLON + StringUtils.SPACE + StringUtils.PARENTHESIS_OPEN + StringUtils.PARENTHESIS_CLOSE
            + Operators.ADD.getOperator() + Operators.SUBSTRACT.getOperator() + Operators.MULTIPLY.getOperator() + Operators.DEVIDE.getOperator()
            + Operators.POWER.getOperator() + Operators.MODULO.getOperator() + Operators.CONVERT.getOperator() + Operators.VARIABLE.getOperator()
            + StringUtils.DOLLAR).toCharArray();
    static {
        Arrays.sort(KNOWN_ARRAY);
    }

    private static boolean radian = true;
    private static boolean exact = false;
    private static boolean scientific = false;
    private static int precision = 3;
    private static boolean unityAbbrev = true;
    private static boolean unitiesSpace = true;
    private static boolean valuesSpace = true;

    public MainProcessor() {
    }

    public static void setRadian(final boolean radian) {
        MainProcessor.radian = radian;
    }

    public static void setExact(final boolean exact) {
        MainProcessor.exact = exact;
    }

    public static void setScientific(final boolean scientific) {
        MainProcessor.scientific = scientific;
    }

    public static void setPrecision(final int precision) {
        MainProcessor.precision = precision;
    }

    public static void setUnityAbbrev(boolean unityAbbrev) {
        MainProcessor.unityAbbrev = unityAbbrev;
    }

    public static void setUnitiesSpace(boolean unitiesSpace) {
        MainProcessor.unitiesSpace = unitiesSpace;
    }

    public static void setValuesSpace(boolean valuesSpace) {
        MainProcessor.valuesSpace = valuesSpace;
    }

    public static boolean isRadian() {
        return MainProcessor.radian;
    }

    public static boolean isExact() {
        return MainProcessor.exact;
    }

    public static boolean isScientific() {
        return MainProcessor.scientific;
    }

    public static int getPrecision() {
        return MainProcessor.precision;
    }

    public static boolean isUnityAbbrev() {
        return MainProcessor.unityAbbrev;
    }

    public static boolean isUnitiesSpace() {
        return MainProcessor.unitiesSpace;
    }

    public static boolean isValuesSpace() {
        return MainProcessor.valuesSpace;
    }

    public Formula process(final String input) throws ProcessorException {
        if (input == null || input.isBlank()) {
            throw new ProcessorException(I18n.ERROR_FORMULA_EMPTY);
        }

        final long start = System.currentTimeMillis();

        final Formula result = new Formula(input, true, processFormula(prepare(input)).toString());

        LOGGER.info("'{}' processed in {} ms", input, System.currentTimeMillis() - start);

        return result;
    }

    public Entity processToEntity(final String input) throws ProcessorException {
        if (input == null || input.isBlank()) {
            throw new ProcessorException(I18n.ERROR_FORMULA_EMPTY);
        }

        return processFormula(prepare(input));
    }

    private String prepare(final String input) throws ProcessorException {
        final String unknown = input.chars().filter(c -> Arrays.binarySearch(KNOWN_ARRAY, (char) c) < 0)
                .collect(StringBuilder::new, (a, c) -> a.append((char) c), (a, b) -> a.append(b)).toString();
        if (!unknown.isEmpty()) {
            throw new ProcessorException(I18n.ERROR_CHARACTERS_UNKNOWN, unknown);
        }

        return StringUtils.replaceCommaByDot(StringUtils.removeAllSpaces(input));
    }

    private Entity processFormula(final String input) throws ProcessorException {
        if (RESTRICTED_LIST.stream().filter(input::contains).findAny().isPresent()) {
            throw new ProcessorException(I18n.ERROR_CHARACTERS_RESTRICTED, input, RESTRICTED_TEXT);
        }

        return processFormula(ResultBuilder.from(input));
    }

    private Entity processFormula(final ResultBuilder input) throws ProcessorException {
        int parenthesisOpen = input.lastIndexOf(StringUtils.PARENTHESIS_OPEN);
        int parenthesisClose = input.indexOf(StringUtils.PARENTHESIS_CLOSE, parenthesisOpen + 1);

        if (parenthesisOpen < 0 && parenthesisClose < 0) {
            final Optional<Entity> entity = input.firstEntity();
            if (input.innerLength() == 0 && entity.isPresent()) {
                return entity.get();
            } else {
                return new FormulaProcessor(input).process();
            }

        } else if (parenthesisOpen < 0 || parenthesisClose <= parenthesisOpen) {
            throw new ProcessorException(I18n.ERROR_FORMULA_PARENTHESIS);
        }

        final ResultBuilder result = new ResultBuilder();
        final String block = input.substring(parenthesisOpen + 1, parenthesisClose);

        if (parenthesisOpen > -1) {

            final String prefix = input.substring(0, parenthesisOpen);
            final Optional<Functions> function = getFunction(prefix);

            if (function.isPresent()) {

                final FunctionThrowable<String, Entity, ProcessorException> processor = s -> {
                    final ResultBuilder rb = ResultBuilder.from(s, input);
                    if (rb.innerLength() == 0 && rb.getEntitiesSize() == 1) {
                        return rb.firstEntity().get();
                    } else {
                        return new FormulaProcessor(rb).process();
                    }
                };

                final Entity[] entities = Arrays.stream(block.split(StringUtils.SEMICOLON)).filter(StringUtils::isNotEmpty).map(processor)
                        .toArray(Entity[]::new);

                final Entity entity = new FunctionProcessor(function.get(), entities).process();
                result.append(input.substring(0, parenthesisOpen - function.get().getFunction().length()), input);
                result.append(entity);
            } else {
                result.append(prefix, input).append(new FormulaProcessor(ResultBuilder.from(block, input)).process());
            }
        } else {
            result.append(block);
        }

        if (++parenthesisClose > -1 && parenthesisClose < input.length()) {
            final String suffix = input.substring(parenthesisClose, input.length());
            result.append(suffix, input);
        }
        return processFormula(result);
    }

    private Optional<Functions> getFunction(final String input) throws ProcessorException {
        final char[] chars = input.toCharArray();
        final int len = chars.length;

        int pos = len;

        while (--pos >= 0 && Arrays.binarySearch(FunctionsTree.CHARS, chars[pos]) > -1) {
        }

        if (pos < len - 1) {
            final char[] inputFunction = Arrays.copyOfRange(chars, pos + 1, len);
            final Optional<Functions> function = FunctionsTree.check(inputFunction);
            if (function.isPresent()) {
                return function;
            } else {
                throw new ProcessorException(I18n.ERROR_FUNCTION_UNKNOWN, new String(inputFunction));
            }
        }

        return Optional.empty();
    }
}
