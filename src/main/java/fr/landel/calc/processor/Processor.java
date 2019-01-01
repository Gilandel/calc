package fr.landel.calc.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
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

    public static final String CONVERT = ">";

    public static final char PARENTHESIS_OPEN = '(';
    public static final char PARENTHESIS_CLOSE = ')';

    private static final String ERROR_SEPARATOR = ", ";

    private String decimalSeparator = ".";
    private String thousandSeparator = ",";

    private boolean radian = true;
    private boolean exact = false;
    private boolean scientific = false;
    private int precision = 3;

    private static final double DEGREE_CONVERTER = 0.017453292519943295D;

    public Processor() {
    }

    public void setRadian(boolean radian) {
        this.radian = radian;
    }

    public void setExact(boolean exact) {
        this.exact = exact;
    }

    public void setScientific(boolean scientific) {
        this.scientific = scientific;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
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

    private String applyAngularFunction(final Functions function, final double d, final Function<Double, Double> angularFunction) {
        double angular = angularFunction.apply(d);
        if (!radian) {
            angular = angular * DEGREE_CONVERTER;
        }
        return stringify(angular);
    }

    private String applyInverseAngularFunction(final Functions function, final double d, final Function<Double, Double> angularFunction) {
        double angular = angularFunction.apply(d);
        if (!radian) {
            angular = angular / DEGREE_CONVERTER;
        }
        return stringify(angular);
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
        String result = StringUtils.EMPTY;
        if (function.isPresent()) {
            Functions f = function.get();

            errors.addAll(f.check(segments));

            if (errors.isEmpty()) {
                if (f.getParamsCount() == 1) {
                    if (f.getParams()[0].getConverter() != null) {

                        final Double d = (Double) f.getParams()[0].getConverter().apply(segments[0]);

                        if (Functions.ABS.equals(f)) {
                            result = stringify(Math.abs(d));
                        } else if (Functions.ACOS.equals(f)) {
                            result = applyInverseAngularFunction(f, d, Math::acos);
                        } else if (Functions.ASIN.equals(f)) {
                            result = applyInverseAngularFunction(f, d, Math::asin);
                        } else if (Functions.ATAN.equals(f)) {
                            result = applyInverseAngularFunction(f, d, Math::atan);
                        } else if (Functions.COS.equals(f)) {
                            result = applyAngularFunction(f, d, Math::cos);
                        } else if (Functions.SIN.equals(f)) {
                            result = applyAngularFunction(f, d, Math::sin);
                        } else if (Functions.TAN.equals(f)) {
                            result = applyAngularFunction(f, d, Math::tan);
                        }
                    }
                } else if (f.getParamsCount() == 0) {
                    if (Functions.PI.equals(f)) {
                        result = stringify(Math.PI);
                    }
                }
            }
        }

        if (errors.isEmpty()) {
            return new Formula(input, true, result);
        } else {
            return new Formula(input, false, errors.stream().map(I18n::getI18n).collect(Collectors.joining(ERROR_SEPARATOR)));
        }
    }

    private String stringify(final double d) {
        double pow = Math.pow(10, precision);
        double r = Math.round(d * pow) / pow;

        final String value = Double.toString(r);
        final int dot = value.indexOf('.');
        int length = dot + 1 + precision;
        final String result;

        if (length > value.length()) {
            char[] chars = new char[length - value.length()];
            Arrays.fill(chars, '0');
            result = value + new String(chars);
        } else if (precision > 0) {
            result = value.substring(0, length);
        } else if (dot > -1) {
            result = value.substring(0, dot);
        } else {
            result = value;
        }

        return result;
    }

    private Optional<Functions> getFunction(final String input, final int parenthesisOpen) throws ProcessorException {
        if (parenthesisOpen > 0) {
            int pos = parenthesisOpen - 1;
            char[] chars = input.toCharArray();
            while (pos >= 0 && Arrays.binarySearch(Functions.CHARS, chars[pos--]) > -1) {
            }

            if (pos + 1 < parenthesisOpen) {
                final char[] inputFunction = Arrays.copyOfRange(chars, pos + 1, parenthesisOpen);
                final Optional<Functions> function = Functions.check(inputFunction);
                if (function.isPresent()) {
                    return function;
                } else {
                    throw new ProcessorException("Function not found: {}", new String(inputFunction));
                }
            }
        }
        return Optional.empty();
    }
}
