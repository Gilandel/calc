package fr.landel.calc.processor;

import java.util.function.Predicate;

import fr.landel.calc.config.I18n;

public class Params<T> {

    public static final Params<Double> VALUE = new Params<>(I18n.DIALOG_FUNCTION_PARAM_VALUE, Entity::isNumber, I18n.DIALOG_ERROR_PARAM_VALUE);
    public static final Params<Double> ACCURACY = new Params<>(I18n.DIALOG_FUNCTION_PARAM_ACCURACY, Entity::isPositiveDecimal, I18n.DIALOG_ERROR_PARAM_ACCURACY);
    public static final Params<Double> ANGULAR = new Params<>(I18n.DIALOG_FUNCTION_PARAM_ANGULAR, Entity::isNumber, I18n.DIALOG_ERROR_PARAM_ANGULAR);
    public static final Params<Double> DATE = new Params<>(I18n.DIALOG_FUNCTION_PARAM_DATE, Entity::isInteger, I18n.DIALOG_ERROR_PARAM_DATE);
    public static final Params<Double> COSINUS = new Params<>(I18n.DIALOG_FUNCTION_PARAM_COSINUS, Entity::isNumber, I18n.DIALOG_ERROR_PARAM_COSINUS);
    public static final Params<Double> SINUS = new Params<>(I18n.DIALOG_FUNCTION_PARAM_SINUS, Entity::isNumber, I18n.DIALOG_ERROR_PARAM_SINUS);
    public static final Params<Double> TANGENT = new Params<>(I18n.DIALOG_FUNCTION_PARAM_TANGENT, Entity::isNumber, I18n.DIALOG_ERROR_PARAM_TANGENT);
    public static final Params<Double> EXPONENT = new Params<>(I18n.DIALOG_FUNCTION_PARAM_EXPONENT, Entity::isNumber, I18n.DIALOG_ERROR_PARAM_EXPONENT);
    public static final Params<String> UNITY = new Params<>(I18n.DIALOG_FUNCTION_PARAM_UNITY, e -> e.isUnity(UnityType.DATE), I18n.DIALOG_ERROR_PARAM_EXPONENT);

    private final I18n i18n;
    private final Predicate<Entity> validator;
    private final I18n predicateI18n;

    public Params(final I18n i18n, final Predicate<Entity> validator, final I18n predicateI18n) {
        this.i18n = i18n;
        this.validator = validator;
        this.predicateI18n = predicateI18n;
    }

    public I18n getI18n() {
        return this.i18n;
    }

    public Predicate<Entity> getValidator() {
        return this.validator;
    }

    public I18n getPredicateI18n() {
        return this.predicateI18n;
    }
}
