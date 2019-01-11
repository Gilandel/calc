package fr.landel.calc.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.landel.calc.config.I18n;
import fr.landel.calc.utils.StringUtils;

public class FunctionProcessor implements Processor {

    private static final String ERROR_FUNCTION = "the input function cannot be processed: {}({})";

    private final Functions function;
    private final Entity[] segments;

    public FunctionProcessor(final Functions function, final Entity[] segments) {
        this.function = function;
        this.segments = Arrays.copyOf(segments, segments.length);
    }

    @Override
    public Entity process() throws ProcessorException {

        final List<I18n> errors = new ArrayList<>();
        Entity result = null;

        errors.addAll(function.check(segments));

        if (errors.isEmpty()) {
            result = function.getProcessor().apply(segments);
        }

        if (result != null) {
            return result;
        } else {
            throw new ProcessorException(ERROR_FUNCTION, function.getFunction(), Arrays.stream(segments).map(String::valueOf).collect(StringUtils.SEMICOLON_JOINING_COLLECTOR));
        }
    }
}
