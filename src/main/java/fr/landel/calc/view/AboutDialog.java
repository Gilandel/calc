package fr.landel.calc.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.BASELINE;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.GroupLayout.Alignment.TRAILING;
import static javax.swing.LayoutStyle.ComponentPlacement.RELATED;
import static javax.swing.LayoutStyle.ComponentPlacement.UNRELATED;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import fr.landel.calc.config.I18n;
import fr.landel.calc.config.Images;
import fr.landel.calc.utils.FrameUtils;
import fr.landel.calc.utils.StringUtils;
import fr.landel.calc.utils.SystemUtils;

public class AboutDialog extends JDialog implements Dialog {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 8054234548556520700L;

    private static final Function<String, String> MAKE_LINK_EXITED = t -> "<html><font color='#0000FF'>" + t + "</font></html>";
    private static final Function<String, String> MAKE_LINK_ENTERED = t -> MAKE_LINK_EXITED.apply("<u>" + t + "</u>");

    private MainFrame parent;

    private JPanel panel = new JPanel();
    private JLabel icon = new JLabel();
    private JLabel authorKey = new JLabel();
    private JLabel creationDateKey = new JLabel();
    private JLabel linkKey = new JLabel();
    private JLabel emailKey = new JLabel();
    private JLabel authorValue = new JLabel();
    private JLabel creationDateValue = new JLabel();
    private JLabel linkValue = new JLabel();
    private JLabel emailValue = new JLabel();
    private JLabel updateDate = new JLabel();
    private JLabel updateDateValue = new JLabel();
    private JButton buttonOk = new JButton();

    private String link;
    private String email;
    private String linkHTMLExited;
    private String linkHTMLEntered;
    private String emailHTMLExited;
    private String emailHTMLEntered;

