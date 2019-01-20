package fr.landel.calc.config;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import fr.landel.calc.utils.Logger;
import fr.landel.calc.utils.StringUtils;

public enum I18n {
    TITLE("project.title"),

    MENU_FILE("menu.file", KeyEvent.VK_F),
    MENU_FILE_CLOSE("menu.file.close", Images.EXIT, KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK),

    MENU_EDIT("menu.edit", KeyEvent.VK_E),
    MENU_EDIT_COPY("menu.edit.copy", Images.COPY, KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK),
    MENU_EDIT_PASTE("menu.edit.paste", Images.PASTE, KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK),
    MENU_EDIT_CUT("menu.edit.cut", Images.CUT, KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK),
    MENU_EDIT_DELETE("menu.edit.delete", Images.DELETE, KeyEvent.VK_DELETE, InputEvent.CTRL_DOWN_MASK),
    MENU_EDIT_CLEAR("menu.edit.clear", Images.CLEAR, KeyEvent.VK_DELETE, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
    MENU_EDIT_INSERT("menu.edit.insert", Images.INSERT, KeyEvent.VK_INSERT, InputEvent.CTRL_DOWN_MASK),

    MENU_VIEW("menu.view", KeyEvent.VK_V),
    MENU_VIEW_KEYBOARD("menu.view.keyboard", KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK),

    MENU_FUNCTIONS("menu.functions", KeyEvent.VK_N),

    MENU_MATH("menu.functions.math"),
    MENU_FUNCTIONS_ABS("menu.functions.math.abs"),
    MENU_FUNCTIONS_ACOS("menu.functions.math.acos"),
    MENU_FUNCTIONS_ASIN("menu.functions.math.asin"),
    MENU_FUNCTIONS_ATAN("menu.functions.math.atan"),
    MENU_FUNCTIONS_CEIL("menu.functions.math.ceil"),
    MENU_FUNCTIONS_COS("menu.functions.math.cos"),
    MENU_FUNCTIONS_EXP("menu.functions.math.exp"),
    MENU_FUNCTIONS_FACT("menu.functions.math.fact"),
    MENU_FUNCTIONS_FLOOR("menu.functions.math.floor"),
    MENU_FUNCTIONS_LOG("menu.functions.math.log"),
    MENU_FUNCTIONS_LN("menu.functions.math.ln"),
    MENU_FUNCTIONS_PI("menu.functions.math.pi"),
    MENU_FUNCTIONS_POW("menu.functions.math.pow"),
    MENU_FUNCTIONS_ROUND("menu.functions.math.round"),
    MENU_FUNCTIONS_SIN("menu.functions.math.sin"),
    MENU_FUNCTIONS_SQR("menu.functions.math.sqr"),
    MENU_FUNCTIONS_TAN("menu.functions.math.tan"),

    MENU_FUNCTIONS_TIME("menu.functions.time"),
    MENU_FUNCTIONS_YEAR("menu.functions.time.year"),
    MENU_FUNCTIONS_MONTH("menu.functions.time.month"),
    MENU_FUNCTIONS_WEEK("menu.functions.time.week"),
    MENU_FUNCTIONS_DAY("menu.functions.time.day"),
    MENU_FUNCTIONS_HOURS("menu.functions.time.hours"),
    MENU_FUNCTIONS_MINUTES("menu.functions.time.minutes"),
    MENU_FUNCTIONS_SECONDS("menu.functions.time.seconds"),
    MENU_FUNCTIONS_MILLISECONDS("menu.functions.time.milliseconds"),
    MENU_FUNCTIONS_MICROSECONDS("menu.functions.time.microseconds"),
    MENU_FUNCTIONS_NANOSECONDS("menu.functions.time.nanoseconds"),
    MENU_FUNCTIONS_NOW("menu.functions.time.now"),

    MENU_FUNCTIONS_CONVERT("menu.functions.convert"),
    MENU_FUNCTIONS_DISTANCE("menu.functions.distance"),
    MENU_FUNCTIONS_METER("menu.functions.distance.meters"),
    MENU_FUNCTIONS_YARDS("menu.functions.distance.yards"),
    MENU_FUNCTIONS_FEET("menu.functions.distance.feet"),
    MENU_FUNCTIONS_INCHES("menu.functions.distance.inches"),
    MENU_FUNCTIONS_TEMP("menu.function.temperature"),
    MENU_FUNCTIONS_FAREINHEIT("menu.functions.temperature.fareinheit"),
    MENU_FUNCTIONS_DEGREE("menu.functions.temperature.degree"),
    MENU_FUNCTIONS_KELVIN("menu.functions.temperature.kelvin"),

    // https://converticious.com

    MENU_SETTINGS("menu.settings", KeyEvent.VK_S),
    MENU_SETTINGS_RADIAN("menu.settings.radian"),
    MENU_SETTINGS_EXACT("menu.settings.exact"),
    MENU_SETTINGS_SCIENTIFIC("menu.settings.scientific"),
    MENU_SETTINGS_UNITY_LENGTH_FULL("menu.settings.unity.length.full"),
    MENU_SETTINGS_UNITY_SPACE("menu.settings.unity.space"),
    MENU_SETTINGS_ACCURACY("menu.settings.accuracy"),
    MENU_SETTINGS_DECIMAL("menu.settings.decimal"),
    MENU_SETTINGS_DECIMAL_PLURAL("menu.settings.decimal.plural"),
    MENU_SETTINGS_CUSTOMIZE("menu.settings.customize"),

    MENU_SETTINGS_PREFERENCES("menu.settings.preferences", Images.PREFERENCES),

    MENU_HELP("menu.help", KeyEvent.VK_H),
    MENU_HELP_ABOUT("menu.help.about", Images.ABOUT),

    FRAME_INPUT("frame.input"),

    DIALOG_BUTTON_OK("dialog.button.ok"),
    DIALOG_BUTTON_CANCEL("dialog.button.cancel"),
    DIALOG_BUTTON_INSERT("dialog.button.insert"),

    DIALOG_PREFERENCES("dialog.preferences"),
    DIALOG_PREFERENCES_HISTORY("dialog.preferences.history"),
    DIALOG_PREFERENCES_HISTORY_MAX("dialog.preferences.history.max"),
    DIALOG_PREFERENCES_HISTORY_SAVE("dialog.preferences.history.save"),
    DIALOG_PREFERENCES_THEME("dialog.preferences.theme"),
    DIALOG_PREFERENCES_LANGUAGE("dialog.preferences.language"),

    DIALOG_ABOUT("dialog.about"),
    DIALOG_ABOUT_PROJECT("dialog.about.project"),
    DIALOG_ABOUT_PROJECT_AUTHOR("dialog.about.project.author"),
    DIALOG_ABOUT_PROJECT_AUTHOR_TEXT("dialog.about.project.author.text"),
    DIALOG_ABOUT_PROJECT_CREATION("dialog.about.project.creation"),
    DIALOG_ABOUT_PROJECT_CREATION_DATE("dialog.about.project.creation.date"),
    DIALOG_ABOUT_PROJECT_UPDATE("dialog.about.project.update"),
    DIALOG_ABOUT_PROJECT_UPDATE_DATE("dialog.about.project.update.date"),
    DIALOG_ABOUT_PROJECT_LINK("dialog.about.project.link"),
    DIALOG_ABOUT_PROJECT_LINK_TEXT("dialog.about.project.link.text"),
    DIALOG_ABOUT_PROJECT_EMAIL("dialog.about.project.email"),
    DIALOG_ABOUT_PROJECT_EMAIL_TEXT("dialog.about.project.email.text"),

    DIALOG_FUNCTION("dialog.function"),

    DIALOG_FUNCTION_ABS("dialog.function.abs"),
    DIALOG_FUNCTION_ACOS("dialog.function.acos"),
    DIALOG_FUNCTION_ASIN("dialog.function.asin"),
    DIALOG_FUNCTION_ATAN("dialog.function.atan"),
    DIALOG_FUNCTION_CEIL("dialog.function.ceil"),
    DIALOG_FUNCTION_COS("dialog.function.cos"),
    DIALOG_FUNCTION_EXP("dialog.function.exp"),
    DIALOG_FUNCTION_FACT("dialog.function.fact"),
    DIALOG_FUNCTION_FLOOR("dialog.function.floor"),
    DIALOG_FUNCTION_LOG("dialog.function.log"),
    DIALOG_FUNCTION_LN("dialog.function.ln"),
    DIALOG_FUNCTION_POW("dialog.function.pow"),
    DIALOG_FUNCTION_ROUND("dialog.function.round"),
    DIALOG_FUNCTION_SIN("dialog.function.sin"),
    DIALOG_FUNCTION_SQR("dialog.function.sqr"),
    DIALOG_FUNCTION_TAN("dialog.function.tan"),

    DIALOG_FUNCTION_YEAR("dialog.function.year"),
    DIALOG_FUNCTION_MONTH("dialog.function.month"),
    DIALOG_FUNCTION_WEEK("dialog.function.week"),
    DIALOG_FUNCTION_DAY("dialog.function.day"),
    DIALOG_FUNCTION_HOURS("dialog.function.hours"),
    DIALOG_FUNCTION_MINUTES("dialog.function.minutes"),
    DIALOG_FUNCTION_SECONDS("dialog.function.seconds"),
    DIALOG_FUNCTION_MILLISECONDS("dialog.function.milliseconds"),
    DIALOG_FUNCTION_MICROSECONDS("dialog.function.microseconds"),
    DIALOG_FUNCTION_NANOSECONDS("dialog.function.nanoseconds"),

    DIALOG_FUNCTION_PARAM_VALUE("dialog.function.param.value"),
    DIALOG_FUNCTION_PARAM_EXPONENT("dialog.function.param.exponent"),
    DIALOG_FUNCTION_PARAM_COSINUS("dialog.function.param.cosinus"),
    DIALOG_FUNCTION_PARAM_SINUS("dialog.function.param.sinus"),
    DIALOG_FUNCTION_PARAM_TANGENT("dialog.function.param.tangent"),
    DIALOG_FUNCTION_PARAM_ACCURACY("dialog.function.param.accuracy"),
    DIALOG_FUNCTION_PARAM_ANGULAR("dialog.function.param.angular"),
    DIALOG_FUNCTION_PARAM_DATE("dialog.function.param.date"),
    DIALOG_FUNCTION_PARAM_UNITY("dialog.function.param.unity"),

    DIALOG_ERROR("dialog.error"),
    DIALOG_ERROR_SELECTION_EMPTY("dialog.error.selection.empty"),

    DIALOG_ERROR_PARAMS_COUNT("dialog.error.params.count"),
    DIALOG_ERROR_PARAM_VALUE("dialog.error.param.value"),
    DIALOG_ERROR_PARAM_ANGULAR("dialog.error.param.angular"),
    DIALOG_ERROR_PARAM_ACCURACY("dialog.error.param.accuracy"),
    DIALOG_ERROR_PARAM_DATE("dialog.error.param.date"),
    DIALOG_ERROR_PARAM_COSINUS("dialog.error.param.cosinus"),
    DIALOG_ERROR_PARAM_SINUS("dialog.error.param.sinus"),
    DIALOG_ERROR_PARAM_TANGENT("dialog.error.param.tangent"),
    DIALOG_ERROR_PARAM_EXPONENT("dialog.error.param.exponent"),
    DIALOG_ERROR_PARAM_UNITY("dialog.error.param.unity"),

    ERROR_CHARACTERS_RESTRICTED("error.characters.restricted"),
    ERROR_CHARACTERS_UNKNOWN("error.characters.unknown"),
    ERROR_RESULT_EVAL("error.result.eval"),
    ERROR_FUNCTION_PARSE("error.function.parse"),
    ERROR_FORMULA_PARSE("error.formula.parse"),
    ERROR_FORMULA_OPERATOR_POSITION("error.formula.operator.position"),
    ERROR_FORMULA_OPERATOR_MISSING("error.formula.operator.missing"),
    ERROR_VARIABLE_VALUE_MISSING("error.variable.value.missing"),
    ERROR_UNITY_TYPE("error.unity.type"),
    ERROR_UNITY_PARSE("error.unity.parse"),
    ERROR_UNITY_PARSE_TYPE("error.unity.parse.type"),
    ERROR_UNITY_BOUNDS("error.unity.bounds"),
    ERROR_UNITY_VALUE_MISSING("error.unity.value.missing");

    private static final Logger LOGGER = new Logger(I18n.class);

    private static final List<String> ISO_COUNTRIES = Arrays.asList(Locale.getISOCountries());
    private static final List<String> ISO_LANGUAGES = Arrays.asList(Locale.getISOLanguages());

    private static final Comparator<Locale> LOCALE_COMPARATOR = (a, b) -> {
        int c = a.getLanguage().compareTo(b.getLanguage());
        if (c == 0) {
            c = a.getCountry().compareTo(b.getCountry());
        }
        return c;
    };

    public static final Locale DEFAULT_LOCALE = Locale.US;

    public static final List<Locale> SUPPORTED_LOCALES = listSupportedLocales();
    public static final List<String> SUPPORTED_LOCALES_NAME = Collections
            .unmodifiableList(SUPPORTED_LOCALES.stream().map(l -> l.getDisplayName(DEFAULT_LOCALE)).collect(Collectors.toList()));

    public static final String TAG_SEPARATOR = "_";

    private static final String RESOURCE_BUNDLE_DIR = "i18n";
    private static final String RESOURCE_BUNDLE_FILENAME_PREFIX = "i18n";
    private static final String RESOURCE_BUNDLE_FILENAME_SUFFIX = ".properties";
    private static final String RESOURCE_BUNDLE_PATH = RESOURCE_BUNDLE_DIR + "/" + RESOURCE_BUNDLE_FILENAME_PREFIX;

    private static Locale matchedLocale = null;
    private static ResourceBundle resourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_PATH);

