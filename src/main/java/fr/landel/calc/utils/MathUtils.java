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

    public static double round2(final double n, final double devider) {
        return n - Math.floor(n / devider) * devider;
    }

    public static void main(String[] args) {
        System.out.println(round2(12, 5.235));
        System.out.println(12 % 5.235);
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
}
