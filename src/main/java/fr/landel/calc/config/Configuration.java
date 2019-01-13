package fr.landel.calc.config;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;

import fr.landel.calc.utils.Logger;

/**
 * Load configuration
 *
 * @since Dec 4, 2018
 * @author Gilles
 *
 */
public final class Configuration {

    private static final Logger LOGGER = new Logger(Configuration.class);

    private static final String PATH = ".calculatrice";
    private static final String FILENAME = "cfg.properties";

    private static final File DIRECTORY = new File(System.getProperty("user.home"), PATH);
    private static final File FILE = new File(DIRECTORY, FILENAME);
    private static final Properties PROPS = new Properties();
    static {
        if (FILE.exists()) {
            try (InputStream is = new BufferedInputStream(new FileInputStream(FILE))) {
                PROPS.load(is);

                // remove old keys
                Iterator<?> keys = PROPS.propertyNames().asIterator();
                while (keys.hasNext()) {
                    String key = String.valueOf(keys.next());
                    if (Conf.getConf(key).isEmpty() && !key.startsWith(Conf.HISTORY_FORMULA.getKey())) {
                        PROPS.remove(key);
                    }
                }

                LOGGER.info("Configuration loaded");

            } catch (IOException e) {
                LOGGER.error(e, "Cannot load configuration: {}", FILE.getAbsolutePath());
            }
        } else if (!DIRECTORY.exists() && !DIRECTORY.mkdirs()) {
            LOGGER.error("Cannot create configuration directory: {}", DIRECTORY.getAbsolutePath());
        }
    }

    private Configuration() {
    }

    public static void save() {
        try {
            if (!FILE.exists() && FILE.createNewFile()) {
                init();
            }

            if (FILE.exists()) {
                try (OutputStream os = new FileOutputStream(FILE)) {
                    PROPS.store(os, I18n.TITLE.getI18n() + " configuration");

                    LOGGER.info("Configuration saved");
                }
            } else {
                LOGGER.error("Cannot save configuration: {}", FILE.getAbsolutePath());
            }
        } catch (IOException e) {
            LOGGER.error(e, "Cannot save configuration: {}", FILE.getAbsolutePath());
        }
    }

    private static void init() {
        Arrays.stream(Conf.values()).filter(v -> !Conf.HISTORY_FORMULA.equals(v) && v.getDefaultValue() != null).forEach(v -> set(v, String.valueOf(v.getDefaultValue())));
    }

    public static void set(final Conf key, final Object value) {
        if (value != null) {
            PROPS.setProperty(key.getKey(), String.valueOf(value));
        } else {
            PROPS.remove(key.getKey());
        }
    }

    public static void set(final Conf key, final int index, final Formula value) {
        final String realKey = key.getKey().concat(String.valueOf(index));
        if (value != null) {
            PROPS.setProperty(realKey, value.toString());
        } else {
            PROPS.remove(realKey);
        }
    }

    public static String get(final Conf key) {
        return PROPS.getProperty(key.getKey());
    }

    public static String get(final Conf key, final int index) {
        return PROPS.getProperty(key.getKey().concat(String.valueOf(index)));
    }

    public static void clear(final String prefix) {
        for (String key : PROPS.stringPropertyNames()) {
            if (key.startsWith(prefix)) {
                PROPS.remove(key);
            }
        }
    }
}
