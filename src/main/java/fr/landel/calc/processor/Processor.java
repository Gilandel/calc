package fr.landel.calc.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import fr.landel.calc.config.Conf;
import fr.landel.calc.config.Formula;
import fr.landel.calc.utils.Logger;
import fr.landel.calc.utils.StringUtils;
import fr.landel.calc.view.Functions;
import fr.landel.calc.view.I18n;

public class Processor {

    private static final Logger LOGGER = new Logger(Processor.class);

    public static final String ADD = "+";
    public static final String SUBSTRACT = "-";
    public static final String MULTIPLY = "*";
    public static final String DEVIDE = "/";
    public static final String MODULO = "%";
    public static final String POWER = "^";

    public static final String CONVERT = ">>";

    public static final char PARENTHESIS_OPEN = '(';
    public static final char PARENTHESIS_CLOSE = ')';

    private String decimalSeparator;
    private String thousandSeparator;

    public Processor() {
    }

    public void updateI18n() {
        this.decimalSeparator = Conf.DECIMAL_SEPARATOR.getString().get();
        this.thousandSeparator = Conf.THOUSAND_SEPARATOR.getString().get();
    }

    public Formula process(final String input) throws ProcessorException {
        if (input == null || input.isBlank()) {
            throw new ProcessorException("Input formula cannot be null, empty or blank");
        }

        return checkParenthesis(removeAllSpaces(input));
    }

    private String removeAllSpaces(final String input) {
        return input.replace(StringUtils.SPACE, StringUtils.EMPTY);
    }

    private Formula checkParenthesis(final String input) throws ProcessorException {
        final int parenthesisOpen = input.lastIndexOf(PARENTHESIS_OPEN);
        final int parenthesisClose = input.indexOf(PARENTHESIS_CLOSE, parenthesisOpen + 1);

        final String block;
        if (parenthesisOpen > -1 && parenthesisClose > parenthesisOpen) {
            block = input.substring(parenthesisOpen + 1, parenthesisClose);
        } else if (parenthesisOpen < 0 && parenthesisClose < 0) {
            block = input;
        } else {
            throw new ProcessorException("Parenthesis error");
        }

        final String[] segments = block.split(Functions.SEPARATOR);

        final Optional<Functions> function = getFunction(input, parenthesisOpen);

        final List<I18n> errors = new ArrayList<>();
        String result = "";
        if (function.isPresent()) {
            Functions f = function.get();

            errors.addAll(f.check(segments));

            if (errors.isEmpty() && f.getParamsCount() > 0 && f.getParams()[0].getConverter() != null) {
                Double d = (Double) f.getParams()[0].getConverter().apply(segments[0]);
                if (Functions.ABS.equals(f)) {
                    result = String.valueOf(Math.abs(d));
                }
            }
        }

        if (errors.isEmpty()) {
            return new Formula(input, true, result);
        } else {
            return new Formula(input, false, errors.stream().map(I18n::getI18n).collect(Collectors.joining(", ")));
        }
    }

    private Optional<Functions> getFunction(final String input, final int parenthesisOpen) {
        if (parenthesisOpen > 0) {
            int pos = parenthesisOpen - 1;
            char[] chars = input.toCharArray();
            while (pos >= 0 && Arrays.binarySearch(Functions.CHARS, chars[pos--]) > -1) {
            }

            if (pos + 1 < parenthesisOpen) {
                return Functions.check(Arrays.copyOfRange(chars, pos + 1, parenthesisOpen));
            }
        }
        return Optional.empty();
    }

    public static void main(String[] args) throws ProcessorException {
        Processor processor = new Processor();

        processor.process("test(12)");
        processor.process("abs(12)");
        processor.process("abs(-12)");
        processor.process("abs(-12.645555)");
    }
}
