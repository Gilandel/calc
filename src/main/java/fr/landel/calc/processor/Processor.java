package fr.landel.calc.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import fr.landel.calc.config.Conf;
import fr.landel.calc.config.Formula;
import fr.landel.calc.utils.Logger;
import fr.landel.calc.utils.StringUtils;
import fr.landel.calc.view.Functions;
import fr.landel.calc.view.I18n;

public class Processor {

    private static final Logger LOGGER = new Logger(Processor.class);

    public static final char ADD = '+';
    public static final char SUBSTRACT = '-';
    public static final char MULTIPLY = '*';
    public static final char DEVIDE = '/';
    public static final char MODULO = '%';
    public static final char POWER = '^';
    public static final char CONVERT = '>';

    private static final char MAX_OPERATOR;
    private static final Character[] OPERATORS_PRIORITIES = {CONVERT, POWER, MULTIPLY, DEVIDE, MODULO, ADD, SUBSTRACT};
    private static final Character[] OPERATORS = Arrays.copyOf(OPERATORS_PRIORITIES, OPERATORS_PRIORITIES.length);
    static {
        Arrays.sort(OPERATORS);
        MAX_OPERATOR = OPERATORS[OPERATORS.length - 1];
    }

    public static final char PARENTHESIS_OPEN = '(';
    public static final char PARENTHESIS_CLOSE = ')';

    private static final Collector<CharSequence, ?, String> ERRORS_COLLECTOR = Collectors.joining(", ");

    private String decimalSeparator = ".";
    private String thousandSeparator = ",";

    private boolean radian = true;
    private boolean exact = false;
    private boolean scientific = false;
    private int precision = 3;

    // PI/180
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

        processFormula(removeAllSpaces(input));

