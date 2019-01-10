package fr.landel.calc.processor;

import java.util.Arrays;
import java.util.Optional;

import fr.landel.calc.config.Conf;
import fr.landel.calc.config.Formula;
import fr.landel.calc.utils.MathUtils;
import fr.landel.calc.utils.StringUtils;
import fr.landel.calc.view.Functions;

public class MainProcessor {

    // TODO corriger detection des caraceteres avant fonction (2e())
    // TODO gerer radian / exact / scientific
    // TODO remonter les erreurs traduites

    public static final char PARENTHESIS_OPEN = '(';
    public static final char PARENTHESIS_CLOSE = ')';

    private String decimalSeparator = ".";
    private String thousandSeparator = ",";

    private boolean radian = true;
    private boolean exact = false;
    private boolean scientific = false;
    private int precision = 3;

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
                        entity = new FormulaProcessor(segments[i]).process();
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
                entity = new FunctionProcessor(function.get(), segments, radian).process();
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
        double r = MathUtils.round(d, precision);

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
        char[] chars = input.toCharArray();
        int len = chars.length;
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
