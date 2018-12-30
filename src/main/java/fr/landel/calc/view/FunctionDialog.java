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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import fr.landel.calc.utils.FrameUtils;
import fr.landel.calc.utils.StringUtils;

public class FunctionDialog extends JDialog implements Dialog {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 3551316166351059808L;

    private static final String ERROR_OPEN = "<html><font style='color:red'><b>";
    private static final String ERROR_CLOSE = "</b></font></html>";
    private static final String NEWLINE = "<br />";

    private MainFrame parent;

    private final JPanel panel = new JPanel();

    public static int maxParams = Functions.maxParams();

    private final JLabel labelFunction = new JLabel();
    private final JLabel labelDescription = new JLabel();
    private final JLabel labelError = new JLabel();

    private final List<JLabel> labels = new ArrayList<>();
    private final List<JTextField> fields = new ArrayList<>();

    private JButton buttonInsert;
    private JButton buttonCancel;

    private Functions function;

    public FunctionDialog(final MainFrame parent) {
        super();
        this.parent = parent;

        this.initComponents();
        this.resetPosition();
    }

    private void initComponents() {
        updateI18n(I18n.DIALOG_FUNCTION, this::setTitle);

        Images.CALCULATOR_16.getIcon().ifPresent(this::setIconImage);
        this.setModal(true);
        this.setResizable(false);

        labelError.setVisible(false);
        labelFunction.setText(StringUtils.EMPTY);
        labelDescription.setText(StringUtils.EMPTY);

        function = Functions.ABS;

        for (int i = 0; i < maxParams; ++i) {
            this.labels.add(new JLabel());
            JTextField field = new JTextField();
            this.fields.add(field);
            this.addFocusListener(field);
        }

        buttonInsert = buildButton(I18n.DIALOG_BUTTON_INSERT, new Dimension(70, BUTTON_HEIGHT), this::buttonInsertActionPerformed);
        buttonCancel = buildButton(I18n.DIALOG_BUTTON_CANCEL, new Dimension(70, BUTTON_HEIGHT), this::buttonCancelActionPerformed);

        final FunctionDialog dialog = this;
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent evt) {
                dialog.buttonCancelActionPerformed(null);
            }
        });

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

    private void setPanelLayout() {
        final GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);

        final ParallelGroup hLabelGroup = layout.createParallelGroup(LEADING);
        final ParallelGroup hFieldGroup = layout.createParallelGroup(LEADING);

        for (int i = 0; i < maxParams; ++i) {
            hLabelGroup.addComponent(labels.get(i));
            hFieldGroup.addComponent(fields.get(i));
        }

        // @formatter:off
        layout.setHorizontalGroup(layout.createParallelGroup(LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(labelError)
                        .addContainerGap())
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(labelFunction)
                        .addContainerGap())
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(labelDescription)
                        .addContainerGap())
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(hLabelGroup)
                        .addGap(10)
                        .addGroup(hFieldGroup)));
        
        final SequentialGroup vGroup = layout.createSequentialGroup()
                .addComponent(labelError)
                .addPreferredGap(UNRELATED)
                .addComponent(labelFunction)
                .addPreferredGap(UNRELATED)
                .addComponent(labelDescription);
        
        for (int i=0; i < maxParams; ++i) {
            vGroup.addPreferredGap(UNRELATED)
                    .addGroup(layout.createParallelGroup(BASELINE)
                            .addComponent(labels.get(i))
                            .addComponent(fields.get(i)));
        }
        
        layout.setVerticalGroup(layout.createParallelGroup(LEADING)
                .addGroup(TRAILING, vGroup));
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
                                .addComponent(buttonInsert, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                                .addContainerGap(5, 5)
                                .addComponent(buttonCancel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                                .addPreferredGap(RELATED, 134, MAX_SIZE)))
                .addContainerGap(10, 10));
        
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                .addPreferredGap(RELATED)
                .addGroup(layout.createParallelGroup()
                        .addComponent(buttonInsert, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                        .addComponent(buttonCancel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
                .addContainerGap(DEFAULT_SIZE, MAX_SIZE));
        // @formatter:on
    }

    public void show(final Functions function, final ActionEvent event) {
        function.updateI18n();
        labelFunction.setText(function.toString());
        updateI18n(function.getI18n(), labelDescription::setText);

        if (function.hasParams()) {
            IntStream.range(0, maxParams).forEach(i -> {
                if (i < function.getParamsCount()) {
                    labels.get(i).setText(function.getParams()[i].getI18n().getI18n());
                    fields.get(i).setText(StringUtils.EMPTY);
                    labels.get(i).setVisible(true);
                    fields.get(i).setVisible(true);
                } else {
                    labels.get(i).setVisible(false);
                    fields.get(i).setVisible(false);
                }
            });

            this.function = function;

            this.pack();

            this.resetPosition();

            this.show(event);
        } else {
            parent.insertText(function.toString());
        }
    }

    @Override
    public void show(final ActionEvent event) {
        FrameUtils.setCentered(this, parent);

        labelError.setVisible(false);
        this.setVisible(true);

        fields.get(0).requestFocus();
    }

    private void addFocusListener(final JTextField field) {
        final int index = fields.indexOf(field);
        field.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent e) {
                if (!fields.contains(e.getOppositeComponent())) {
                    labelFunction.setText(function.toString());
                }
            }

            @Override
            public void focusGained(FocusEvent e) {
                if (index < function.getParamsCount()) {
                    final String text = function.getFocusParams()[index];
                    labelFunction.setText(text);
                }
            }
        });
    }

    private void buttonInsertActionPerformed(ActionEvent evt) {
        boolean valid = true;

        if (this.function != null) {
            final String[] params = fields.subList(0, this.function.getParamsCount()).stream().map(JTextField::getText).toArray(String[]::new);

            final List<I18n> errors = this.function.check(params);
            valid = errors.isEmpty();

            if (valid) {
                parent.insertText(this.function.inject(params));
            } else {
                labelError.setText(errors.stream().map(e -> e.getI18n()).collect(Collectors.joining(NEWLINE, ERROR_OPEN, ERROR_CLOSE)));
            }

            labelError.setVisible(!valid);

            if (!valid) {
                this.pack();
            }
        }

        this.setVisible(!valid);
    }

    private void buttonCancelActionPerformed(ActionEvent evt) {
        this.setVisible(false);
    }
}