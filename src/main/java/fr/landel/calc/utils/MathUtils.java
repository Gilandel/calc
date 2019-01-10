package fr.landel.calc.utils;

public class MathUtils {

    private static final long TEN = 10;

    private MathUtils() {
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
}
