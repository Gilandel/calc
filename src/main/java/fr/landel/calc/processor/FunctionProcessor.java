package fr.landel.calc.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import fr.landel.calc.utils.MathUtils;
import fr.landel.calc.view.Functions;
import fr.landel.calc.view.I18n;

public class FunctionProcessor implements Processor {

    // TODO utiliser entoty pour les parametres et ajouter fonction isDecimal
    // TODO isPositive (1E-5)

    private static final String ERROR_FUNCTION_ZERO = "The input function cannot be processed: {}()";
    private static final String ERROR_FUNCTION_ONE = "The input function cannot be processed: {}({})";
    private static final String ERROR_FUNCTION_TWO = "The input function cannot be processed: {}({}; {})";
    private static final String ERROR_FUNCTION = "The input function cannot be processed: {}({})";

    private final Functions function;
    private final String[] segments;
    private final boolean radian;

    // PI/180
    private static final double DEGREE_CONVERTER = 0.017453292519943295D;

    public FunctionProcessor(final Functions function, final String[] segments, final boolean radian) {
        this.function = function;
        this.segments = Arrays.copyOf(segments, segments.length);
        this.radian = radian;
    }

    @Override
    public Entity process() throws ProcessorException {

        final List<I18n> errors = new ArrayList<>();
        Entity result = null;

        errors.addAll(function.check(segments));

        if (errors.isEmpty()) {
            if (function.getParamsCount() == 1) {
                result = callFunctionOneParam();

            } else if (function.getParamsCount() == 2) {
                result = callFunctionTwoParams();

            } else if (function.getParamsCount() == 0) {
                result = callFunctionZeroParam();
            }
        }

        if (result != null) {
            return result;
        } else {
            throw new ProcessorException(ERROR_FUNCTION, function.getFunction(), Arrays.stream(segments).collect(Collectors.joining("; ")));
        }
    }

    private Entity callFunctionZeroParam() throws ProcessorException {
        Double result = null;

        switch (function) {
        case PI:
            result = Math.PI;
            break;
        case E:
            result = Math.E;
            break;
        case RANDOM:
            result = Math.random();
            break;
        default:
        }

        if (result != null) {
            return new Entity(0, result, Unity.NUMBER);
        } else {
            throw new ProcessorException(ERROR_FUNCTION_ZERO, function.getFunction());
        }
    }

    private Entity callFunctionOneParam() throws ProcessorException {
        final Entity entity = new FormulaProcessor(segments[0]).process();

        final Double d = entity.getValue();
        Double result = null;

        switch (function) {
        case ABS:
            result = Math.abs(d);
            break;
        case ACOS:
            result = applyInverseAngularFunction(function, d, Math::acos);
            break;
        case ASIN:
            result = applyInverseAngularFunction(function, d, Math::asin);
            break;
        case ATAN:
            result = applyInverseAngularFunction(function, d, Math::atan);
            break;
        case COS:
            result = applyAngularFunction(function, d, Math::cos);
            break;
        case SIN:
            result = applyAngularFunction(function, d, Math::sin);
            break;
        case TAN:
            result = applyAngularFunction(function, d, Math::tan);
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
            result = (double) MathUtils.fact(d.longValue());
            break;
        default:
        }

        if (result != null) {
            return new Entity(0, result, entity.getUnities());
        } else {
            throw new ProcessorException(ERROR_FUNCTION_ONE, function.getFunction(), entity);
        }
    }

    private Entity callFunctionTwoParams() throws ProcessorException {

        final Entity entity1 = new FormulaProcessor(segments[0]).process();
        final Entity entity2 = new FormulaProcessor(segments[1]).process();

        final Double d1 = entity1.getValue();
        final Double d2 = entity2.getValue();
        Entity resultEntity = null;
        Double result = null;

        switch (function) {
        case POW:
            resultEntity = Operators.POWER.process(entity1, entity2);
            break;
        case FLOOR:
            result = MathUtils.floor(d1, d2);
            break;
        case CEIL:
            result = MathUtils.ceil(d1, d2);
            break;
        case ROUND:
            result = MathUtils.round(d1, d2);
            break;
        default:
        }

        if (resultEntity != null) {
            return resultEntity;
        } else if (result != null) {
            return new Entity(entity1.getIndex(), result, entity1.getUnities());
        } else {
            throw new ProcessorException(ERROR_FUNCTION_TWO, function.getFunction(), entity1, entity2);
        }
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
}
