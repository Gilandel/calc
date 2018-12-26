package fr.landel.calc.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Logger {

    // date [level] class - message
    private static final String FORMAT_SIMPLE = "{} [{}] {} - {}";
    private static final String FORMAT_EXCEPTION = "{} [{}] {} - {}\n{}";

    private static final Collector<String, ?, List<String>> COLLECTOR = Collectors.toList();

    private static Level level = Level.INFO;
    private final String clazz;

    public Logger(final Class<?> clazz) {
        this.clazz = clazz.getSimpleName();
    }

    private void log(final Level level, final Throwable throwable, final String message, final Object... args) {

        if (Logger.level.ordinal() <= level.ordinal()) {
            final String date = DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now());

            final String content;
            if (args != null && args.length > 0) {
                content = StringUtils.inject(message,
                        Arrays.stream(args).map(String::valueOf).collect(COLLECTOR).toArray(new String[args.length]));
            } else {
                content = message;
            }

            final String log;
            if (throwable != null) {
                log = StringUtils.inject(FORMAT_EXCEPTION, date, level.name(), clazz, content, throwable.getMessage());
            } else {
                log = StringUtils.inject(FORMAT_SIMPLE, date, level.name(), clazz, content);
            }

            if (Level.ERROR.equals(level)) {
                System.err.println(log);
            } else {
                System.out.println(log);
            }
        }
    }

    public void debug(final String message, final Object... args) {
        this.log(Level.DEBUG, null, message, args);
    }

    public void debug(final Throwable throwable, final String message, final Object... args) {
        this.log(Level.DEBUG, throwable, message, args);
    }

    public void info(final String message, final Object... args) {
        this.log(Level.INFO, null, message, args);
    }

    public void info(final Throwable throwable, final String message, final Object... args) {
        this.log(Level.INFO, throwable, message, args);
    }

    public void warn(final String message, final Object... args) {
        this.log(Level.WARN, null, message, args);
    }

    public void warn(final Throwable throwable, final String message, final Object... args) {
        this.log(Level.WARN, throwable, message, args);
    }

    public void error(final String message, final Object... args) {
        this.log(Level.ERROR, null, message, args);
    }

    public void error(final Throwable throwable, final String message, final Object... args) {
        this.log(Level.ERROR, throwable, message, args);
    }

    public enum Level {
        DEBUG,
        INFO,
        WARN,
        ERROR;
    }
}
