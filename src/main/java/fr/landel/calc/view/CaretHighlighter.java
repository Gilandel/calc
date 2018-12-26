package fr.landel.calc.view;

import java.awt.Color;

import javax.swing.JTextArea;
import javax.swing.event.CaretEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

import fr.landel.calc.utils.Logger;

public class CaretHighlighter {

    private static final Logger LOGGER = new Logger(CaretHighlighter.class);

    private final JTextArea textAreaFormula;

    private final Highlighter highlighter;
    private final Highlighter.HighlightPainter painterSel;
    private final Highlighter.HighlightPainter painterOk;
    private final Highlighter.HighlightPainter painterErr;

    // private int textAreaFormulaCaret;

    public CaretHighlighter(final JTextArea textAreaFormula) {
        this.textAreaFormula = textAreaFormula;
        this.highlighter = new DefaultHighlighter();
        this.painterSel = new DefaultHighlighter.DefaultHighlightPainter(new Color(200, 100, 200));
        this.painterOk = new DefaultHighlighter.DefaultHighlightPainter(Color.GREEN);
        this.painterErr = new DefaultHighlighter.DefaultHighlightPainter(Color.RED);
    }

    public Highlighter getHighlighter() {
        return this.highlighter;
    }

    private int getParenthesis(final int start, final boolean next) {
        int inc = 0;
        String text = textAreaFormula.getText();
        int len = text.length();
        if (next) {
            if (start < len) {
                for (int i = start + 1; i < len; i++)
                    if (text.charAt(i) == ')') {
                        if (inc == 0)
                            return i;
                        inc--;
                    } else if (text.charAt(i) == '(')
                        inc++;

            }
        } else if (start >= 0) {
            for (int i = start - 1; i >= 0; i--) {
                if (text.charAt(i) == '(') {
                    if (inc == 0)
                        return i;
                    inc--;
                    continue;
                }
                if (text.charAt(i) == ')')
                    inc++;
            }

        }
        return -1;
    }

    private void highlightText(final int pos) {
        try {
            highlighter.removeAllHighlights();

            final String text = textAreaFormula.getText();
            final int length = text.length();

            if (pos >= 0 && length > 0 && length >= pos) {
                if (pos > 0) {
                    char c = text.charAt(pos - 1);
                    if (c == '(' || c == ')') {
                        int i = getParenthesis(pos - 1, c != ')');
                        if (i >= 0) {
                            highlighter.addHighlight(pos - 1, pos, painterOk);
                            highlighter.addHighlight(i, i + 1, painterOk);
                        } else {
                            highlighter.addHighlight(pos - 1, pos, painterErr);
                        }
                    } else if (length > pos) {
                        c = text.charAt(pos);
                        if (c == '(' || c == ')') {
                            int i = getParenthesis(pos, c != ')');
                            if (i >= 0) {
                                highlighter.addHighlight(pos, pos + 1, painterOk);
                                highlighter.addHighlight(i, i + 1, painterOk);
                            } else {
                                highlighter.addHighlight(pos, pos + 1, painterErr);
                            }
                        }
                    }
                } else {
                    char c = text.charAt(pos);
                    if (c == '(' || c == ')') {
                        int i = getParenthesis(pos, c != ')');
                        if (i >= 0) {
                            highlighter.addHighlight(pos, pos + 1, painterOk);
                            highlighter.addHighlight(i, i + 1, painterOk);
                        } else {
                            highlighter.addHighlight(pos, pos + 1, painterErr);
                        }
                    }
                }
            }
        } catch (BadLocationException e) {
            LOGGER.error(e, "Cannot highlight text");
        }
    }

    public void updateCaret(final CaretEvent evt) {
        int start;
        int end;
        if (evt.getDot() <= evt.getMark()) {
            start = evt.getDot();
            end = evt.getMark();
        } else {
            start = evt.getMark();
            end = evt.getDot();
        }
        try {
            if (start < end) {
                highlighter.removeAllHighlights();
                highlighter.addHighlight(start, end, painterSel);
                textAreaFormula.requestFocus();
            } else {
                highlightText(start);
            }
        } catch (Exception e) {
            LOGGER.error(e, "Cannot update caret");
        }
        // textAreaFormulaCaret = textAreaFormula.getSelectionStart();
    }
}
