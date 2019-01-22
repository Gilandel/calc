package fr.landel.calc.config;

import java.awt.Frame;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Pattern;

import fr.landel.calc.processor.Entity;
import fr.landel.calc.processor.ProcessorException;
import fr.landel.calc.utils.Logger;
import fr.landel.calc.utils.StringUtils;

public enum Conf {

    LOCALE("locale", Locale.class, I18n.DEFAULT_LOCALE),
    EXACT("result.exact", Boolean.class, false),
    RADIAN("result.radian", Boolean.class, false),
    SCIENTIFIC("result.scientific", Boolean.class, false),
    PRECISION("result.precision", Integer.class, 6),
    THEME("frame.theme", (Pattern) null, "Windows"),
    X("frame.x", Integer.class, null),
    Y("frame.y", Integer.class, null),
    WIDTH("frame.width", Integer.class, 500),
    HEIGHT("frame.height", Integer.class, 300),
    EXTENDED_STATE("frame.extendedState", Integer.class, Frame.NORMAL),
    KEYBOARD("frame.keyboard", Boolean.class, true),
    LOG_MODE("log.mode", Pattern.compile("^(" + Logger.LOG_MODE_CONSOLE + "|" + Logger.LOG_MODE_FILE + ")$"), Logger.LOG_MODE_CONSOLE),
    UNITY_ABBREV("unity.abbrev", Boolean.class, true),
    UNITY_SPACE("unity.space", Boolean.class, true),
    VALUE_SPACE("value.space", Boolean.class, true),

    HISTORY_MAX("history.max", Integer.class, 100),
    HISTORY_SAVE("history.save", Boolean.class, true),

    HISTORY_FORMULA("history.formula.{}", String.class, null),
    HISTORY_FORMULA_STATUS("history.formula.{}.status", Boolean.class, true),
    HISTORY_FORMULA_RESULT("history.formula.{}.result", String.class, null),

    VARIABLE_MAX("variable.max", Integer.class, 25),
    VARIABLE_SAVE("variable.save", Boolean.class, true),

    VARIABLE_KEY("variable.{}.key", Entity.PATTERN_VARIABLE, null),
    VARIABLE_VALUE("variable.{}.value", String.class, null);

    private static final Logger LOGGER = new Logger(Conf.class);

    private final String key;
    private final Class<?> type;
    private final Object defaultValue;
    private final Pattern pattern;
    private Boolean matches;

    private <T> Conf(final String key, final Class<T> type, final T defaultValue) {
        this.key = key;
        this.type = type;
        this.defaultValue = defaultValue;
        this.pattern = null;
    }

    private <T> Conf(final String key, final Pattern pattern, final String defaultValue) {
        this.key = key;
        this.type = String.class;
        this.defaultValue = defaultValue;
        this.pattern = pattern;
    }

    public Optional<Boolean> getBoolean() {

        if (!Boolean.class.equals(this.type)) {
            throw new IllegalArgumentException("Key type doesn't match boolean");
        }

        final String value = Configuration.get(this);

        if (value != null && value.length() > 0) {
            return Optional.of(Boolean.parseBoolean(value));
        } else {
            return Optional.ofNullable((Boolean) this.defaultValue);
        }
    }

    public Optional<Integer> getInt() {

        if (!Integer.class.equals(this.type)) {
            throw new IllegalArgumentException("Key type doesn't match boolean");
        }

        final String value = Configuration.get(this);

        if (value != null && value.length() > 0) {
            return Optional.ofNullable(parseInt(value, (Integer) this.defaultValue));
        } else {
            return Optional.ofNullable((Integer) this.defaultValue);
        }
    }

    public Optional<String> getString() {

        if (!String.class.equals(this.type)) {
            throw new IllegalArgumentException("Key type doesn't match boolean");
        }

        final String value = Configuration.get(this);

        if (value != null && value.length() > 0 && (this.pattern == null || Boolean.TRUE.equals(matches) || this.pattern.matcher(value).matches())) {
            matches = true;
            return Optional.of(value);
        } else {
            matches = false;
            return Optional.ofNullable((String) this.defaultValue);
        }
    }

    public void set(final Object object) {
        if (object == null) {
            Configuration.set(this, null);
        } else if (this.type.isAssignableFrom(object.getClass())) {
            Configuration.set(this, object);
        } else {
            throw new IllegalArgumentException("Object type doesn't match key type");
        }
    }

