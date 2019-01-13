package fr.landel.calc.utils;

import java.util.Map.Entry;

public class Pair<T, U> implements Entry<T, U> {

    private final T left;
    private final U right;

    private Pair(final T left, final U right) {
        this.left = left;
        this.right = right;
    }

    /**
     * @return the left
     * @category getter
     */
    public T getLeft() {
        return this.left;
    }

    /**
     * @return the right
     * @category getter
     */
    public U getRight() {
        return this.right;
    }

    @Override
    public T getKey() {
        return this.getLeft();
    }

    @Override
    public U getValue() {
        return this.getRight();
    }

    @Override
    public U setValue(final U value) {
        throw new UnsupportedOperationException();
    }

    public static <T, U> Pair<T, U> of(final T left, final U right) {
        return new Pair<>(left, right);
    }
}