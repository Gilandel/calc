package fr.landel.calc.utils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SystemUtils {

    private static final Logger LOGGER = new Logger(SystemUtils.class);

    private static final String BROWSERS[] = {"firefox", "chrome", "opera", "konqueror", "epiphany", "mozilla", "netscape"};

    private static boolean mac = false;
    private static boolean linux = false;
    private static boolean windows = false;
    static {
        final String os = System.getProperty("os.name");
        if (os.startsWith("Mac")) {
            mac = true;
        } else if (os.startsWith("Linux")) {
            linux = true;
        } else if (os.startsWith("Windows")) {
            windows = true;
        }
    }

    private SystemUtils() {
    }

    public static boolean isMac() {
        return mac;
    }

    public static boolean isLinux() {
        return linux;
    }

    public static boolean isWindows() {
        return windows;
    }

    public static void execLink(String cmd) {
        String browser = null;

        try {
            if (isWindows()) {
                Runtime.getRuntime().exec(new StringBuilder("cmd /c start ").append(cmd).toString());

            } else if (isMac()) {
                Runtime.getRuntime().exec(new StringBuilder("open ").append(cmd).toString());

                try {
                    Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
                    Method openURL = fileMgr.getDeclaredMethod("openURL", String.class);
                    openURL.invoke(null, cmd);

                } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    LOGGER.error(e, "Cannot execute link: {}", cmd);
                }
            } else {
                try {
                    for (int count = 0; count < BROWSERS.length && browser == null; count++) {
                        if (Runtime.getRuntime().exec(new String[] {"which", BROWSERS[count]}).waitFor() == 0) {
                            browser = BROWSERS[count];
                        }
                    }

                    if (browser == null) {
                        throw new IOException("Could not find web browser");
                    }
                    Runtime.getRuntime().exec(new String[] {browser, cmd});

                } catch (IOException | InterruptedException e) {
                    LOGGER.error(e, "Cannot execute link: {}", cmd);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
