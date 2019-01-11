package fr.landel.calc.function;

public class FunctionalException extends RuntimeException {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -4010669118228438345L;

    public FunctionalException(final Throwable throwable) {
        super(throwable);
    }
}
