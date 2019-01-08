package fr.landel.calc.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import fr.landel.calc.config.Conf;
import fr.landel.calc.config.Formula;
import fr.landel.calc.utils.StringUtils;
import fr.landel.calc.view.Functions;
import fr.landel.calc.view.I18n;

public class MainProcessor {

    public static final char PARENTHESIS_OPEN = '(';
    public static final char PARENTHESIS_CLOSE = ')';

    private String decimalSeparator = ".";
    private String thousandSeparator = ",";

    private boolean radian = true;
    private boolean exact = false;
    private boolean scientific = false;
    private int precision = 3;

    // PI/180
    private static final double DEGREE_CONVERTER = 0.017453292519943295D;

    public MainProcessor() {
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

        return new Formula(input, true, processFormula(removeAllSpaces(input)));
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

        Entity entity;
        final String[] segments;
        String block;
        if (parenthesisOpen > -1 && parenthesisClose > parenthesisOpen) {
            block = input.substring(parenthesisOpen + 1, parenthesisClose);
            if (block.isEmpty()) {
                segments = new String[0];
            } else {
                segments = block.split(Functions.SEPARATOR);
                for (int i = 0; i < segments.length; ++i) {
                    if (!segments[i].isEmpty()) {
                        entity = new SimpleFormulaProcessor(segments[i]).process();
                        if (entity.isNumber()) {
                            segments[i] = stringify(entity.getValue());
                        } else {
                            segments[i] = entity.toString();
                        }
                    }
                }
                if (segments.length == 1) {
                    block = segments[0];
                }
            }
        } else if (parenthesisOpen < 0 && parenthesisClose < 0) {
            entity = new SimpleFormulaProcessor(input).process();
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

    private String processFunction(final Optional<Functions> function, final String[] segments) throws ProcessorException {
        final List<I18n> errors = new ArrayList<>();
        double result = 0;
        if (function.isPresent()) {
            final Functions f = function.get();

            errors.addAll(f.check(segments));

            if (errors.isEmpty()) {
                if (f.getParamsCount() == 1) {
                    if (f.getParams()[0].getConverter() != null) {

                        final Double d = new SimpleFormulaProcessor(segments[0]).process().getValue();

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
                            result = MainProcessor.fact(d.longValue());
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

                        final Double d1 = new SimpleFormulaProcessor(segments[0]).process().getValue();
                        final Double d2 = new SimpleFormulaProcessor(segments[1]).process().getValue();

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

        // System.out.println(processor.processFormula("((3+2)*pow(9/abs(3);1-5))-2"));
        System.out.println(processor.processFormula("15in>>m/1000"));
        System.out.println(processor.processFormula("(15h+12s)>>his"));
        System.out.println(processor.processFormula("5K>>C"));
        System.out.println(processor.processFormula("5C>>K"));
        System.out.println(processor.processFormula("15/(1200/3937/12)"));
    }
}