    private static final Cursor HAND_CURSOR = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    private static final Cursor DEFAULT_CURSOR = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);

    public AboutDialog(final MainFrame parent) {
        super();
        this.parent = parent;

        this.initComponents();
        this.resetPosition();
    }

    private void initComponents() {
        Images.CALCULATOR_16.getIcon().ifPresent(this::setIconImage);
        this.setModal(true);
        this.setResizable(false);

        Images.CALCULATOR.getImage().ifPresent(icon::setIcon);

        buttonOk = buildButton(I18n.DIALOG_BUTTON_OK, new Dimension(60, BUTTON_HEIGHT), this::buttonOkActionPerformed);

        updateI18n(I18n.DIALOG_ABOUT, this::setTitle);
        final TitledBorder titledBorder = BorderFactory.createTitledBorder(StringUtils.EMPTY);
        panel.setBorder(titledBorder);
        updateI18n(I18n.DIALOG_ABOUT_PROJECT, titledBorder::setTitle);

        updateI18n(I18n.DIALOG_ABOUT_PROJECT_AUTHOR, authorKey::setText);
        updateI18n(I18n.DIALOG_ABOUT_PROJECT_CREATION, creationDateKey::setText);
        updateI18n(I18n.DIALOG_ABOUT_PROJECT_UPDATE, updateDate::setText);
        updateI18n(I18n.DIALOG_ABOUT_PROJECT_LINK, linkKey::setText);
        updateI18n(I18n.DIALOG_ABOUT_PROJECT_EMAIL, emailKey::setText);
        updateI18n(I18n.DIALOG_ABOUT_PROJECT_AUTHOR_TEXT, authorValue::setText);
        updateI18n(I18n.DIALOG_ABOUT_PROJECT_CREATION_DATE, creationDateValue::setText);
        updateI18n(I18n.DIALOG_ABOUT_PROJECT_UPDATE_DATE, updateDateValue::setText);
        updateI18n(I18n.DIALOG_ABOUT_PROJECT_LINK_TEXT, text -> {
            this.linkHTMLExited = MAKE_LINK_EXITED.apply(text);
            this.linkHTMLEntered = MAKE_LINK_ENTERED.apply(text);
            linkValue.setText(this.linkHTMLExited);
        });
        updateI18n(I18n.DIALOG_ABOUT_PROJECT_EMAIL_TEXT, text -> {
            this.emailHTMLExited = MAKE_LINK_EXITED.apply(text);
            this.emailHTMLEntered = MAKE_LINK_ENTERED.apply(text);
            emailValue.setText(this.emailHTMLExited);
        });
        updateI18n(I18n.DIALOG_BUTTON_OK, buttonOk::setText);

        this.setPanelLayout();
        this.setDialogLayout();

        this.updateI18n();

        linkValue.addMouseListener(buildMouseAdapter(this::linkMouseClicked, this::linkMouseEntered, this::linkMouseExited));
        emailValue.addMouseListener(buildMouseAdapter(this::emailMouseClicked, this::emailMouseEntered, this::emailMouseExited));
    }

    public void resetLookAndFeel(final String laf) {
        FrameUtils.setLookAndFeel(this, laf, true);

        this.resetPosition();
    }

    public void resetPosition() {
        if (SystemUtils.isLinux()) {
            final Dimension dim = new Dimension();

            dim.width = panel.getX() * 2 + panel.getWidth() + this.getInsets().left + this.getInsets().right;
            dim.height = buttonOk.getHeight() + buttonOk.getY() + panel.getY() + this.getInsets().top + this.getInsets().bottom;

            this.setSize(dim);
            this.setPreferredSize(dim);
        }

        FrameUtils.setCentered(this, this.parent);
    }

    private void setPanelLayout() {
        final GroupLayout layout = new GroupLayout(panel);

        panel.setLayout(layout);

        // @formatter:off
        layout.setHorizontalGroup(layout.createParallelGroup(LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(LEADING)
                                .addComponent(creationDateKey)
                                .addComponent(authorKey)
                                .addComponent(updateDate)
                                .addComponent(linkKey)
                                .addComponent(emailKey))
                        .addGap(35, 35, 35)
                        .addGroup(layout.createParallelGroup(LEADING)
                                .addComponent(linkValue)
                                .addComponent(emailValue)
                                .addComponent(updateDateValue)
                                .addComponent(authorValue)
                                .addComponent(creationDateValue))
                        .addPreferredGap(RELATED, 48, MAX_SIZE)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(icon))
                        .addContainerGap()));
        
        layout.setVerticalGroup(layout.createParallelGroup(LEADING)
                .addGroup(TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(BASELINE)
                                .addComponent(authorKey)
                                .addComponent(authorValue))
                        .addPreferredGap(UNRELATED)
                        .addGroup(layout.createParallelGroup(BASELINE)
                                .addComponent(creationDateKey)
                                .addComponent(creationDateValue))
                        .addPreferredGap(UNRELATED)
                        .addGroup(layout.createParallelGroup(BASELINE)
                                .addComponent(updateDate)
                                .addComponent(updateDateValue))
                        .addPreferredGap(UNRELATED)
                        .addGroup(layout.createParallelGroup(BASELINE)
                                .addComponent(linkKey)
                                .addComponent(linkValue))
                        .addPreferredGap(UNRELATED)
                        .addGroup(layout.createParallelGroup(BASELINE)
                                .addComponent(emailKey)
                                .addComponent(emailValue))
                        .addGap(33, 33, 33))
                .addGroup(layout.createSequentialGroup()
                        .addComponent(icon)
                        .addContainerGap(99, MAX_SIZE)));
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
                                .addPreferredGap(RELATED, 134, MAX_SIZE)))
                .addContainerGap(10, 10));
        
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panel, PREFERRED_SIZE, 165, PREFERRED_SIZE)
                .addPreferredGap(RELATED)
                .addComponent(buttonOk, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                .addContainerGap(DEFAULT_SIZE, MAX_SIZE));
        // @formatter:on
    }

    private void buttonOkActionPerformed(ActionEvent evt) {
        this.setVisible(false);
    }

    public void show(ActionEvent evt) {
        linkValue.setText(this.linkHTMLExited);
        emailValue.setText(this.emailHTMLExited);
        resetPosition();
        this.setVisible(true);
    }

    private MouseAdapter buildMouseAdapter(final Consumer<MouseEvent> mouseClicked, final Consumer<MouseEvent> mouseEntered,
            final Consumer<MouseEvent> mouseExited) {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mouseClicked.accept(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                mouseEntered.accept(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mouseExited.accept(e);
            }
        };
    }

    private void linkMouseClicked(MouseEvent evt) {
        SystemUtils.execLink(this.link);
    }

    private void emailMouseClicked(MouseEvent evt) {
        SystemUtils.execLink("mailto:" + this.email);
    }

    private void linkMouseEntered(MouseEvent evt) {
        linkValue.setText(this.linkHTMLEntered);
        linkValue.setCursor(HAND_CURSOR);
    }

    private void linkMouseExited(MouseEvent evt) {
        linkValue.setText(this.linkHTMLExited);
        linkValue.setCursor(DEFAULT_CURSOR);
    }

    private void emailMouseEntered(MouseEvent evt) {
        emailValue.setText(this.emailHTMLEntered);
        emailValue.setCursor(HAND_CURSOR);
    }

    private void emailMouseExited(MouseEvent evt) {
        emailValue.setText(this.emailHTMLExited);
        emailValue.setCursor(DEFAULT_CURSOR);
    }
}
