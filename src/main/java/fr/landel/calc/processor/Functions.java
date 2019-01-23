package fr.landel.calc.processor;

import java.time.LocalDateTime;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import fr.landel.calc.config.I18n;
import fr.landel.calc.utils.DateUtils;
import fr.landel.calc.utils.MathUtils;
import fr.landel.calc.utils.StringUtils;

public enum Functions implements FunctionConstants {

    ABS("abs", I18n.DIALOG_FUNCTION_ABS, ONE_PARAM.apply(Math::abs), Params.VALUE),
    ACOS("acos", I18n.DIALOG_FUNCTION_ACOS, ONE_PARAM.apply(MathUtils.applyInverseAngularFunction(Math::acos)), Params.COSINUS),
    ASIN("asin", I18n.DIALOG_FUNCTION_ASIN, ONE_PARAM.apply(MathUtils.applyInverseAngularFunction(Math::asin)), Params.SINUS),
    ATAN("atan", I18n.DIALOG_FUNCTION_ATAN, ONE_PARAM.apply(MathUtils.applyInverseAngularFunction(Math::atan)), Params.TANGENT),
    CEIL("ceil", I18n.DIALOG_FUNCTION_CEIL, TWO_PARAM.apply(MathUtils::ceil), Params.VALUE, Params.ACCURACY),
    COS("cos", I18n.DIALOG_FUNCTION_COS, ONE_PARAM.apply(MathUtils.applyAngularFunction(Math::cos)), Params.ANGULAR),
    EXP("exp", I18n.DIALOG_FUNCTION_EXP, ONE_PARAM.apply(Math::exp), Params.VALUE),
    FACT("fact", I18n.DIALOG_FUNCTION_FACT, ONE_PARAM.apply(MathUtils::fact), Params.VALUE),
    FLOOR("floor", I18n.DIALOG_FUNCTION_FLOOR, TWO_PARAM.apply(MathUtils::floor), Params.VALUE, Params.ACCURACY),
    LOG("log", I18n.DIALOG_FUNCTION_LOG, ONE_PARAM.apply(Math::log10), Params.VALUE),
    LN("ln", I18n.DIALOG_FUNCTION_LN, ONE_PARAM.apply(Math::log), Params.VALUE),
    PI("pi", NO_PARAM.apply(() -> Math.PI)),
    E("e", NO_PARAM.apply(() -> Math.E)),
    RANDOM("rand", NO_PARAM.apply(Math::random)),
    POW("pow", I18n.DIALOG_FUNCTION_POW, TWO_PARAM.apply(Math::pow), Params.VALUE, Params.EXPONENT),
    ROUND("round", I18n.DIALOG_FUNCTION_ROUND, TWO_PARAM.apply(MathUtils::round), Params.VALUE, Params.ACCURACY),
    SIN("sin", I18n.DIALOG_FUNCTION_SIN, ONE_PARAM.apply(MathUtils.applyAngularFunction(Math::sin)), Params.ANGULAR),
    SQR("sqr", I18n.DIALOG_FUNCTION_SQR, ONE_PARAM.apply(Math::sqrt), Params.VALUE),
    TAN("tan", I18n.DIALOG_FUNCTION_TAN, ONE_PARAM.apply(MathUtils.applyAngularFunction(Math::tan)), Params.ANGULAR),

