package fr.landel.calc.utils;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class ClipboardUtils {

    private static final Logger LOGGER = new Logger(ClipboardUtils.class);

    private ClipboardUtils() {
    }

    public static String getText() {
        String result = "";
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        final Transferable contents = clipboard.getContents(null);
        // has transferable text
        if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                result = (String) contents.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException | IOException e) {
                LOGGER.error(e, "Cannot get clipboard text");
            }
        }
        return result;
    }

    public static void setText(String text) {
        final ClipboardOwner owner = (clipboard, contents) -> LOGGER.debug("Clipboard owner lost");
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(text), owner);
    }
}
