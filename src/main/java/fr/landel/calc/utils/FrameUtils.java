package fr.landel.calc.utils;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

public class FrameUtils {

    private static final Logger LOGGER = new Logger(FrameUtils.class);

    private FrameUtils() {
    }

    public static int getScreenWidth() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return screenSize.width;
    }

    public static int getScreenHeight() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return screenSize.height;
    }

    public static void setCentered(Frame frame, Frame parent) {
        // frame.pack();
        frame.setLocation(parent.getLocation().x + (parent.getSize().width - frame.getSize().width) / 2,
                parent.getLocation().y + (parent.getSize().height - frame.getSize().height) / 2);
    }

    public static void setCentered(Dialog dialog, Frame parent) {
        // dialog.pack();
        dialog.setLocation(parent.getLocation().x + (parent.getSize().width - dialog.getSize().width) / 2,
                parent.getLocation().y + (parent.getSize().height - dialog.getSize().height) / 2);
    }

    public static void setScreenCentered(Frame frame) {
        frame.pack();
        frame.setLocation((getScreenWidth() - frame.getWidth()) / 2, (getScreenHeight() - frame.getHeight()) / 2);
    }

    public static void setScreenCentered(Dialog dialog) {
        dialog.pack();
        dialog.setLocation((getScreenWidth() - dialog.getWidth()) / 2, (getScreenHeight() - dialog.getHeight()) / 2);
    }

    public static Frame getFrame(String name) {
        int i = 0;
        Frame aFrame[];
        for (aFrame = JFrame.getFrames(); i < aFrame.length && !aFrame[i].getName().matches(name); i++)
            ;
        return aFrame[i];
    }

    public static String getCrossPlatformLookAndFeel() {
        return StringUtils.field(UIManager.getCrossPlatformLookAndFeelClassName(),
                StringUtils.count(UIManager.getCrossPlatformLookAndFeelClassName(), ".") - 1, ".").toLowerCase();
    }

    public static String[] getLookAndFeel() {
        javax.swing.UIManager.LookAndFeelInfo aFeel[] = UIManager.getInstalledLookAndFeels();
        String aLAF[] = new String[aFeel.length];
        for (int i = 0; i < aFeel.length; i++)
            aLAF[i] = aFeel[i].getName();

        return aLAF;
    }

    public static void setLookAndFeel(final Window frame, final String name, final boolean applyDefault) {
        try {
            for (LookAndFeelInfo feel : UIManager.getInstalledLookAndFeels()) {
                if (feel.getName().toLowerCase().equals(name.toLowerCase())) {

                    UIManager.setLookAndFeel(feel.getClassName());
                    SwingUtilities.updateComponentTreeUI(frame);
                    return;
                }
            }

            if (applyDefault) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                SwingUtilities.updateComponentTreeUI(frame);
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            LOGGER.error(e, "Cannot change look and feel for frame: '{}'", name);
        }
    }

    public static void setTitleBar(Frame frame, boolean state) {
        if (!frame.isDisplayable())
            frame.setUndecorated(state);
    }

    public static void setTitleBar(Dialog dialog, boolean state) {
        if (!dialog.isDisplayable())
            dialog.setUndecorated(state);
    }
}
