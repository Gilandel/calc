package fr.landel.calc.utils;

import java.util.function.Function;

import fr.landel.calc.processor.MainProcessor;

public final class MathUtils {

    private static final long TEN = 10;

    // PI/180
    private static final double DEGREE_CONVERTER = 0.017453292519943295D;

    private MathUtils() {
        throw new UnsupportedOperationException();
    }

    public static double fact(final Double n) {
        return (double) fact(n.longValue());
    }

    public static long fact(final long n) {
        if (n == 0L) {
            return 1L;
        } else {
            return n * fact(n - 1L);
        }
    }

    public static double pow10(final double n) {
        return Math.pow(TEN, n);
    }

    public static double round(final double n, final double accuracy) {
        final double x = pow10(accuracy);
        return Math.round(n * x) / x;
    }

    public static double ceil(final double n, final double accuracy) {
        final double x = pow10(accuracy);
        return Math.ceil(n * x) / x;
    }

    public static double floor(final double n, final double accuracy) {
        final double x = pow10(accuracy);
        return Math.floor(n * x) / x;
    }

    public static Function<Double, Double> applyAngularFunction(final Function<Double, Double> angularFunction) {
        return d -> {
            double angular = angularFunction.apply(d);
            if (!MainProcessor.isRadian()) {
                angular = angular * DEGREE_CONVERTER;
            }
            return angular;
        };
    }

    public static Function<Double, Double> applyInverseAngularFunction(final Function<Double, Double> angularFunction) {
        return d -> {
            double angular = angularFunction.apply(d);
            if (!MainProcessor.isRadian()) {
                angular = angular / DEGREE_CONVERTER;
            }
            return angular;
        };
    }

    public static boolean isEqualOrGreater(final double v1, final double v2, final int precision) {
        return v1 > v2 || isEqual(v1, v2, precision);
    }

    public static boolean isEqual(final double v1, final double v2, final int precision) {
        return Math.abs(v1 - v2) < 1d / MathUtils.pow10(precision);
    }
}
