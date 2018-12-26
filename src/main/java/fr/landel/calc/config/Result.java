package fr.landel.calc.config;

import java.io.Serializable;

public class Result implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1564739132976848080L;

    private final boolean success;
    private final String result;

    /**
     * Constructor
     *
     * @param success
     *            the success
     * @param result
     *            the result
     * @category constructor
     */
    public Result(final boolean success, final String result) {
        this.success = success;
        this.result = result;
    }

    /**
     * @return the success
     * @category getter
     */
    public boolean isSuccess() {
        return this.success;
    }

    /**
     * @return the result
     * @category getter
     */
    public String getResult() {
        return this.result;
    }
}
