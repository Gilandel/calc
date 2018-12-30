package fr.landel.calc.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringUtils {

    public static final String EMPTY = "";
    public static final String SPACE = " ";
    public static final String INJECT_FIELD = "{}";

    private static final Pattern PATTERN_PARAM = Pattern.compile("\\{\\}");

    private StringUtils() {
    }

    public static int count(final String text, final String stringToFind) {
        int fromIndex = 0;
        int count = 0;
        while ((fromIndex = text.indexOf(stringToFind, fromIndex) + 1) > 0) {
            ++count;
        }
        return count;
    }

    public static int count(final String text, final String stringToFind, final boolean regex) {
        if (regex) {
            return count(text, Pattern.compile(stringToFind));
        } else {
            return count(text, stringToFind);
        }
    }

    public static int count(final String text, final Pattern regex) {
        final Matcher matcher = regex.matcher(text);
        int count = 0;
        while (matcher.find()) {
            if (matcher.group().length() > 0) {
                ++count;
            }
        }
        return count;
    }

    public static String inject(final String text, final String... params) {
        return inject(text, (Object[]) params);
    }

    public static String inject(final String text, final Object... params) {
        if (params == null || params.length == 0) {
            return text;
        }

        String result = EMPTY;
        final String[] split = PATTERN_PARAM.split(text);
        for (int index = 0; index < split.length; index++) {
            result = result.concat(split[index]);
            if (params.length > index) {
                result = result.concat(String.valueOf(params[index]));
            }
        }
        return result;
    }

    public static String field(String text, int index, String separator) {
        return field(text, index, separator, false);
    }

    public static String field(String text, int index, String separator, boolean regex) {
        Pattern p;
        if (regex)
            p = Pattern.compile(separator);
        else
            p = Pattern.compile(Pattern.quote(separator));
        return field(text, index, p);
    }

    public static String field(String text, int index, Pattern regex) {
        String res = EMPTY;
        int i = 0;
        int count = count(text, regex);
        if (index <= count) {
            int start[] = new int[count + 1];
            int end[] = new int[count + 1];
            for (Matcher m = regex.matcher(text); m.find();) {
                start[i] = m.start();
                end[i] = m.end();
                i++;
            }

            if (index == 0) {
                if (start[0] > 0)
                    res = text.substring(0, start[0]);
                else
                    res = EMPTY;
            } else if (index == count) {
                if (end[count - 1] < text.length())
                    res = text.substring(end[count - 1], text.length());
                else
                    res = EMPTY;
            } else {
                res = text.substring(end[index - 1], start[index]);
            }
        } else {
            res = text;
        }
        return res;
    }
}
