package fr.landel.calc.view;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.SwingConstants;

import fr.landel.calc.config.I18n;
import fr.landel.calc.utils.StringUtils;

public interface Dialog {

    int MAX_SIZE = 32767;
    int BUTTON_HEIGHT = 29;
    Map<I18n, Map<String[], Set<Consumer<String>>>> MAP_I18N_SETTER = new HashMap<>();

    void pack();

    default void updateI18n() {
        String text;

        // by i18n
        for (Entry<I18n, Map<String[], Set<Consumer<String>>>> entry : MAP_I18N_SETTER.entrySet()) {

            // by parameters
            for (Entry<String[], Set<Consumer<String>>> map : entry.getValue().entrySet()) {

                text = StringUtils.inject(entry.getKey().getI18n(), map.getKey());

                // by consumer
                for (Consumer<String> consumer : map.getValue()) {
                    consumer.accept(text);
                }
            }
        }

        this.pack();
    };

    void resetLookAndFeel(String laf);

    void resetPosition();

    void show(ActionEvent event);

    private void addI18nSetter(final I18n i18n, final Consumer<String> setter, final String... params) {
        final Map<String[], Set<Consumer<String>>> map;
        Set<Consumer<String>> consumers = null;

        if (!MAP_I18N_SETTER.containsKey(i18n)) {
            map = new HashMap<>();
            consumers = new HashSet<>();
            map.put(params, consumers);
            MAP_I18N_SETTER.put(i18n, map);

        } else {
            map = MAP_I18N_SETTER.get(i18n);
            for (Entry<String[], Set<Consumer<String>>> entry : map.entrySet()) {
                if (Arrays.equals(entry.getKey(), params)) {
                    consumers = entry.getValue();
                    break;
                }
            }
            if (consumers == null) {
                consumers = new HashSet<>();
                map.put(params, consumers);
                MAP_I18N_SETTER.put(i18n, map);
            }
        }

        consumers.add(setter);
    }

    default void updateI18n(final I18n i18n, final Consumer<String> setter, final String... params) {
        addI18nSetter(i18n, setter, params);
        setter.accept(StringUtils.inject(i18n.getI18n(), params));
    }

    default JButton buildButton(final I18n i18n, final Dimension dimension, final ActionListener actionListener, final String... params) {
        final JButton button = buildButton(dimension, actionListener);
        addI18nSetter(i18n, button::setText, params);
        return button;
    }

    default JButton buildButton(final Dimension dimension, final ActionListener actionListener) {
        final JButton button = new JButton();
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setPreferredSize(dimension);
        button.setMinimumSize(dimension);
        button.setMaximumSize(dimension);
        if (actionListener != null) {
            button.addActionListener(actionListener);
        }
        return button;
    }
}
