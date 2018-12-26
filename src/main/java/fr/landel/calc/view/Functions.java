package fr.landel.calc.view;

import java.util.Optional;

import fr.landel.calc.utils.StringUtils;

public enum Functions {
    ABS("abs", I18n.DIALOG_FUNCTION_PARAM_VALUE, I18n.DIALOG_FUNCTION_ABS),
    ACOS("acos", I18n.DIALOG_FUNCTION_PARAM_COSINUS, I18n.DIALOG_FUNCTION_ACOS),
    ASIN("asin", I18n.DIALOG_FUNCTION_PARAM_SINUS, I18n.DIALOG_FUNCTION_ASIN),
    ATAN("atan", I18n.DIALOG_FUNCTION_PARAM_TANGENT, I18n.DIALOG_FUNCTION_ATAN),
    CEIL("ceil", I18n.DIALOG_FUNCTION_PARAM_VALUE, I18n.DIALOG_FUNCTION_PARAM_ACCURACY, I18n.DIALOG_FUNCTION_CEIL),
    COS("cos", I18n.DIALOG_FUNCTION_PARAM_ANGULAR, I18n.DIALOG_FUNCTION_COS),
    EXP("exp", I18n.DIALOG_FUNCTION_PARAM_VALUE, I18n.DIALOG_FUNCTION_EXP),
    FACT("fact", I18n.DIALOG_FUNCTION_PARAM_VALUE, I18n.DIALOG_FUNCTION_FACT),
    FLOOR("floor", I18n.DIALOG_FUNCTION_PARAM_VALUE, I18n.DIALOG_FUNCTION_PARAM_ACCURACY, I18n.DIALOG_FUNCTION_FLOOR),
    LOG("log", I18n.DIALOG_FUNCTION_PARAM_VALUE, I18n.DIALOG_FUNCTION_LOG),
    LN("ln", I18n.DIALOG_FUNCTION_PARAM_VALUE, I18n.DIALOG_FUNCTION_LN),
    PI("pi"),
    POW("pow", I18n.DIALOG_FUNCTION_PARAM_VALUE, I18n.DIALOG_FUNCTION_PARAM_EXPONENT, I18n.DIALOG_FUNCTION_POW),
    ROUND("round", I18n.DIALOG_FUNCTION_PARAM_VALUE, I18n.DIALOG_FUNCTION_PARAM_ACCURACY, I18n.DIALOG_FUNCTION_ROUND),
    SIN("sin", I18n.DIALOG_FUNCTION_PARAM_VALUE, I18n.DIALOG_FUNCTION_SIN),
    SQR("sqr", I18n.DIALOG_FUNCTION_PARAM_VALUE, I18n.DIALOG_FUNCTION_SQR),
    TAN("tan", I18n.DIALOG_FUNCTION_PARAM_VALUE, I18n.DIALOG_FUNCTION_TAN);

    private final String function;
    private final Optional<I18n> param1;
    private final Optional<I18n> param2;
    private final I18n i18n;
    private final String injectable;
    private String toString;
    private String focusParam1;
    private String focusParam2;

    private Functions(final String function, final I18n param1, final I18n param2, final I18n i18n) {
        this.function = function;
        this.param1 = Optional.ofNullable(param1);
        this.param2 = Optional.ofNullable(param2);
        this.i18n = i18n;

        final StringBuilder builderInjectable = new StringBuilder(function).append("(");
        this.param1.ifPresent(t -> builderInjectable.append("{}"));
        this.param2.ifPresent(t -> builderInjectable.append("; {}"));
        this.injectable = builderInjectable.append(")").toString();

        this.updateI18n();

        if (i18n != null) {
            i18n.addUpdateListener(c -> this.updateI18n());
        }
    }

    private Functions(final String function, final I18n param1, final I18n i18n) {
        this(function, param1, null, i18n);
    }

    private Functions(final String function, final I18n i18n) {
        this(function, null, null, i18n);
    }

    private Functions(final String function) {
        this(function, null, null, null);
    }

    public String getFunction() {
        return this.function;
    }

    public boolean hasParams() {
        return this.param1.isPresent();
    }

    public Optional<I18n> getParam1() {
        return this.param1;
    }

    public Optional<I18n> getParam2() {
        return this.param2;
    }

    public I18n getI18n() {
        return this.i18n;
    }

    public String getFocusParam1() {
        return this.focusParam1;
    }

    public String getFocusParam2() {
        return this.focusParam2;
    }

    public String inject(String... params) {
        return StringUtils.inject(this.injectable, params);
    }

    public void updateI18n() {
        final StringBuilder builder = new StringBuilder(function).append("(");
        final StringBuilder builderFocus1 = new StringBuilder("<html>").append(function).append("(");
        final StringBuilder builderFocus2 = new StringBuilder("<html>").append(function).append("(");
        this.param1.ifPresent(t -> {
            builder.append(t.getI18n());
            builderFocus1.append("<u><b>").append(t.getI18n()).append("</b></u>");
            builderFocus2.append(t.getI18n());
        });
        this.param2.ifPresent(t -> {
            builder.append("; ").append(t.getI18n());
            builderFocus1.append("; ").append(t.getI18n());
            builderFocus2.append("; <u><b>").append(t.getI18n()).append("</b></u>");
        });
        this.toString = builder.append(")").toString();
        this.focusParam1 = builderFocus1.append(")</html>").toString();
        this.focusParam2 = builderFocus2.append(")</html>").toString();
    }

    @Override
    public String toString() {
        return this.toString;
    }
}
