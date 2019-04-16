package fr.landel.calc.utils;

import java.io.Serializable;

public class Single<T> implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 7839057630731605017L;

    private T single;

    private Single(final T single) {
        this.single = single;
    }

    /**
     * @return the single
     * @category getter
     */
    public T get() {
        return this.single;
    }

    public void set(final T single) {
        this.single = single;
    }

    public static <T> Single<T> of(final T single) {
        return new Single<>(single);
    }

    @Override
    public String toString() {
        return String.valueOf(single);
    }
}