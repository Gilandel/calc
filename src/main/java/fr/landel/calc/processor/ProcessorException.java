package fr.landel.calc.processor;

import fr.landel.calc.config.I18n;

public class ProcessorException extends Exception {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -4010669118128438345L;

    public ProcessorException(final Throwable throwable, final I18n i18n, final Object... params) {
        super(i18n.getI18n(params), throwable);
    }

    public ProcessorException(final I18n i18n, final Object... params) {
        super(i18n.getI18n(params));
    }
}
