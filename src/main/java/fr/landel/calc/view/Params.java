package fr.landel.calc.view;

import java.util.Arrays;
import java.util.function.Predicate;

public class Params<T> {

    // TODO validation entity et plus string

    private static final char[] INTEGER = "0123456789+Ee".toCharArray();
    private static final char[] DECIMAL = "0123456789-+.Ee".toCharArray();
    static {
        Arrays.sort(INTEGER);
        Arrays.sort(DECIMAL);
    }

    private static final Predicate<String> IS_POSITIVE_DECIMAL = s -> !s.isEmpty() && s.charAt(0) != '-' && s.chars().allMatch(i -> Arrays.binarySearch(DECIMAL, (char) i) > -1);

    private static final Predicate<String> IS_DECIMAL = s -> {
        if (!s.isEmpty() && s.chars().allMatch(i -> Arrays.binarySearch(DECIMAL, (char) i) > -1)) {

            long dots = s.chars().filter(i -> i == '.').count();
            if (dots > 1) {
                return false;
            }

            long minus = s.chars().filter(i -> i == '-').count();
            return minus == 0 || minus == 1 && s.charAt(0) == '-';
        }
        return false;
    };

    private static final Predicate<String> IS_INTEGER = s -> {
        if (!s.isEmpty() && s.chars().allMatch(i -> Arrays.binarySearch(INTEGER, (char) i) > -1)) {

            long minus = s.chars().filter(i -> i == '-').count();
            return minus == 0 || minus == 1 && s.charAt(0) == '-';
        }
        return false;
    };

    public static final Params<Double> VALUE = new Params<>(I18n.DIALOG_FUNCTION_PARAM_VALUE, Double.class, IS_DECIMAL, I18n.DIALOG_ERROR_PARAM_VALUE);
    public static final Params<Double> ACCURACY = new Params<>(I18n.DIALOG_FUNCTION_PARAM_ACCURACY, Double.class, IS_POSITIVE_DECIMAL, I18n.DIALOG_ERROR_PARAM_ACCURACY);
    public static final Params<Double> ANGULAR = new Params<>(I18n.DIALOG_FUNCTION_PARAM_ANGULAR, Double.class, IS_DECIMAL, I18n.DIALOG_ERROR_PARAM_ANGULAR);
    public static final Params<Double> DATE = new Params<>(I18n.DIALOG_FUNCTION_PARAM_DATE, Double.class, IS_INTEGER, I18n.DIALOG_ERROR_PARAM_DATE);
    public static final Params<Double> COSINUS = new Params<>(I18n.DIALOG_FUNCTION_PARAM_COSINUS, Double.class, IS_DECIMAL, I18n.DIALOG_ERROR_PARAM_COSINUS);
    public static final Params<Double> SINUS = new Params<>(I18n.DIALOG_FUNCTION_PARAM_SINUS, Double.class, IS_DECIMAL, I18n.DIALOG_ERROR_PARAM_SINUS);
    public static final Params<Double> TANGENT = new Params<>(I18n.DIALOG_FUNCTION_PARAM_TANGENT, Double.class, IS_DECIMAL, I18n.DIALOG_ERROR_PARAM_TANGENT);
    public static final Params<Double> EXPONENT = new Params<>(I18n.DIALOG_FUNCTION_PARAM_EXPONENT, Double.class, IS_DECIMAL, I18n.DIALOG_ERROR_PARAM_EXPONENT);

    private final I18n i18n;
    private final Class<T> type;
    private final Predicate<String> validator;
    private final I18n predicateI18n;

    public Params(final I18n i18n, final Class<T> type, final Predicate<String> validator, final I18n predicateI18n) {
        this.i18n = i18n;
        this.type = type;
        this.validator = validator;
        this.predicateI18n = predicateI18n;
    }

    public I18n getI18n() {
        return this.i18n;
    }

    public Class<T> getType() {
        return this.type;
    }

    public Predicate<String> getValidator() {
        return this.validator;
    }

    public I18n getPredicateI18n() {
        return this.predicateI18n;
    }
}
