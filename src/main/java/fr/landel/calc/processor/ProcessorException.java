package fr.landel.calc.processor;

import fr.landel.calc.config.I18n;
import fr.landel.calc.utils.StringUtils;

public class ProcessorException extends Exception {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -4010669118128438345L;

    public ProcessorException(final Throwable throwable, final String message, final Object... params) {
        super(StringUtils.inject(message, params), throwable);
    }

    public ProcessorException(final String message, final Object... params) {
        super(StringUtils.inject(message, params));
    }

    public ProcessorException(final Throwable throwable, final I18n i18n, final Object... params) {
        super(StringUtils.inject(i18n.getI18n(), params), throwable);
    }

    public ProcessorException(final I18n i18n, final Object... params) {
        super(StringUtils.inject(i18n.getI18n(), params));
    }
}
