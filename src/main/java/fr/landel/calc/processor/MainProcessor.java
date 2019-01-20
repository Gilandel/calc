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
    private static final char[] KNOWN_ARRAY = ("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_" + StringUtils.COMMA + StringUtils.DOT
            + StringUtils.SEMICOLON + StringUtils.SPACE + StringUtils.PARENTHESIS_OPEN + StringUtils.PARENTHESIS_CLOSE + Operators.ADD.getOperator()
            + Operators.SUBSTRACT.getOperator() + Operators.MULTIPLY.getOperator() + Operators.DEVIDE.getOperator() + Operators.POWER.getOperator()
            + Operators.MODULO.getOperator() + Operators.CONVERT.getOperator() + Operators.VARIABLE.getOperator() + StringUtils.DOLLAR).toCharArray();
    static {
        Arrays.sort(KNOWN_ARRAY);
    }

    private static boolean radian = true;
    private static boolean exact = false;
    private static boolean scientific = false;
    private static int precision = 3;
    private static boolean unityFullLength = true;
    private static boolean unitySpace = true;

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

    public static void setUnityFullLength(boolean unityFullLength) {
        MainProcessor.unityFullLength = unityFullLength;
    }

    public static void setUnitySpace(boolean unitySpace) {
        MainProcessor.unitySpace = unitySpace;
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

    public static boolean isUnityFullLength() {
        return MainProcessor.unityFullLength;
    }

    public static boolean isUnitySpace() {
        return MainProcessor.unitySpace;
    }

    public Formula process(final String input) throws ProcessorException {
        if (input == null || input.isBlank()) {
            throw new ProcessorException("Input formula cannot be null, empty or blank");
        }

        final long start = System.currentTimeMillis();

        final Formula result = new Formula(input, true, processFormula(prepare(input)));

        LOGGER.info("'{}' processed in {} ms", input, System.currentTimeMillis() - start);

        return result;
    }

    private String prepare(final String input) throws ProcessorException {
        final String unknown = input.chars().filter(c -> Arrays.binarySearch(KNOWN_ARRAY, (char) c) < 0)
                .collect(StringBuilder::new, (a, c) -> a.append((char) c), (a, b) -> a.append(b)).toString();
        if (!unknown.isEmpty()) {
            throw new ProcessorException(I18n.ERROR_CHARACTERS_UNKNOWN, unknown);
        }

        return StringUtils.replaceCommaByDot(StringUtils.removeAllSpaces(input));
    }

    private String processFormula(final String input) throws ProcessorException {
        if (RESTRICTED_LIST.stream().filter(input::contains).findAny().isPresent()) {
            throw new ProcessorException(I18n.ERROR_CHARACTERS_RESTRICTED, input, RESTRICTED_TEXT);
        }

        return processFormula(ResultBuilder.from(input));
    }

    // must return an entity (3h/2)>>i
    private String processFormula(final ResultBuilder input) throws ProcessorException {
        int parenthesisOpen = input.lastIndexOf(StringUtils.PARENTHESIS_OPEN);
        int parenthesisClose = input.indexOf(StringUtils.PARENTHESIS_CLOSE, parenthesisOpen + 1);

        if (parenthesisOpen < 0 && parenthesisClose < 0) {
            final Optional<Entity> entity = input.firstEntity();
            if (input.innerLength() == 0 && entity.isPresent()) {
                return entity.get().toString();
            } else {
                return new FormulaProcessor(input).process().toString();
            }

        } else if (parenthesisOpen < 0 || parenthesisClose <= parenthesisOpen) {
            throw new ProcessorException("Parenthesis error");
        }

        final ResultBuilder result = new ResultBuilder();
        final String block = input.substring(parenthesisOpen + 1, parenthesisClose);

        if (parenthesisOpen > -1) {

            final String prefix = input.substring(0, parenthesisOpen);
            final Optional<Functions> function = getFunction(prefix);

            if (function.isPresent()) {

                final FunctionThrowable<String, Entity, ProcessorException> processor = s -> {
                    final Optional<Entity> entity = input.getEntity(s);
                    if (entity.isPresent()) {
                        return entity.get();
                    } else {
                        return new FormulaProcessor(s).process();
                    }
                };

                final Entity[] entities = Arrays.stream(block.split(StringUtils.SEMICOLON)).filter(StringUtils::isNotEmpty).map(processor)
                        .toArray(Entity[]::new);

                final Entity entity = new FunctionProcessor(function.get(), entities).process();
                result.append(input.substring(0, parenthesisOpen - function.get().getFunction().length()));
                result.append(entity);
            } else {
                result.append(prefix).append(new FormulaProcessor(ResultBuilder.from(block, input)).process());
            }
        } else {
            result.append(block);
        }

        if (++parenthesisClose > -1 && parenthesisClose < input.length()) {
            final String suffix = input.substring(parenthesisClose, input.length());
            result.append(suffix);
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
                throw new ProcessorException("Function not found: {}", new String(inputFunction));
            }
        }

        return Optional.empty();
    }

    public static void main(String[] args) throws ProcessorException {
        long start = System.currentTimeMillis();

        MainProcessor processor = new MainProcessor();

        System.out.println(processor.processFormula("2007y3M-2008y2M"));
        System.out.println(processor.processFormula("100Y7M*2")); // bug > 293 ans (long)
        System.out.println(processor.processFormula("3*(3+2)"));
        System.out.println(processor.processFormula("((3+2)*pow(9/abs(3);1-5))-2"));
        System.out.println(processor.processFormula("15in>>m"));
        System.out.println(processor.processFormula("(15h+12s)>>his"));
        System.out.println(processor.processFormula("5K>>C"));
        System.out.println(processor.processFormula("5C>>K"));
        System.out.println(processor.processFormula("15/(1200/3937/12)"));
        System.out.println(processor.processFormula("15m>>in"));
        System.out.println(processor.processFormula("2017y12M >>y")); // bug

        System.out.printf("%n%d ms", System.currentTimeMillis() - start);
    }
}