    // FIXME date, 3>>M 2019Y>>y
    YEARS("year", I18n.DIALOG_FUNCTION_YEAR, e -> new Entity(e[0].getIndex(), Double.valueOf(e[0].getDate().get().getYear())), Params.DATE_DATE),
    MONTH(
            "month",
            I18n.DIALOG_FUNCTION_MONTH,
            e -> new Entity(e[0].getIndex(), Double.valueOf(e[0].getDate().get().getMonthValue())),
            Params.DATE_DATE),
    WEEK(
            "week",
            I18n.DIALOG_FUNCTION_WEEK,
            e -> new Entity(e[0].getIndex(), Double.valueOf(e[0].getDate().get().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR))),
            Params.DATE_DATE),
    DAY("day", I18n.DIALOG_FUNCTION_DAY, e -> new Entity(e[0].getIndex(), Double.valueOf(e[0].getDate().get().getDayOfMonth())), Params.DATE_DATE),
    HOURS("hour", I18n.DIALOG_FUNCTION_HOURS, e -> new Entity(e[0].getIndex(), Double.valueOf(e[0].getDate().get().getHour())), Params.DATE_DATE),
    MINUTES(
            "minute",
            I18n.DIALOG_FUNCTION_MINUTES,
            e -> new Entity(e[0].getIndex(), Double.valueOf(e[0].getDate().get().getMinute())),
            Params.DATE_DATE),
    SECONDS(
            "second",
            I18n.DIALOG_FUNCTION_SECONDS,
            e -> new Entity(e[0].getIndex(), Double.valueOf(e[0].getDate().get().getSecond())),
            Params.DATE_DATE),
    MILLISECONDS(
            "millisecond",
            I18n.DIALOG_FUNCTION_MILLISECONDS,
            e -> new Entity(e[0].getIndex(), Math.floor(e[0].getDate().get().getNano() / DateUtils.NANO_PER_MILLISECOND)),
            Params.DATE_DATE),
    MICROSECONDS(
            "microsecond",
            I18n.DIALOG_FUNCTION_MICROSECONDS,
            e -> new Entity(e[0].getIndex(), Math.floor(e[0].getDate().get().getNano() / DateUtils.NANO_PER_MICROSECOND))),
    NANOSECONDS(
            "nanosecond",
            I18n.DIALOG_FUNCTION_NANOSECONDS,
            e -> new Entity(e[0].getIndex(), Double.valueOf(e[0].getDate().get().getNano())),
            Params.DATE_DATE),
    NOW("now", e -> {
        return new Entity(0, System.currentTimeMillis() * DateUtils.NANO_PER_MILLISECOND + DateUtils.NANO_1970, LocalDateTime.now(),
                Unity.DATE_NANOSECONDS);
    });

    public static final int MAX_PARAMS = Arrays.stream(Functions.values()).map(f -> f.getParamsCount()).max(Integer::compareTo).orElse(0);

    private static final String TAG_HTML_OPEN = "<html>";
    private static final String TAG_HTML_CLOSE = "</html>";
    private static final String TAG_HIGHLIGHT_OPEN = "<b><u>";
    private static final String TAG_HIGHLIGHT_CLOSE = "</u></b>";

    private final String function;
    private final I18n i18n;
    private final Function<Entity[], Entity> processor;
    private final Params<?>[] params;
    private final boolean hasParams;
    private final int paramsCount;
    private final String injectable;
    private String toString;
    private String[] focusParams;

    private Functions(final String function, final I18n i18n, final Function<Entity[], Entity> processor, final Params<?>... params) {
        this.function = function;
        this.i18n = i18n;
        this.processor = processor;

        this.hasParams = params != null && params.length > 0;
        if (this.hasParams) {
            this.params = params;
            this.paramsCount = params.length;
        } else {
            this.params = new Params<?>[0];
            this.paramsCount = 0;
        }

        this.focusParams = new String[this.params.length];

        final StringBuilder builderInjectable = new StringBuilder(function).append(StringUtils.PARENTHESIS_OPEN);
        builderInjectable.append(Arrays.stream(this.params).map(p -> StringUtils.INJECT_FIELD).collect(StringUtils.SEMICOLON_JOINING_COLLECTOR));
        this.injectable = builderInjectable.append(StringUtils.PARENTHESIS_CLOSE).toString();

        this.updateI18n();

        if (i18n != null) {
            i18n.addUpdateListener(c -> this.updateI18n());
        }
    }

    private Functions(final String function, final Function<Entity[], Entity> processor) {
        this(function, null, processor);
    }

    public String getFunction() {
        return this.function;
    }

    public Function<Entity[], Entity> getProcessor() {
        return this.processor;
    }

    public boolean hasParams() {
        return this.hasParams;
    }

    public int getParamsCount() {
        return this.paramsCount;
    }

    public Params<?>[] getParams() {
        return this.params;
    }

    public I18n getI18n() {
        return this.i18n;
    }

    public String[] getFocusParams() {
        return this.focusParams;
    }

    public List<I18n> check(final Entity... params) {
        final List<I18n> errors = new ArrayList<>();

        if (this.getParamsCount() != params.length) {
            errors.add(I18n.DIALOG_ERROR_PARAMS_COUNT);
            return errors;
        }

        Predicate<Entity> predicate;
        for (int i = 0; i < this.getParamsCount(); ++i) {
            predicate = this.getParams()[i].getValidator();
            if (predicate != null && !predicate.test(params[i])) {
                errors.add(this.getParams()[i].getPredicateI18n());
            }
        }
        return errors;
    }

    public String inject(final String... params) {
        return StringUtils.inject(this.injectable, params);
    }

    public void updateI18n() {
        final String injectableFocus = new StringBuilder(TAG_HTML_OPEN).append(this.injectable).append(TAG_HTML_CLOSE).toString();
        final String[] params = Arrays.stream(this.params).map(p -> p.getI18n().getI18n()).toArray(String[]::new);

        final List<String[]> paramsFocus = new ArrayList<>();
        for (int i = 0; i < params.length; ++i) {
            String[] paramFocus = Arrays.copyOf(params, params.length);
            paramFocus[i] = new StringBuilder(TAG_HIGHLIGHT_OPEN).append(params[i]).append(TAG_HIGHLIGHT_CLOSE).toString();
            paramsFocus.add(paramFocus);
        }

        this.focusParams = paramsFocus.stream().map(p -> StringUtils.inject(injectableFocus, p)).toArray(String[]::new);
        this.toString = StringUtils.inject(this.injectable, params);
    }

    @Override
    public String toString() {
        return this.toString;
    }
}