    private Locale locale;
    private final String key;
    private String i18n;
    private Optional<Integer> mnemonic;
    private final Optional<KeyStroke> accelerator;
    private final Optional<ImageIcon> image;
    private final List<Consumer<String>> updateListeners = new ArrayList<>();

    private I18n(final String key, final Integer mnemonic, final Images image, final int keyCode, final int modifier) {
        this.key = key;
        this.mnemonic = Optional.ofNullable(mnemonic);
        if (image != null && image.getImage().isPresent()) {
            this.image = Optional.of(image.getImage().get());
        } else {
            this.image = Optional.empty();
        }
        if (keyCode > 0) {
            this.accelerator = Optional.of(KeyStroke.getKeyStroke(keyCode, modifier));
        } else {
            this.accelerator = Optional.empty();
        }
    }

    private I18n(final String key, final Integer mnemonic, final Images image) {
        this(key, mnemonic, image, 0, 0);
    }

    private I18n(final String key, final Images image, final int keyCode, final int modifier) {
        this(key, null, image, keyCode, modifier);
    }

    private I18n(final String key, final int keyCode, final int modifier) {
        this(key, null, null, keyCode, modifier);
    }

    private I18n(final String key, final Images image) {
        this(key, null, image, 0, 0);
    }

    private I18n(final String key, final Integer mnemonic) {
        this(key, mnemonic, null, 0, 0);
    }

