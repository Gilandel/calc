package fr.landel.calc.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.BASELINE;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.GroupLayout.Alignment.TRAILING;
import static javax.swing.LayoutStyle.ComponentPlacement.RELATED;
import static javax.swing.LayoutStyle.ComponentPlacement.UNRELATED;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

import fr.landel.calc.config.Conf;
import fr.landel.calc.config.Configuration;
import fr.landel.calc.config.I18n;
import fr.landel.calc.config.Images;
import fr.landel.calc.utils.FrameUtils;
import fr.landel.calc.utils.StringUtils;

public class PreferencesDialog extends JDialog implements Dialog {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 355131616351059808L;

    private static final Integer HISTORY_MIN = 1;
    private static final Integer HISTORY_MAX = 1_000;
    private static final Integer HISTORY_STEP = 1;

    private MainFrame parent;
    private String laf;

    private List<String> lookAndFeels;

    private final JPanel panel = new JPanel();
    private final JPanel panelHistory = new JPanel();
    private final JPanel panelTheme = new JPanel();
    private final JPanel panelLanguage = new JPanel();

    private final JCheckBox saveHistory = new JCheckBox();
    private final JLabel labelSize = new JLabel();
    private final JSpinner spinnerSize = new JSpinner();
    private final JLabel labelTheme = new JLabel();
    private final JComboBox<String> comboBoxTheme = new JComboBox<>();
    private final JLabel labelLanguage = new JLabel();
    private final JComboBox<String> comboBoxLanguage = new JComboBox<>();

    private JButton buttonOk;
    private JButton buttonCancel;

    public PreferencesDialog(final MainFrame parent, final String laf) {
        super();
        this.parent = parent;
        this.laf = laf;

        this.initComponents();
        this.resetPosition();
    }

