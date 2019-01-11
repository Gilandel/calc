package fr.landel.calc.processor;

import java.util.Arrays;
import java.util.Optional;

import fr.landel.calc.config.Conf;
import fr.landel.calc.config.Formula;
import fr.landel.calc.utils.MathUtils;
import fr.landel.calc.utils.StringUtils;

public class MainProcessor {

    // TODO corriger detection des caracteres avant fonction (2e())
    // TODO gerer radian / exact / scientific
    // TODO remonter les erreurs traduites

    private String decimalSeparator = ".";
    private String thousandSeparator = ",";

    private static boolean radian = true;
    private static boolean exact = false;
    private static boolean scientific = false;
    private static int precision = 3;

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

    public void updateI18n() {
        this.decimalSeparator = Conf.DECIMAL_SEPARATOR.getString().get();
        this.thousandSeparator = Conf.THOUSAND_SEPARATOR.getString().get();
    }

    public Formula process(final String input) throws ProcessorException {
        if (input == null || input.isBlank()) {
            throw new ProcessorException("Input formula cannot be null, empty or blank");
        }

        return new Formula(input, true, processFormula(removeAllSpaces(input)));
    }

    private String removeAllSpaces(final String input) {
        return input.replace(StringUtils.SPACE, StringUtils.EMPTY);
    }

    // must return an entity (3h/2)>>i
    private String processFormula(final String input) throws ProcessorException {
        int parenthesisOpen = input.lastIndexOf(StringUtils.PARENTHESIS_OPEN);
        int parenthesisClose = input.indexOf(StringUtils.PARENTHESIS_CLOSE, parenthesisOpen + 1);

        Entity entity;
        final String[] segments;
        final Entity[] entities;
        String block;
        if (parenthesisOpen > -1 && parenthesisClose > parenthesisOpen) {
            block = input.substring(parenthesisOpen + 1, parenthesisClose);
            if (block.isEmpty()) {
                segments = new String[0];
                entities = new Entity[0];
            } else {
                segments = block.split(StringUtils.SEMICOLON);
                entities = new Entity[segments.length];

                for (int i = 0; i < segments.length; ++i) {
                    if (!segments[i].isEmpty()) {
                        entities[i] = new FormulaProcessor(segments[i]).process();
                    }
                }
                if (segments.length == 1) {
                    block = segments[0];
                }
            }
        } else if (parenthesisOpen < 0 && parenthesisClose < 0) {
            entity = new FormulaProcessor(input).process();
            if (entity.isNumber()) {
                return stringify(entity.getValue());
            } else {
                return entity.toString();
            }
        } else {
            throw new ProcessorException("Parenthesis error");
        }

        final StringBuilder result = new StringBuilder();
        if (parenthesisOpen > 0) {
            final String prefix = input.substring(0, parenthesisOpen);
            final Optional<Functions> function = getFunction(prefix);
            if (function.isPresent()) {
                entity = new FunctionProcessor(function.get(), entities).process();
                result.append(input.substring(0, parenthesisOpen - function.get().getFunction().length()));
                result.append(entity);
            } else {
                result.append(prefix).append(block);
            }
        } else {
            result.append(block);
        }

        if (++parenthesisClose > -1 && parenthesisClose < input.length()) {
            result.append(input.substring(parenthesisClose, input.length()));
        }
        return processFormula(result.toString());
    }

    private String stringify(final double d) {
        double r = MathUtils.round(d, getPrecision());

        final String value = Double.toString(r);
        final int dot = value.indexOf('.');
        int length = dot + 1 + getPrecision();
        final String result;

        if (length > value.length()) {
            char[] chars = new char[length - value.length()];
            Arrays.fill(chars, '0');
            result = value + new String(chars);
        } else if (getPrecision() > 0) {
            result = value.substring(0, length);
        } else if (dot > -1) {
            result = value.substring(0, dot);
        } else {
            result = value;
        }

        return result;
    }

    private Optional<Functions> getFunction(final String input) throws ProcessorException {
        final char[] chars = input.toCharArray();
        final int len = chars.length;

        int pos = len;

        while (--pos >= 0 && Arrays.binarySearch(Functions.CHARS, chars[pos]) > -1) {
        }

        if (pos < len - 1) {
            final char[] inputFunction = Arrays.copyOfRange(chars, pos + 1, len);
            final Optional<Functions> function = Functions.check(inputFunction);
            if (function.isPresent()) {
                return function;
            } else {
                throw new ProcessorException("Function not found: {}", new String(inputFunction));
            }
        }

        return Optional.empty();
    }

    public static void main(String[] args) throws ProcessorException {
        MainProcessor processor = new MainProcessor();

        System.out.println(processor.processFormula("((3+2)*pow(9/abs(3);1-5))-2"));
        System.out.println(processor.processFormula("15in>>m/1000"));
        System.out.println(processor.processFormula("(15h+12s)>>his"));
        System.out.println(processor.processFormula("5K>>C"));
        System.out.println(processor.processFormula("5C>>K"));
        System.out.println(processor.processFormula("15/(1200/3937/12)"));
    }
}