    private I18n(final String key) {
        this(key, null, null, 0, 0);
    }

    public String getKey() {
        return this.key;
    }

    public Optional<Integer> getMnemonic() {
        this.getI18n();
        return this.mnemonic;
    }

    public Optional<KeyStroke> getAccelerator() {
        return this.accelerator;
    }

    public Optional<ImageIcon> getImage() {
        return this.image;
    }

    public String getI18n() {
        final Locale locale = Locale.getDefault();
        if (this.i18n == null || !locale.equals(this.locale)) {
            this.reload(locale);
        }
        return this.i18n;
    }

    private void reload(final Locale locale) {
        boolean updated = false;

        if (!Objects.equals(matchedLocale, locale)) {
            resourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_PATH, locale);
            matchedLocale = locale;
            updated = true;
        }

        try {
            String text = resourceBundle.getString(this.key);
            int length = text.length();
            if (length > 2) {
                int p1 = text.indexOf('{');
                if (p1 + 2 == text.indexOf('}')) {
                    this.mnemonic = Optional.of((int) text.toUpperCase().charAt(p1 + 1));
                }
                if (text.charAt(0) == '"' && text.charAt(length - 1) == '"') {
                    text = text.substring(1, length - 1);
                }
                if (this.mnemonic.isPresent()) {
                    text = text.replace("{", "").replace("}", "");
                }
            }
            this.i18n = text;
        } catch (MissingResourceException e) {
            LOGGER.error(e, "The key '{}' cannot be found for locale: {}", key, locale.toLanguageTag());
            this.i18n = "Unknown";
        }
        this.locale = locale;

