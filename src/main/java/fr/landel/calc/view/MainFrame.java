package fr.landel.calc.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.GroupLayout.Alignment.TRAILING;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.ListSelectionEvent;

import fr.landel.calc.config.Conf;
import fr.landel.calc.config.Configuration;
import fr.landel.calc.config.Formula;
import fr.landel.calc.utils.ClipboardUtils;
import fr.landel.calc.utils.FrameUtils;
import fr.landel.calc.utils.StringUtils;

/**
 * (Description)
 *
 * @since Dec 6, 2018
 * @author Gilles
 *
 */
public class MainFrame extends JFrame implements Dialog {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 5111170027743866090L;

    private static final Font FONT_MENU = new Font("DejaVu Sans", 0, 11);
    private static final Font FONT_FRAME = new Font("Arial", 0, 10);
    private static final Font FONT_FORMULA = new Font("Tahoma", 0, 14);

    private static final Insets MENU_INSETS = new Insets(2, 0, 2, 2);

    public static final int LEFT = 2;
    public static final int RIGHT = 4;
    public static final int CENTER = 0;

    private static final Dimension DIM_BUTTON = new Dimension(45, 29);

    private String laf;

    private AboutDialog aboutDialog;
    private PreferencesDialog preferencesDialog;
    private FunctionDialog functionDialog;

    private JCheckBoxMenuItem itemViewKeyboard;

    private JPanel bottomPanel;
    private ButtonGroup groupPrecision;
    private CaretHighlighter highlighter;

    private JMenuItem itemEditCut, itemEditCopy, itemEditDelete, itemEditClear;

    private JPopupMenu popupFormula = new JPopupMenu();
    private JMenuItem itemFormulaCut, itemFormulaCopy;

    private JTextArea textAreaFormula;
    private JList<String> screenList;

    private MainFrameList mainFrameList;

    private final List<Formula> formulas;

    public MainFrame() {
        super();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Images.CALCULATOR_16.getIcon().ifPresent(this::setIconImage);

        this.laf = Conf.THEME.getString().get();
        this.aboutDialog = new AboutDialog(this);
        this.preferencesDialog = new PreferencesDialog(this, this.laf);
        this.functionDialog = new FunctionDialog(this);

        this.formulas = Conf.getFormulas();

        this.initMenu();
        this.initComponents();
        this.initPopupMenu();
        this.initFrame();
    }