        return null;
    }

    private String removeAllSpaces(final String input) {
        return input.replace(StringUtils.SPACE, StringUtils.EMPTY);
    }

    private double applyAngularFunction(final Functions function, final double d, final Function<Double, Double> angularFunction) {
        double angular = angularFunction.apply(d);
        if (!radian) {
            angular = angular * DEGREE_CONVERTER;
        }
        return angular;
    }

    private double applyInverseAngularFunction(final Functions function, final double d, final Function<Double, Double> angularFunction) {
        double angular = angularFunction.apply(d);
        if (!radian) {
            angular = angular / DEGREE_CONVERTER;
        }
        return angular;
    }

    private String processFormula(final String input) throws ProcessorException {
        int parenthesisOpen = input.lastIndexOf(PARENTHESIS_OPEN);
        int parenthesisClose = input.indexOf(PARENTHESIS_CLOSE, parenthesisOpen + 1);

        final String[] segments;
        String block;
        if (parenthesisOpen > -1 && parenthesisClose > parenthesisOpen) {
            block = input.substring(parenthesisOpen + 1, parenthesisClose);
            segments = block.split(Functions.SEPARATOR);
            for (int i = 0; i < segments.length; ++i) {
                segments[i] = this.processBlock(segments[i]);
            }
        } else if (parenthesisOpen < 0 && parenthesisClose < 0) {
            return processBlock(input);
        } else {
            throw new ProcessorException("Parenthesis error");
        }

        final StringBuilder result = new StringBuilder();
        if (parenthesisOpen > 0) {
            final String prefix = input.substring(0, parenthesisOpen);
            Optional<Functions> function = getFunction(prefix);
            if (function.isPresent()) {
                block = processFunction(function, segments);
                result.append(input.substring(0, parenthesisOpen - function.get().getFunction().length()));
                result.append(block);
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

    private String processCalc(final String v1, final String v2, final char operator) throws ProcessorException {
        return null;
    }

    private String[] processSegment(final String[] segments, final char operator) throws ProcessorException {

        boolean move = false;
        for (int i = 0; i < segments.length; ++i) {
            if (segments[i].length() == 1) {
                char c = segments[i].charAt(0);
                if (c == operator && !move) {
                    if (i > 0 && i < segments.length) {
                        segments[i - 1] = processCalc(segments[i - 1], segments[i + 1], operator);
                        i += 2;
                        move = true;
                    }
                }
            }

            if (move) {
                segments[i - 2] = segments[i];
            }
        }

        if (move) {
            return Arrays.copyOf(segments, segments.length - 2);
        } else {
            throw new ProcessorException("");
        }
    }

    private String processBlock(final String block) throws ProcessorException {
        final char[] chars = block.toCharArray();
        String[] output = new String[chars.length];

        int[] count = new int[MAX_OPERATOR];

        int max = 0;
        int previous = 0;
        boolean isPreviousOperator = true;
        boolean isPreviousParenthesisOpen = false;
        for (int i = 0; i < chars.length; ++i) {
            if (Arrays.binarySearch(OPERATORS, chars[i]) > -1) {
                if (i > 0) {
                    output[max++] = new String(Arrays.copyOfRange(chars, previous, i));
                    output[max++] = String.valueOf(chars[i]);
                    ++count[chars[i]];

                } else if (chars[i] == SUBSTRACT && (isPreviousOperator || isPreviousParenthesisOpen)) {
                    output[max++] = new String(Arrays.copyOfRange(chars, previous, i + 1));

                } else {
                    output[max++] = String.valueOf(chars[i]);
                    ++count[chars[i]];
                }
                isPreviousOperator = true;
                isPreviousParenthesisOpen = false;
                previous = i + 1;
            } else {
                if (chars[i] == '(') {
                    isPreviousParenthesisOpen = true;
                } else {
                    isPreviousParenthesisOpen = false;
                }
                isPreviousOperator = false;
            }
        }
        if (previous <= chars.length) {
            output[max++] = new String(Arrays.copyOfRange(chars, previous, chars.length));
        }

        String[] segments = Arrays.copyOf(output, max);
        for (int j = 0; j < OPERATORS_PRIORITIES.length; ++j) {
            for (int i = 0; i < count[j]; ++i) {
                segments = processSegment(segments, (char) i);
            }
        }

        return segments[0];
    }

    public static void main(String[] args) throws ProcessorException {
        Processor processor = new Processor();

        System.out.println(processor.processFormula("((3+2)*pow(9/abs(3);1-5))-2"));
    }

    private String processFunction(final Optional<Functions> function, final String[] segments) throws ProcessorException {
        final List<I18n> errors = new ArrayList<>();
        double result = 0;
        if (function.isPresent()) {
            final Functions f = function.get();

            errors.addAll(f.check(segments));

            if (errors.isEmpty()) {
                if (f.getParamsCount() == 1) {
                    if (f.getParams()[0].getConverter() != null) {

                        final Double d = (Double) f.getParams()[0].getConverter().apply(segments[0]);

                        switch (f) {
                        case ABS:
                            result = Math.abs(d);
                            break;
                        case ACOS:
                            result = applyInverseAngularFunction(f, d, Math::acos);
                            break;
                        case ASIN:
                            result = applyInverseAngularFunction(f, d, Math::asin);
                            break;
                        case ATAN:
                            result = applyInverseAngularFunction(f, d, Math::atan);
                            break;
                        case COS:
                            result = applyAngularFunction(f, d, Math::cos);
                            break;
                        case SIN:
                            result = applyAngularFunction(f, d, Math::sin);
                            break;
                        case TAN:
                            result = applyAngularFunction(f, d, Math::tan);
                            break;
                        case EXP:
                            result = Math.exp(d);
                            break;
                        case LN:
                            result = Math.log(d);
                            break;
                        case LOG:
                            result = Math.log10(d);
                            break;
                        case SQR:
                            result = Math.sqrt(d);
                            break;
                        case FACT:
                            result = Processor.fact(d.longValue());
                            break;
                        case FLOOR:
                            result = Math.floor(d);
                            break;
                        case CEIL:
                            result = Math.ceil(d);
                            break;
                        case ROUND:
                            result = Math.round(d);
                            break;
                        default:
                        }
                    }
                } else if (f.getParamsCount() == 2) {
                    if (f.getParams()[0].getConverter() != null && f.getParams()[1].getConverter() != null) {

                        final Double d1 = (Double) f.getParams()[0].getConverter().apply(segments[0]);
                        final Double d2 = (Double) f.getParams()[1].getConverter().apply(segments[1]);

                        switch (f) {
                        case POW:
                            result = Math.pow(d1, d2);
                            break;
                        default:
                        }
                    }
                } else if (f.getParamsCount() == 0) {

                    switch (f) {
                    case PI:
                        result = Math.PI;
                        break;
                    default:
                    }
                }
            }
        }

        return stringify(result);
    }

    private static long fact(long n) {
        if (n == 0L) {
            return 1L;
        } else {
            return n * fact(n - 1L);
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

    private Optional<Functions> getFunction(final String input) throws ProcessorException {
        int len = input.length();
        int pos = len;
        char[] chars = input.toCharArray();
        while (pos >= 0 && Arrays.binarySearch(Functions.CHARS, chars[--pos]) > -1) {
        }

        if (pos + 1 < len) {
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
}
