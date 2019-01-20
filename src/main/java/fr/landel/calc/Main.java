package fr.landel.calc;

import java.awt.EventQueue;
import java.util.Locale;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import fr.landel.calc.config.Conf;
import fr.landel.calc.config.I18n;
import fr.landel.calc.processor.ProcessorException;
import fr.landel.calc.utils.Logger;
import fr.landel.calc.view.MainFrame;

/**
 * Main class
 *
 * @since Dec 3, 2018
 * @author Gilles
 *
 */
public class Main {

    private static final Logger LOGGER = new Logger(Main.class);

    /**
     * @param args
     *            the arguments (not used)
     */
    public static void main(String[] args) {

        Locale.setDefault(Conf.getLocale());

        LOGGER.info("Start {}", I18n.TITLE.getI18n());

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                    new MainFrame();

                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException
                        | ProcessorException e) {
                    LOGGER.error(e, "Cannot initialize theme");
                }
            }
        });
    }
}