        if (updated) {
            this.updateListeners.forEach(c -> c.accept(this.i18n));
        }
    }

    public static Locale getCurrentLocale() {
        return resourceBundle.getLocale();
    }

    private static List<Locale> listSupportedLocales() {
        final SortedSet<Locale> locales = new TreeSet<>(LOCALE_COMPARATOR);

        try {
            final URI uri = I18n.class.getResource("/" + RESOURCE_BUNDLE_DIR).toURI();
            Path myPath;
            if (uri.getScheme().equals("jar")) {
                final FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object> emptyMap());
                myPath = fileSystem.getPath("/" + RESOURCE_BUNDLE_DIR);
            } else {
                myPath = Paths.get(uri);
            }

            Files.walk(myPath, 1).forEach(p -> getLocales(p, locales));

        } catch (URISyntaxException | IOException e) {
            LOGGER.error(e, "Cannot list supported locales");
        }

        return Collections.unmodifiableList(new ArrayList<>(locales));
    }

    private static void getLocales(final Path path, final SortedSet<Locale> locales) {
        String resource = path.getFileName().toString();

        if (resource.startsWith(RESOURCE_BUNDLE_FILENAME_PREFIX) && resource.endsWith(RESOURCE_BUNDLE_FILENAME_SUFFIX)) {
            String tag = resource.replace(RESOURCE_BUNDLE_FILENAME_PREFIX, StringUtils.EMPTY);
            tag = tag.replace(RESOURCE_BUNDLE_FILENAME_SUFFIX, StringUtils.EMPTY);

            if (tag.startsWith(TAG_SEPARATOR)) {
                tag = tag.substring(1);
            }

            if (!tag.isEmpty()) {
                final Locale.Builder builder = new Locale.Builder();
                final String[] sections = tag.split(TAG_SEPARATOR);
                if (sections.length == 2 && ISO_COUNTRIES.contains(sections[1])) {
                    builder.setRegion(sections[1]);
                }
                if (ISO_LANGUAGES.contains(sections[0])) {
                    builder.setLanguage(sections[0]);

                    final Locale locale = builder.build();
                    locales.add(locale);
                }
            }
        }
    }

    public void addUpdateListener(final Consumer<String> consumer) {
        this.updateListeners.add(consumer);
    }

    @Override
    public String toString() {
        return this.key;
    }
}