    private void initPopupMenu() {
        itemFormulaCut = add(popupFormula, setMenuItem(I18n.MENU_EDIT_CUT, false), this::clipboardCut);
        itemFormulaCopy = add(popupFormula, setMenuItem(I18n.MENU_EDIT_COPY, false), this::clipboardCopy);
        add(popupFormula, setMenuItem(I18n.MENU_EDIT_PASTE), this::clipboardPaste);

        this.textAreaFormula.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                listMouseClicked(e);
            }
        });
    }

    private void initMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuFile = add(menuBar, setMenu(I18n.MENU_FILE));
        add(menuFile, setMenuItem(I18n.MENU_FILE_CLOSE), this::windowClosing);

        JMenu menuEdit = add(menuBar, setMenu(I18n.MENU_EDIT));
        itemEditCut = add(menuEdit, setMenuItem(I18n.MENU_EDIT_CUT, false), this::clipboardCut);
        itemEditCopy = add(menuEdit, setMenuItem(I18n.MENU_EDIT_COPY, false), this::clipboardCopy);
        add(menuEdit, setMenuItem(I18n.MENU_EDIT_PASTE), this::clipboardPaste);
        menuEdit.add(new JSeparator());
        itemEditDelete = add(menuEdit, setMenuItem(I18n.MENU_EDIT_DELETE, false), this::deleteHistory);
        itemEditClear = add(menuEdit, setMenuItem(I18n.MENU_EDIT_CLEAR, false), this::clearHistory);

        JMenu menuView = add(menuBar, setMenu(I18n.MENU_VIEW));
        itemViewKeyboard = add(menuView, setCheckBoxMenuItem(null, I18n.MENU_VIEW_KEYBOARD), this::splitActionListener);

        JMenu menuFunctions = menuBar.add(setMenu(I18n.MENU_FUNCTIONS));
        JMenu menuMath = add(menuFunctions, setMenu(I18n.MENU_MATH));
        add(menuMath, setMenuItem(I18n.MENU_FUNCTIONS_ABS), this.showFunctionDialog(Functions.ABS));
        add(menuMath, setMenuItem(I18n.MENU_FUNCTIONS_ACOS), this.showFunctionDialog(Functions.ACOS));
        add(menuMath, setMenuItem(I18n.MENU_FUNCTIONS_ASIN), this.showFunctionDialog(Functions.ASIN));
        add(menuMath, setMenuItem(I18n.MENU_FUNCTIONS_ATAN), this.showFunctionDialog(Functions.ATAN));
        add(menuMath, setMenuItem(I18n.MENU_FUNCTIONS_CEIL), this.showFunctionDialog(Functions.CEIL));
        add(menuMath, setMenuItem(I18n.MENU_FUNCTIONS_COS), this.showFunctionDialog(Functions.COS));
        add(menuMath, setMenuItem(I18n.MENU_FUNCTIONS_EXP), this.showFunctionDialog(Functions.EXP));
        add(menuMath, setMenuItem(I18n.MENU_FUNCTIONS_FACT), this.showFunctionDialog(Functions.FACT));
        add(menuMath, setMenuItem(I18n.MENU_FUNCTIONS_FLOOR), this.showFunctionDialog(Functions.FLOOR));
        add(menuMath, setMenuItem(I18n.MENU_FUNCTIONS_LOG), this.showFunctionDialog(Functions.LOG));
        add(menuMath, setMenuItem(I18n.MENU_FUNCTIONS_LN), this.showFunctionDialog(Functions.LN));
        add(menuMath, setMenuItem(I18n.MENU_FUNCTIONS_PI), this.showFunctionDialog(Functions.PI));
        add(menuMath, setMenuItem(I18n.MENU_FUNCTIONS_POW), this.showFunctionDialog(Functions.POW));
        add(menuMath, setMenuItem(I18n.MENU_FUNCTIONS_ROUND), this.showFunctionDialog(Functions.ROUND));
        add(menuMath, setMenuItem(I18n.MENU_FUNCTIONS_SIN), this.showFunctionDialog(Functions.SIN));
        add(menuMath, setMenuItem(I18n.MENU_FUNCTIONS_SQR), this.showFunctionDialog(Functions.SQR));
        add(menuMath, setMenuItem(I18n.MENU_FUNCTIONS_TAN), this.showFunctionDialog(Functions.TAN));
        JMenu menuTime = add(menuFunctions, setMenu(I18n.MENU_FUNCTIONS_TIME));
        add(menuTime, setMenuItem(I18n.MENU_FUNCTIONS_YEAR), this.showFunctionDialog(Functions.YEARS));
        add(menuTime, setMenuItem(I18n.MENU_FUNCTIONS_MONTH), this.showFunctionDialog(Functions.MONTH));
        add(menuTime, setMenuItem(I18n.MENU_FUNCTIONS_WEEK), this.showFunctionDialog(Functions.WEEK));
        add(menuTime, setMenuItem(I18n.MENU_FUNCTIONS_DAY), this.showFunctionDialog(Functions.DAY));
        add(menuTime, setMenuItem(I18n.MENU_FUNCTIONS_HOURS), this.showFunctionDialog(Functions.HOURS));
        add(menuTime, setMenuItem(I18n.MENU_FUNCTIONS_MINUTES), this.showFunctionDialog(Functions.MINUTES));
        add(menuTime, setMenuItem(I18n.MENU_FUNCTIONS_SECONDS), this.showFunctionDialog(Functions.SECONDS));
        add(menuTime, setMenuItem(I18n.MENU_FUNCTIONS_MILLISECONDS), this.showFunctionDialog(Functions.MILLISECONDS));
        add(menuTime, setMenuItem(I18n.MENU_FUNCTIONS_MICROSECONDS), this.showFunctionDialog(Functions.MICROSECONDS));
        add(menuTime, setMenuItem(I18n.MENU_FUNCTIONS_NANOSECONDS), this.showFunctionDialog(Functions.NANOSECONDS));
        add(menuTime, setMenuItem(I18n.MENU_FUNCTIONS_NOW), this.showFunctionDialog(Functions.NOW));

        JMenu menuParameters = add(menuBar, setMenu(I18n.MENU_SETTINGS));
        add(menuParameters, setCheckBoxMenuItem(Conf.RADIAN, I18n.MENU_SETTINGS_RADIAN), this.setParameter(Conf.RADIAN));
        add(menuParameters, setCheckBoxMenuItem(Conf.EXACT, I18n.MENU_SETTINGS_EXACT), this.setParameter(Conf.EXACT));
        add(menuParameters, setCheckBoxMenuItem(Conf.SCIENTIFIC, I18n.MENU_SETTINGS_SCIENTIFIC), this.setParameter(Conf.SCIENTIFIC));
        JMenu itemPrecision = add(menuParameters, setMenu(I18n.MENU_SETTINGS_ACCURACY));
        groupPrecision = new ButtonGroup();
        for (int i = 0; i <= 15; i++) {
            add(itemPrecision, groupPrecision, setPrecisionMenuItem(i), this.changePrecision(i));
        }
        menuParameters.add(new JSeparator());
        add(menuParameters, setMenuItem(I18n.MENU_SETTINGS_PREFERENCES), preferencesDialog::show);

        JMenu menuAbout = add(menuBar, setMenu(I18n.MENU_HELP));
        add(menuAbout, setMenuItem(I18n.MENU_HELP_ABOUT), aboutDialog::show);

        this.setJMenuBar(menuBar);
    }

    private void initComponents() {
        final JPanel topPanel = new JPanel();

        final JScrollPane screen = new JScrollPane();

        screenList = new JList<>();
        mainFrameList = new MainFrameList(this, screenList, formulas);
        mainFrameList.addCounterListener(this::updateList);
        screenList.setBackground(new Color(245, 245, 255));
        screenList.setDragEnabled(true);
        screenList.setListData(new String[0]);
        screenList.addListSelectionListener(this::updateListSelection);

        screen.setViewportView(screenList);

        final JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBackground(Color.BLACK);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setMaximumSize(new Dimension(MAX_SIZE, 28));
        scrollPane.setPreferredSize(new Dimension(373, 28));

        textAreaFormula = new JTextArea();
        highlighter = new CaretHighlighter(textAreaFormula);
        textAreaFormula.setHighlighter(highlighter.getHighlighter());
        textAreaFormula.setBackground(new Color(245, 245, 255));
        textAreaFormula.setFont(FONT_FORMULA);
        textAreaFormula.setRows(1);
        textAreaFormula.setColumns(20);
        textAreaFormula.setTabSize(0);
        textAreaFormula.setDragEnabled(true);
        textAreaFormula.setMargin(new Insets(4, 2, 2, 2));
        textAreaFormula.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                formulaKeyPressed(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                formulaKeyReleased(e);
            }
        });
        textAreaFormula.addCaretListener(highlighter::updateCaret);
        textAreaFormula.addCaretListener(this::updateCaret);
        updateI18n(I18n.FRAME_INPUT, textAreaFormula::setToolTipText);
        scrollPane.setViewportView(textAreaFormula);

        final JButton buttonEqual = buildButton(DIM_BUTTON, null);
        buttonEqual.setText("=");

        bottomPanel = new JPanel();
        final JPanel digits = new JPanel();
        final JPanel operators = new JPanel();

        final JButton button0 = buildButton("0");
        final JButton button1 = buildButton("1");
        final JButton button2 = buildButton("2");
        final JButton button3 = buildButton("3");
        final JButton button4 = buildButton("4");
        final JButton button5 = buildButton("5");
        final JButton button6 = buildButton("6");
        final JButton button7 = buildButton("7");
        final JButton button8 = buildButton("8");
        final JButton button9 = buildButton("9");
        final JButton buttonDot = buildButton(".");
        final JButton buttonAdd = buildButton("+");
        final JButton buttonSubstract = buildButton("-");
        final JButton buttonMultiply = buildButton("*");
        final JButton buttonDevide = buildButton("/");
        final JButton buttonPercent = buildButton("%");
        final JButton buttonPower = buildButton("^");
        final JButton buttonParenthesisOpen = buildButton("(");
        final JButton buttonParenthesisClose = buildButton(")");

        final JPanel panelLeft = new JPanel();
        final JPanel panelRight = new JPanel();

        final GroupLayout tLayout = new GroupLayout(topPanel);
        topPanel.setLayout(tLayout);

        // @formatter:off
        tLayout.setHorizontalGroup(tLayout.createParallelGroup()
                .addGroup(tLayout.createSequentialGroup()
                        .addComponent(screen, DEFAULT_SIZE, 289, MAX_SIZE))
                .addGroup(tLayout.createSequentialGroup()
                        .addContainerGap(5, 5)
                        .addComponent(scrollPane, DEFAULT_SIZE, 218, MAX_SIZE)
                        .addContainerGap(5, 5)
                        .addComponent(buttonEqual, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                        .addContainerGap(5, 5)));

        tLayout.setVerticalGroup(tLayout.createSequentialGroup()
                .addComponent(screen, DEFAULT_SIZE, 100, MAX_SIZE)
                .addGroup(tLayout.createParallelGroup()
                        .addGroup(tLayout.createSequentialGroup()
                                .addGap(6)
                                .addComponent(scrollPane, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
                        .addGroup(tLayout.createSequentialGroup()
                                .addGap(5)
                                .addComponent(buttonEqual, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)))
                .addGap(5, 5, 5));
        // @formatter:on

        final GroupLayout dLayout = new GroupLayout(digits);
        digits.setLayout(dLayout);

        // @formatter:off
        dLayout.setHorizontalGroup(dLayout.createParallelGroup()
                .addGroup(LEADING, dLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(button7)
                        .addGap(2)
                        .addComponent(button8)
                        .addGap(2)
                        .addComponent(button9)
                        .addContainerGap())
                .addGroup(LEADING, dLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(button4)
                        .addGap(2)
                        .addComponent(button5)
                        .addGap(2)
                        .addComponent(button6)
                        .addContainerGap())
                .addGroup(LEADING, dLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(button1)
                        .addGap(2)
                        .addComponent(button2)
                        .addGap(2)
                        .addComponent(button3)
                        .addContainerGap())
                .addGroup(LEADING, dLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(button0)
                        .addGap(2)
                        .addComponent(buttonDot)
                        .addContainerGap()));
        
        dLayout.setVerticalGroup(dLayout.createParallelGroup()
                .addGroup(LEADING, dLayout.createSequentialGroup()
                        .addComponent(button7)
                        .addGap(2)
                        .addComponent(button4)
                        .addGap(2)
                        .addComponent(button1)
                        .addGap(2)
                        .addComponent(button0))
                .addGroup(LEADING, dLayout.createSequentialGroup()
                        .addComponent(button8)
                        .addGap(2)
                        .addComponent(button5)
                        .addGap(2)
                        .addComponent(button2)
                        .addGap(2)
                        .addComponent(buttonDot))
                .addGroup(LEADING, dLayout.createSequentialGroup()
                        .addComponent(button9)
                        .addGap(2)
                        .addComponent(button6)
                        .addGap(2)
                        .addComponent(button3)));
        // @formatter:on

        final GroupLayout oLayout = new GroupLayout(operators);
        operators.setLayout(oLayout);

        // @formatter:off
        oLayout.setHorizontalGroup(oLayout.createParallelGroup()
                .addGroup(TRAILING, oLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(buttonAdd)
                        .addGap(2)
                        .addComponent(buttonPercent)
                        .addContainerGap())
                .addGroup(TRAILING, oLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(buttonSubstract)
                        .addGap(2)
                        .addComponent(buttonPower)
                        .addContainerGap())
                .addGroup(TRAILING, oLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(buttonMultiply)
                        .addGap(2)
                        .addComponent(buttonParenthesisOpen)
                        .addContainerGap())
                .addGroup(TRAILING, oLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(buttonDevide)
                        .addGap(2)
                        .addComponent(buttonParenthesisClose)
                        .addContainerGap()));

        oLayout.setVerticalGroup(oLayout.createParallelGroup()
                .addGroup(LEADING, oLayout.createSequentialGroup()
                        .addComponent(buttonAdd)
                        .addGap(2)
                        .addComponent(buttonSubstract)
                        .addGap(2)
                        .addComponent(buttonMultiply)
                        .addGap(2)
                        .addComponent(buttonDevide))
                .addGroup(LEADING, oLayout.createSequentialGroup()
                        .addComponent(buttonPercent)
                        .addGap(2)
                        .addComponent(buttonPower)
                        .addGap(2)
                        .addComponent(buttonParenthesisOpen)
                        .addGap(2)
                        .addComponent(buttonParenthesisClose)));
        // @formatter:on

        final GroupLayout bLayout = new GroupLayout(bottomPanel);
        bottomPanel.setLayout(bLayout);

        // @formatter:off
        bLayout.setHorizontalGroup(bLayout.createParallelGroup()
                .addGroup(LEADING, bLayout.createSequentialGroup()
                        .addComponent(panelLeft, DEFAULT_SIZE, 100, MAX_SIZE)
                        .addComponent(digits)
                        .addContainerGap()
                        .addComponent(operators)
                        .addComponent(panelRight, DEFAULT_SIZE, 100, MAX_SIZE)));

        bLayout.setVerticalGroup(bLayout.createParallelGroup()
                .addGroup(LEADING, bLayout.createSequentialGroup()
                        .addComponent(panelLeft))
                .addGroup(LEADING, bLayout.createSequentialGroup()
                        .addComponent(digits)
                        .addGap(4))
                .addGroup(LEADING, bLayout.createSequentialGroup()
                        .addComponent(operators)
                        .addGap(4))
                .addGroup(LEADING, bLayout.createSequentialGroup()
                        .addComponent(panelRight)));
        // @formatter:on

        final BorderLayout mainLayout = new BorderLayout(0, 0);
        this.getContentPane().setLayout(mainLayout);
        this.getContentPane().add(topPanel, BorderLayout.CENTER);
        this.getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        ((JPanel) this.getContentPane()).setBorder(BorderFactory.createEmptyBorder());

        this.updateI18n();

        buttonEqual.addActionListener(this::equalActionListener);
    }

    private void initFrame() {
        updateI18n(I18n.TITLE, this::setTitle);
        updateI18n(I18n.TITLE, this::setName);

        this.setSize(Conf.WIDTH.getInt().get(), Conf.HEIGHT.getInt().get());
        this.setMinimumSize(new Dimension(400, 320));

        this.setLocationRelativeTo(null);
        if (Conf.X.getInt().isPresent()) {
            this.setLocation(Conf.X.getInt().get(), Conf.Y.getInt().get());
        }

        this.setExtendedState(Conf.EXTENDED_STATE.getInt().get());

        this.formulas.forEach(mainFrameList::addFormula);

        boolean keyboardState = Conf.KEYBOARD.getBoolean().orElse(true);
        bottomPanel.setVisible(keyboardState);
        itemViewKeyboard.setState(keyboardState);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setFont(FONT_FRAME);

        screenList.ensureIndexIsVisible(screenList.getModel().getSize() - 1);

        this.show(null);

        textAreaFormula.requestFocusInWindow();

        final MainFrame frame = this;

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent evt) {
                frame.windowClosing(evt);
            }
        });
    }

    public void resetLookAndFeel(final String laf) {
        FrameUtils.setLookAndFeel(this, laf, true);

        if (this.aboutDialog != null) {
            this.aboutDialog.resetLookAndFeel(laf);
        }
        if (this.preferencesDialog != null) {
            this.preferencesDialog.resetLookAndFeel(laf);
        }
        if (this.functionDialog != null) {
            this.functionDialog.resetLookAndFeel(laf);
        }
    }

    public void resetPosition() {
        FrameUtils.setLookAndFeel(this, this.laf, true);
    }

    @Override
    public void show(final ActionEvent event) {
        this.resetPosition();
        this.setVisible(true);
    }

    private void windowClosing(final ActionEvent evt) {
        windowClosing();
        this.dispose();
    }

    private void windowClosing(final WindowEvent evt) {
        windowClosing();
    }

    private void windowClosing() {
        final int state = this.getExtendedState();

        Conf.EXTENDED_STATE.set(this.getExtendedState());

        if (Frame.MAXIMIZED_HORIZ == state) {
            final Rectangle bounds = this.getBounds();
            Conf.Y.set(bounds.y);
            Conf.HEIGHT.set(bounds.height);

        } else if (Frame.MAXIMIZED_VERT == state) {
            final Rectangle bounds = this.getBounds();
            Conf.X.set(bounds.x);
            Conf.WIDTH.set(bounds.width);

        } else if (Frame.NORMAL == state) {
            final Rectangle bounds = this.getBounds();
            Conf.X.set(bounds.x);
            Conf.Y.set(bounds.y);
            Conf.WIDTH.set(bounds.width);
            Conf.HEIGHT.set(bounds.height);
        }

        if (Conf.HISTORY_SAVE.getBoolean().get()) {
            final int max = Conf.HISTORY_MAX.getInt().get();
            int toRemove = this.formulas.size() - max;

            final Iterator<Formula> it = this.formulas.iterator();
            while (it.hasNext() && toRemove-- > 0) {
                it.next();
                it.remove();
            }

            Conf.clearFormulas();
            int i = 0;
            for (Formula formula : this.formulas) {
                Conf.setFormula(i++, formula);
            }
        }

        Configuration.save();
    }

    private JButton buildButton(final String text) {
        final JButton button = buildButton(DIM_BUTTON, this::buttonActionListener);
        button.setText(text);
        return button;
    }

    private void buttonActionListener(final ActionEvent evt) {
        insertText(((JButton) evt.getSource()).getText());
    }

    private ActionListener setParameter(final Conf conf) {
        return evt -> conf.set(((JCheckBoxMenuItem) evt.getSource()).getState());
    }

    private ActionListener changePrecision(final int precision) {
        return evt -> Conf.PRECISION.set(precision);
    }

    private JMenu add(final JMenuBar menu, final JMenu subMenu) {
        menu.add(subMenu);
        return subMenu;
    }

    private JMenu add(final JMenu menu, final JMenu subMenu) {
        menu.add(subMenu);
        return subMenu;
    }

    private JMenuItem add(final JMenu menu, final JMenuItem subMenu, final ActionListener actionListener) {
        menu.add(subMenu);
        if (actionListener != null) {
            subMenu.addActionListener(actionListener);
        }
        return subMenu;
    }

    public JMenuItem add(final JPopupMenu menu, final JMenuItem subMenu, final ActionListener actionListener) {
        menu.add(subMenu);
        if (actionListener != null) {
            subMenu.addActionListener(actionListener);
        }
        return subMenu;
    }

    private JCheckBoxMenuItem add(final JMenu menu, final JCheckBoxMenuItem subMenu, final ActionListener actionListener) {
        menu.add(subMenu);
        if (actionListener != null) {
            subMenu.addActionListener(actionListener);
        }
        return subMenu;
    }

    public JRadioButtonMenuItem add(final JMenu menu, final ButtonGroup group, final JRadioButtonMenuItem subMenu, final ActionListener actionListener) {

        menu.add(subMenu);
        group.add(subMenu);
        if (actionListener != null) {
            subMenu.addActionListener(actionListener);
        }
        return subMenu;
    }

    public JMenu setMenu(final I18n i18n, final String... params) {
        return setMenuItem(new JMenu(), true, i18n, params);
    }

    public JMenuItem setMenuItem(final I18n i18n, final String... params) {
        return setMenuItem(i18n, true, params);
    }

    public JMenuItem setMenuItem(final I18n i18n, final boolean enabled, final String... params) {
        return setMenuItem(new JMenuItem(), enabled, i18n, params);
    }

    private JCheckBoxMenuItem setCheckBoxMenuItem(final Conf conf, final I18n i18n, final String... params) {
        final JCheckBoxMenuItem item = setMenuItem(new JCheckBoxMenuItem(), true, i18n, params);

        if (conf != null) {
            item.setState(conf.getBoolean().get());
        }

        return item;
    }

    private JRadioButtonMenuItem setPrecisionMenuItem(final int precision) {
        final I18n i18n;
        if (precision > 1) {
            i18n = I18n.MENU_SETTINGS_DECIMAL_PLURAL;
        } else {
            i18n = I18n.MENU_SETTINGS_DECIMAL;
        }

        final JRadioButtonMenuItem item = setMenuItem(new JRadioButtonMenuItem(), true, i18n, String.valueOf(precision));

        if (Conf.PRECISION.getInt().orElse(3) == precision) {
            item.setSelected(true);
        }
        return item;
    }

    private <T extends JMenuItem> T setMenuItem(final T menu, final boolean enabled, final I18n i18n, final String... params) {
        i18n.getMnemonic().ifPresent(m -> menu.setMnemonic(m));
        i18n.getImage().ifPresent(i -> menu.setIcon(i));
        i18n.getAccelerator().ifPresent(a -> menu.setAccelerator(a));

        menu.setIconTextGap(0);
        menu.setFont(FONT_MENU);
        menu.setMargin(MENU_INSETS);
        menu.setEnabled(enabled);
        updateI18n(i18n, menu::setText, params);

        return menu;
    }

    private void equalActionListener(final ActionEvent evt) {
        mainFrameList.addFormula(textAreaFormula.getText(), "result", true);
        // TODO no error
        textAreaFormula.setText(StringUtils.EMPTY);
    }

    private void splitActionListener(final ActionEvent evt) {
        boolean visible = bottomPanel.isVisible();
        itemViewKeyboard.setState(!visible);
        bottomPanel.setVisible(!visible);
        Conf.KEYBOARD.set(!visible);
    }

    public void listMouseClicked(final MouseEvent evt) {
        if (evt.getButton() == MouseEvent.BUTTON3) {
            popupFormula.show(evt.getComponent(), evt.getX(), evt.getY());
            textAreaFormula.requestFocus();

        }
    }

    private void clipboardCut(final ActionEvent evt) {
        final String text = textAreaFormula.getSelectedText();
        if (text != null && !text.isEmpty()) {
            ClipboardUtils.setText(text);
            insertText(StringUtils.EMPTY);
        }
    }

    private void clipboardCopy(final ActionEvent evt) {
        ClipboardUtils.setText(textAreaFormula.getSelectedText());
    }

    private void clipboardPaste(final ActionEvent evt) {
        insertText(ClipboardUtils.getText());
    }

    public void deleteHistory(final ActionEvent evt) {
        if (screenList.isSelectionEmpty()) {
            JOptionPane.showMessageDialog(this, I18n.DIALOG_ERROR_SELECTION_EMPTY.getI18n(), I18n.DIALOG_ERROR.getI18n(), JOptionPane.ERROR_MESSAGE);
        } else {
            mainFrameList.removeSelected();
        }
    }

    public void clearHistory(final ActionEvent evt) {
        mainFrameList.clear();
    }

    public void insertText(final String text) {
        final int end = textAreaFormula.getSelectionEnd();
        final int start = textAreaFormula.getSelectionStart();
        if (end - start > 0) {
            textAreaFormula.replaceRange(text, start, end);
        } else {
            textAreaFormula.insert(text, textAreaFormula.getCaretPosition());
        }
    }

    private void formulaKeyPressed(final KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == '\r') {
            mainFrameList.addFormula(textAreaFormula.getText(), "result", true);

        } else if (evt.isControlDown() && evt.getKeyCode() == KeyEvent.VK_V) {
            //
        }
    }

    private void formulaKeyReleased(final KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == '\r') {
            // TODO no error
            textAreaFormula.setText(StringUtils.EMPTY);
        } else if (evt.isControlDown() && evt.getKeyCode() == KeyEvent.VK_V) {
            //
        }
    }

    private ActionListener showFunctionDialog(final Functions function) {
        return evt -> this.functionDialog.show(function, evt);
    }

    private void updateCaret(final CaretEvent evt) {
        final boolean hasSelection = textAreaFormula.getSelectedText() != null;

        itemEditCut.setEnabled(hasSelection);
        itemEditCopy.setEnabled(hasSelection);

        itemFormulaCut.setEnabled(hasSelection);
        itemFormulaCopy.setEnabled(hasSelection);
    }

    private void updateListSelection(final ListSelectionEvent evt) {
        final boolean hasSelection = !screenList.isSelectionEmpty();

        itemEditDelete.setEnabled(hasSelection);
    }

    private void updateList(final Integer count) {
        itemEditClear.setEnabled(count > 0);
    }
}