    private void initComponents() {
        updateI18n(I18n.DIALOG_PREFERENCES, this::setTitle);

        Images.CALCULATOR_16.getIcon().ifPresent(this::setIconImage);
        this.setModal(true);
        this.setResizable(false);

        updateI18n(I18n.DIALOG_PREFERENCES_HISTORY_SAVE, saveHistory::setText);

        updateI18n(I18n.DIALOG_PREFERENCES_HISTORY_MAX, labelSize::setText);
        spinnerSize.setModel(new SpinnerNumberModel(Conf.HISTORY_MAX.getInt().get(), HISTORY_MIN, HISTORY_MAX, HISTORY_STEP));

        saveHistory.addChangeListener(evt -> {
            final boolean enabled = saveHistory.isSelected();

            labelSize.setEnabled(enabled);
            spinnerSize.setEnabled(enabled);
        });

        updateI18n(I18n.DIALOG_PREFERENCES_THEME, labelTheme::setText);
        lookAndFeels = Arrays.asList(FrameUtils.getLookAndFeel());
        lookAndFeels.forEach(comboBoxTheme::addItem);
        comboBoxTheme.addItemListener(evt -> this.parent.resetLookAndFeel(evt.getItem().toString()));

        updateI18n(I18n.DIALOG_PREFERENCES_LANGUAGE, labelLanguage::setText);
        I18n.SUPPORTED_LOCALES_NAME.forEach(comboBoxLanguage::addItem);
        comboBoxLanguage.addItemListener(this::languageChanged);

        this.resetConf();

        buttonOk = buildButton(I18n.DIALOG_BUTTON_OK, new Dimension(60, BUTTON_HEIGHT), this::buttonOkActionPerformed);
        buttonOk.setFocusCycleRoot(true);

        buttonCancel = buildButton(I18n.DIALOG_BUTTON_CANCEL, new Dimension(70, BUTTON_HEIGHT), this::buttonCancelActionPerformed);

        final PreferencesDialog dialog = this;
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent evt) {
                dialog.buttonCancelActionPerformed(null);
            }
        });

        this.setHistoryLayout();
        this.setThemeLayout();
        this.setLanguageLayout();
        this.setPanelLayout();
        this.setDialogLayout();

        this.updateI18n();
    }

    public void resetLookAndFeel(final String laf) {
        FrameUtils.setLookAndFeel(this, laf, true);

        if ("Nimbus".equals(laf)) {
            this.setSize(this.getWidth(), 370);
        } else if ("Metal".equals(laf)) {
            this.setSize(this.getWidth(), 330);
        } else {
            this.setSize(this.getWidth(), 290);
        }

        this.resetPosition();
    }

    public void resetPosition() {
        FrameUtils.setCentered(this, this.parent);
    }

    private void setTitledBorder(final JPanel panel, final I18n i18n) {
        final TitledBorder border = BorderFactory.createTitledBorder(StringUtils.EMPTY);
        panel.setBorder(border);
        updateI18n(i18n, text -> {
            border.setTitle(text);
            panel.repaint();
        });
    }

    private void setHistoryLayout() {
        final GroupLayout layout = new GroupLayout(panelHistory);
        panelHistory.setLayout(layout);
        setTitledBorder(panelHistory, I18n.DIALOG_PREFERENCES_HISTORY);

        // @formatter:off
        layout.setHorizontalGroup(layout.createParallelGroup(LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(LEADING)
                                .addComponent(saveHistory)))
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(LEADING)
                                .addComponent(labelSize))
                        .addGap(35, 35, 35)
                        .addGroup(layout.createParallelGroup(LEADING)
                                .addComponent(spinnerSize))
                        .addContainerGap()));
        
        layout.setVerticalGroup(layout.createParallelGroup(LEADING)
                .addGroup(TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(BASELINE)
                                .addComponent(saveHistory))
                        .addPreferredGap(UNRELATED)
                        .addGroup(layout.createParallelGroup(BASELINE)
                                .addComponent(labelSize)
                                .addComponent(spinnerSize))
                        .addContainerGap()));
        // @formatter:on
    }

    private void setThemeLayout() {
        final GroupLayout layout = new GroupLayout(panelTheme);
        panelTheme.setLayout(layout);
        setTitledBorder(panelTheme, I18n.DIALOG_PREFERENCES_THEME);

        // @formatter:off
        layout.setHorizontalGroup(layout.createParallelGroup(LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(LEADING)
                                .addComponent(labelTheme))
                        .addGap(35, 35, 35)
                        .addGroup(layout.createParallelGroup(LEADING)
                                .addComponent(comboBoxTheme))
                        .addContainerGap()));
        
        layout.setVerticalGroup(layout.createParallelGroup(LEADING)
                .addGroup(TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(BASELINE)
                                .addComponent(labelTheme)
                                .addComponent(comboBoxTheme))
                        .addContainerGap()));
        // @formatter:on
    }

    private void setLanguageLayout() {
        final GroupLayout layout = new GroupLayout(panelLanguage);
        panelLanguage.setLayout(layout);
        setTitledBorder(panelLanguage, I18n.DIALOG_PREFERENCES_LANGUAGE);

        // @formatter:off
        layout.setHorizontalGroup(layout.createParallelGroup(LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(LEADING)
                                .addComponent(labelLanguage))
                        .addGap(35, 35, 35)
                        .addGroup(layout.createParallelGroup(LEADING)
                                .addComponent(comboBoxLanguage))
                        .addContainerGap()));
        
        layout.setVerticalGroup(layout.createParallelGroup(LEADING)
                .addGroup(TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(BASELINE)
                                .addComponent(labelLanguage)
                                .addComponent(comboBoxLanguage))
                        .addContainerGap()));
        // @formatter:on
    }

    private void setPanelLayout() {
        final GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);

        // @formatter:off
        layout.setHorizontalGroup(layout.createParallelGroup(LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(LEADING)
                                .addComponent(panelHistory)))
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(LEADING)
                                .addComponent(panelTheme)))
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(LEADING)
                                .addComponent(panelLanguage))));
        
        layout.setVerticalGroup(layout.createParallelGroup(LEADING)
                .addGroup(TRAILING, layout.createSequentialGroup()
                        .addComponent(panelHistory)
                        .addGap(10)
                        .addComponent(panelTheme)
                        .addGap(10)
                        .addComponent(panelLanguage)));
        // @formatter:on
    }

    private void setDialogLayout() {
        final GroupLayout layout = new GroupLayout(this.getContentPane());

        this.getContentPane().setLayout(layout);

        // @formatter:off
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addContainerGap(10, 10)
                .addGroup(layout.createParallelGroup(LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(panel, DEFAULT_SIZE, DEFAULT_SIZE, MAX_SIZE))
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap(135, MAX_SIZE)
                                .addComponent(buttonOk, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                                .addContainerGap(5, 5)
                                .addComponent(buttonCancel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                                .addPreferredGap(RELATED, 134, MAX_SIZE)))
                .addContainerGap(10, 10));
        
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                .addPreferredGap(RELATED)
                .addGroup(layout.createParallelGroup()
                        .addComponent(buttonOk, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                        .addComponent(buttonCancel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
                .addContainerGap(DEFAULT_SIZE, MAX_SIZE));
        // @formatter:on
    }

    private void resetConf() {
        saveHistory.setSelected(Conf.HISTORY_SAVE.getBoolean().get());
        spinnerSize.setValue(Conf.HISTORY_MAX.getInt().get());
        comboBoxTheme.setSelectedIndex(lookAndFeels.indexOf(laf));
        comboBoxLanguage.setSelectedIndex(I18n.SUPPORTED_LOCALES.indexOf(Locale.getDefault()));
    }

    @Override
    public void show(final ActionEvent event) {
        this.resetPosition();
        this.setVisible(true);
        buttonOk.requestFocus();
    }

    private void languageChanged(final ItemEvent evt) {
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            Locale.setDefault(I18n.SUPPORTED_LOCALES.get(comboBoxLanguage.getSelectedIndex()));

            this.updateI18n();
        }
    }

    private void buttonOkActionPerformed(ActionEvent evt) {
        Conf.HISTORY_SAVE.set(saveHistory.isSelected());
        Conf.HISTORY_MAX.set(spinnerSize.getValue());

        this.laf = comboBoxTheme.getSelectedItem().toString();
        Conf.THEME.set(this.laf);

        Locale.setDefault(I18n.SUPPORTED_LOCALES.get(comboBoxLanguage.getSelectedIndex()));
        Conf.LOCALE.set(I18n.getCurrentLocale());

        Configuration.save();

        this.setVisible(false);
    }

    private void buttonCancelActionPerformed(ActionEvent evt) {
        this.parent.resetLookAndFeel(this.laf);
        this.resetConf();

        this.setVisible(false);
    }
}
