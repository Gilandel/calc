package fr.landel.calc.view;

import java.awt.Color;
import java.util.Arrays;

import javax.swing.JTextArea;
import javax.swing.event.CaretEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

import fr.landel.calc.processor.MainProcessor;
import fr.landel.calc.utils.Logger;

public class CaretHighlighter {

    // TODO analyser le texte a la modification pas au deplacement du curseur
    // precalculer les zones a coloriser

    private static final Logger LOGGER = new Logger(CaretHighlighter.class);

    private final JTextArea textAreaFormula;

    private final Highlighter highlighter;
    private final Highlighter.HighlightPainter painterSel;
    private final Highlighter.HighlightPainter painterOk;
    private final Highlighter.HighlightPainter painterFun;
    private final Highlighter.HighlightPainter painterErr;

    // private int textAreaFormulaCaret;

    public CaretHighlighter(final JTextArea textAreaFormula) {
        this.textAreaFormula = textAreaFormula;
        this.highlighter = new DefaultHighlighter();
        this.painterSel = new DefaultHighlighter.DefaultHighlightPainter(new Color(200, 100, 200));
        this.painterOk = new DefaultHighlighter.DefaultHighlightPainter(Color.GREEN);
        this.painterFun = new DefaultHighlighter.DefaultHighlightPainter(new Color(180, 255, 200));
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
                    if (text.charAt(i) == MainProcessor.PARENTHESIS_CLOSE) {
                        if (inc == 0)
                            return i;
                        inc--;
                    } else if (text.charAt(i) == MainProcessor.PARENTHESIS_OPEN)
                        inc++;

            }
        } else if (start >= 0) {
            for (int i = start - 1; i >= 0; i--) {
                if (text.charAt(i) == MainProcessor.PARENTHESIS_OPEN) {
                    if (inc == 0)
                        return i;
                    inc--;
                    continue;
                }
                if (text.charAt(i) == MainProcessor.PARENTHESIS_CLOSE)
                    inc++;
            }

        }
        return -1;
    }

    private void highlightText(final int pos) {
        try {
            highlighter.removeAllHighlights();

            final String text = textAreaFormula.getText();
            final char[] array = text.toCharArray();
            final int length = array.length;

            if (pos >= 0 && length > 0 && length >= pos) {
                if (pos > 0) {
                    char c = array[pos - 1];
                    if (c == MainProcessor.PARENTHESIS_OPEN || c == MainProcessor.PARENTHESIS_CLOSE) {
                        int i = getParenthesis(pos - 1, c != MainProcessor.PARENTHESIS_CLOSE);
                        if (i >= 0) {
                            highlighter.addHighlight(pos - 1, pos, painterOk);
                            highlighter.addHighlight(i, i + 1, painterOk);
                        } else {
                            highlighter.addHighlight(pos - 1, pos, painterErr);
                        }
                    } else if (Arrays.binarySearch(Functions.CHARS, c) > -1) {
                        int start = pos - 1;
                        int end = start;
                        for (int i = pos - 1; i >= 0 && Arrays.binarySearch(Functions.CHARS, (char) array[i]) > -1; --i) {
                            start = i;
                        }
                        for (int i = pos; i < length && Arrays.binarySearch(Functions.CHARS, (char) array[i]) > -1; ++i) {
                            end = i;
                        }
                        if (start > -1 && end >= start && Functions.check(Arrays.copyOfRange(array, start, end + 1)).isPresent()) {
                            highlighter.addHighlight(start, end + 1, painterFun);
                        }
                    } else if (length > pos) {
                        c = array[pos];
                        if (c == MainProcessor.PARENTHESIS_OPEN || c == MainProcessor.PARENTHESIS_CLOSE) {
                            int i = getParenthesis(pos, c != MainProcessor.PARENTHESIS_CLOSE);
                            if (i >= 0) {
                                highlighter.addHighlight(pos, pos + 1, painterOk);
                                highlighter.addHighlight(i, i + 1, painterOk);
                            } else {
                                highlighter.addHighlight(pos, pos + 1, painterErr);
                            }
                        } else if (Arrays.binarySearch(Functions.CHARS, c) > -1) {
                            int start = pos;
                            int end = start + 1;
                            for (int i = pos; i >= 0 && Arrays.binarySearch(Functions.CHARS, (char) array[i]) > -1; --i) {
                                start = i;
                            }
                            for (int i = pos + 1; i < length && Arrays.binarySearch(Functions.CHARS, (char) array[i]) > -1; ++i) {
                                end = i;
                            }
                            if (start > -1 && end >= start && Functions.check(Arrays.copyOfRange(array, start, end + 1)).isPresent()) {
                                highlighter.addHighlight(start, end + 1, painterFun);
                            }
                        }
                    }
                } else {
                    char c = array[pos];
                    if (c == MainProcessor.PARENTHESIS_OPEN || c == MainProcessor.PARENTHESIS_CLOSE) {
                        int i = getParenthesis(pos, c != MainProcessor.PARENTHESIS_CLOSE);
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
    }
}