    public static void clearFormulas() {
        Configuration.clear(StringUtils.inject(Conf.HISTORY_FORMULA.getKey(), StringUtils.EMPTY));
    }

    public static void setFormula(final int index, final Formula formula) {
        Configuration.set(Conf.HISTORY_FORMULA, index, formula.getFormula());
        formula.getResult().ifPresent(result -> {
            Configuration.set(Conf.HISTORY_FORMULA_STATUS, index, String.valueOf(result.isSuccess()));
            Configuration.set(Conf.HISTORY_FORMULA_RESULT, index, result.getResult());
        });
    }

    public static List<Formula> getFormulas() {

        final int max = HISTORY_MAX.getInt().get();

        final List<Formula> formulas = new LinkedList<>();

        String formula, success, result;
        for (int index = 0; index < max; index++) {
            formula = Configuration.get(Conf.HISTORY_FORMULA, index);
            success = Configuration.get(Conf.HISTORY_FORMULA_STATUS, index);
            result = Configuration.get(Conf.HISTORY_FORMULA_RESULT, index);

            if (formula != null && !formula.isBlank()) {
                if (result != null && !result.isBlank()) {
                    formulas.add(new Formula(formula, Boolean.parseBoolean(success), result));
                } else {
                    formulas.add(new Formula(formula));
                }
            } else {
                break;
            }
        }

        return formulas;
    }

    public static void saveVariables() {
        int index = 0;
        for (Entry<String, Optional<Entity>> entry : Entity.VARIABLES.entrySet()) {
            Configuration.set(Conf.VARIABLE_KEY, index, entry.getKey());
            if (entry.getValue().isPresent()) {
                Configuration.set(Conf.VARIABLE_VALUE, index, entry.getValue().get().toString());
            }
            ++index;
        }
    }

    public static void loadVariables() throws ProcessorException {

        final int max = VARIABLE_MAX.getInt().get();

        String key, value;
        for (int index = 0; index < max; index++) {
            key = Configuration.get(Conf.VARIABLE_KEY, index);
            value = Configuration.get(Conf.VARIABLE_VALUE, index);

            if (key != null && !key.isBlank()) {
                if (value != null) {
                    Entity.VARIABLES.put(key, Optional.ofNullable(new Entity(0, StringUtils.replaceCommaByDot(StringUtils.removeAllSpaces(value)))));
                } else {
                    Entity.VARIABLES.put(key, Optional.empty());
                }
            } else {
                break;
            }
        }
    }

    public static Locale getLocale() {
        final String value = Configuration.get(Conf.LOCALE);
        final Locale defaultValue = (Locale) Conf.LOCALE.defaultValue;

        if (value != null && value.length() > 0) {
            final String[] sections = value.split(I18n.TAG_SEPARATOR);
            if (sections.length == 2) {
                return bestMatchLocale(sections[0], sections[1]).orElse(defaultValue);
            } else {
                return bestMatchLocale(sections[0], StringUtils.EMPTY).orElse(defaultValue);
            }
        } else {
            return defaultValue;
        }
    }

    private static Optional<Locale> bestMatchLocale(final String language, final String region) {
        Locale bestMatch = null;
        Locale languageMatch = null;

        for (Locale locale : I18n.SUPPORTED_LOCALES) {

            if (locale.getLanguage().equals(language)) {

                if (locale.getCountry().equals(region)) {
                    return Optional.of(locale);

                } else if (StringUtils.EMPTY.equals(locale.getCountry())) {
                    languageMatch = locale;

                } else if (bestMatch == null) {
                    bestMatch = locale;
                }
            }
        }

        // prefer country less locale
        if (languageMatch != null) {
            return Optional.of(languageMatch);

        } else { // otherwise take first match
            return Optional.ofNullable(bestMatch);
        }
    }

    private static Integer parseInt(final String value, final Integer defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            LOGGER.warn(e, "Cannot format number: {}", value);
            return defaultValue;
        }
    }

    public String getKey() {
        return this.key;
    }

    public Object getDefaultValue() {
        return this.defaultValue;
    }

    public static Optional<Conf> getConf(final String key) {
        return Arrays.stream(Conf.values()).filter(v -> v.key.equals(key)).findAny();
    }
}