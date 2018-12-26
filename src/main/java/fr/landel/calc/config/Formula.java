package fr.landel.calc.config;

import java.io.Serializable;
import java.util.Optional;

public class Formula implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -8662711988520996344L;

    private final String formula;
    private Optional<Result> result;

    /**
     * Constructor
     *
     * @param formula
     *            the formula
     */
    public Formula(final String formula) {
        this.formula = formula;
    }

    /**
     * Constructor
     *
     * @param formula
     *            the formula
     * @param success
     *            the success status
     * @param result
     *            the result
     */
    public Formula(final String formula, final boolean success, final String result) {
        this(formula);
        this.result = Optional.of(new Result(success, result));
    }

    /**
     * @return the formula
     * @category getter
     */
    public String getFormula() {
        return this.formula;
    }

    /**
     * @param result
     *            the result of the formula
     * @category setter
     */
    public void setResult(final Result result) {
        this.result = Optional.ofNullable(result);
    }

    /**
     * @return the result
     * @category getter
     */
    public Optional<Result> getResult() {
        return this.result;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(this.formula);
        this.result.ifPresent(
                result -> builder.append("__STATUS__").append(result.isSuccess()).append("__RESULT__").append(result.getResult()));
        return builder.toString();
    }
}
