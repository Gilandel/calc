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

import javax.swing.GroupLayout;
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

    private MainFrame parent;

    private final JPanel panel = new JPanel();

    private final JLabel labelFunction = new JLabel();
    private final JLabel labelDescription = new JLabel();

    private final JLabel labelP1 = new JLabel();
    private final JTextField fieldP1 = new JTextField();

    private final JLabel labelP2 = new JLabel();
    private final JTextField fieldP2 = new JTextField();

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

        labelFunction.setText(StringUtils.EMPTY);
        labelDescription.setText(StringUtils.EMPTY);

        fieldP1.addFocusListener(this.fieldFocus(fieldP1));
        fieldP2.addFocusListener(this.fieldFocus(fieldP2));

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

        // @formatter:off
        layout.setHorizontalGroup(layout.createParallelGroup(LEADING)
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
                        .addGroup(layout.createParallelGroup(LEADING)
                                .addComponent(labelP1)
                                .addComponent(labelP2))
                        .addGap(10)
                        .addGroup(layout.createParallelGroup(LEADING)
                                .addComponent(fieldP1)
                                .addComponent(fieldP2))));
        
        layout.setVerticalGroup(layout.createParallelGroup(LEADING)
                .addGroup(TRAILING, layout.createSequentialGroup()
                        .addComponent(labelFunction)
                        .addPreferredGap(UNRELATED)
                        .addComponent(labelDescription)
                        .addPreferredGap(UNRELATED)
                        .addGroup(layout.createParallelGroup(BASELINE)
                                .addComponent(labelP1)
                                .addComponent(fieldP1))
                        .addPreferredGap(UNRELATED)
                        .addGroup(layout.createParallelGroup(BASELINE)
                                .addComponent(labelP2)
                                .addComponent(fieldP2))));
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
            function.getParam1().ifPresent(t -> {
                labelP1.setText(t.getI18n());
                fieldP1.setText(StringUtils.EMPTY);
            });

            if (function.getParam2().isPresent()) {
                labelP2.setText(function.getParam2().get().getI18n());
                fieldP2.setText(StringUtils.EMPTY);
                labelP2.setVisible(true);
                fieldP2.setVisible(true);
            } else {
                labelP2.setVisible(false);
                fieldP2.setVisible(false);
            }

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
        this.setVisible(true);

        fieldP1.requestFocus();
    }

    private FocusListener fieldFocus(final JTextField field) {
        return new FocusListener() {
            @Override
            public void focusLost(FocusEvent e) {
                if (!fieldP1.equals(e.getOppositeComponent()) && !fieldP2.equals(e.getOppositeComponent())) {
                    labelFunction.setText(function.toString());
                }
            }

            @Override
            public void focusGained(FocusEvent e) {
                final String text;
                if (fieldP1.equals(field)) {
                    text = function.getFocusParam1();
                } else {
                    text = function.getFocusParam2();
                }
                if (text != null) {
                    labelFunction.setText(text);
                }
            }
        };
    }

    private void buttonInsertActionPerformed(ActionEvent evt) {
        if (this.function != null) {
            parent.insertText(this.function.inject(fieldP1.getText(), fieldP2.getText()));
        }

        this.setVisible(false);
    }

    private void buttonCancelActionPerformed(ActionEvent evt) {
        this.setVisible(false);
    }
}