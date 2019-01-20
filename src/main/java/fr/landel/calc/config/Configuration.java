package fr.landel.calc.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import fr.landel.calc.function.TriFunction;
import fr.landel.calc.utils.Logger;
import fr.landel.calc.utils.StringUtils;

/**
 * Load configuration (don't use Properties to keep output order)
 *
 * @since Dec 4, 2018
 * @author Gilles
 *
 */
public final class Configuration extends ConcurrentHashMap<String, String> {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 5012113863446988529L;

    private static final Logger LOGGER = new Logger(Configuration.class);

    private static final String COMMENT_PREFIX = "# ";
    private static final String KEY_VALUE_SEPARATOR = " = ";

    private static final String PATH = ".calculatrice";
    private static final String FILENAME = "cfg.properties";

    private static final File DIRECTORY = new File(System.getProperty("user.home"), PATH);
    private static final File FILE = new File(DIRECTORY, FILENAME);
    private static final Configuration PROPS = new Configuration();
    static {
        if (FILE.exists()) {
            PROPS.load();

            // remove old keys
            String key;
            final Iterator<Entry<String, String>> keys = PROPS.entrySet().iterator();
            while (keys.hasNext()) {
                key = String.valueOf(keys.next());
                if (Conf.getConf(key).isEmpty() && !key.startsWith(Conf.HISTORY_FORMULA.getKey())) {
                    PROPS.remove(key);
                }
            }

            LOGGER.info("Configuration loaded");
        } else if (!DIRECTORY.exists() && !DIRECTORY.mkdirs()) {
            LOGGER.error("Cannot create configuration directory: {}", DIRECTORY.getAbsolutePath());
        }
    }

    private static final String HISTORY_FORMULA_PREFIX = StringUtils.inject(Conf.HISTORY_FORMULA.getKey(), StringUtils.EMPTY);
    private static final String VARIABLE_PREFIX = "variable.";
    private static final int HISTORY_FORMULA_PREFIX_LENGTH = HISTORY_FORMULA_PREFIX.length();
    private static final int VARIABLE_PREFIX_LENGTH = VARIABLE_PREFIX.length();

    private static final TriFunction<String, String, Integer, Integer> COMPARATOR_PREFIX = (a, b, l) -> {
        final String aSegment = a.substring(l);
        final String bSegment = b.substring(l);
        final int aDot = aSegment.indexOf('.');
        final int bDot = bSegment.indexOf('.');
        final int aIndex = Integer.parseInt(aDot < 0 ? aSegment : aSegment.substring(0, aDot));
        final int bIndex = Integer.parseInt(bDot < 0 ? bSegment : bSegment.substring(0, bDot));

        if (aIndex < bIndex) {
            return -1;
        } else if (aIndex > bIndex) {
            return 1;
        } else {
            return a.compareTo(b);
        }
    };

    private static final Comparator<String> COMPARATOR = (a, b) -> {
        final boolean aHistory = a.startsWith(HISTORY_FORMULA_PREFIX);
        final boolean bHistory = b.startsWith(HISTORY_FORMULA_PREFIX);
        final boolean aVariable = a.startsWith(VARIABLE_PREFIX);
        final boolean bVariable = b.startsWith(VARIABLE_PREFIX);

        if (aHistory && !bHistory) {
            return 1;
        } else if (!aHistory && bHistory) {
            return -1;
        } else if (aVariable && !bVariable) {
            return 1;
        } else if (!aVariable && bVariable) {
            return -1;
        } else if (aHistory && bHistory) {
            return COMPARATOR_PREFIX.apply(a, b, HISTORY_FORMULA_PREFIX_LENGTH);
        } else if (aVariable && bVariable) {
            return COMPARATOR_PREFIX.apply(a, b, VARIABLE_PREFIX_LENGTH);
        } else {
            return a.compareTo(b);
        }
    };

    private Configuration() {
        super();
    }

    public void load() {
        try (final BufferedReader br = Files.newBufferedReader(FILE.toPath())) {
            br.lines().forEach(this::put);

        } catch (IOException e) {
            LOGGER.error(e, "Cannot load configuration: {}", FILE.getAbsolutePath());
        }
    }

    private void put(final String line) {
        if (!line.isBlank() && line.indexOf(COMMENT_PREFIX) != 0) {
            final int len = KEY_VALUE_SEPARATOR.length();
            final int pos = line.indexOf(KEY_VALUE_SEPARATOR);
            if (pos > -1 && pos + len < line.length()) {
                final String key = line.substring(0, pos).trim();
                final String value = line.substring(pos + len);

                this.put(key, value);
            }
        }
    }

    public void store(final String comment) {
        try (BufferedWriter bw = Files.newBufferedWriter(FILE.toPath(), StandardCharsets.UTF_8)) {
            bw.append(COMMENT_PREFIX).append(comment);
            bw.newLine();
            bw.append(COMMENT_PREFIX).append(new Date().toString());
            bw.newLine();

            final SortedMap<String, String> map = new TreeMap<>(COMPARATOR);
            map.putAll(this);

            for (Entry<String, String> entry : map.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isBlank()) {
                    bw.append(entry.getKey()).append(KEY_VALUE_SEPARATOR).append(entry.getValue());
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            LOGGER.error(e, "Cannot save configuration: {}", FILE.getAbsolutePath());
        }
    }

    public static void save() {
        try {
            if (!FILE.exists() && FILE.createNewFile()) {
                init();
            }

            if (FILE.exists()) {

                PROPS.store(I18n.TITLE.getI18n() + " configuration");

                LOGGER.info("Configuration saved");

            } else {
                LOGGER.error("Cannot save configuration: {}", FILE.getAbsolutePath());
            }
        } catch (IOException e) {
            LOGGER.error(e, "Cannot save configuration: {}", FILE.getAbsolutePath());
        }
    }

    private static void init() {
        Arrays.stream(Conf.values()).filter(v -> !Conf.HISTORY_FORMULA.equals(v) && v.getDefaultValue() != null)
                .forEach(v -> set(v, String.valueOf(v.getDefaultValue())));
    }

    public static void set(final Conf key, final Object value) {
        if (value != null) {
            PROPS.put(key.getKey(), String.valueOf(value));
        } else {
            PROPS.remove(key.getKey());
        }
    }

    public static void set(final Conf key, final int index, final String value) {
        final String realKey = StringUtils.inject(key.getKey(), index);
        if (value != null) {
            PROPS.put(realKey, value);
        } else {
            PROPS.remove(realKey);
        }
    }

    public static String get(final Conf key) {
        return PROPS.get(key.getKey());
    }

    public static String get(final Conf key, final int index) {
        return PROPS.get(StringUtils.inject(key.getKey(), index));
    }

    public static void clear(final String prefix) {
        for (String key : PROPS.keySet()) {
            if (key.startsWith(prefix)) {
                PROPS.remove(key);
            }
        }
    }
}
