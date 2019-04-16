package fr.landel.calc.utils;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.Objects;

public class Pair<T, U> implements Entry<T, U>, Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 8420117321848633016L;

    private static final String SEPARATOR = " = ";

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

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !obj.getClass().isAssignableFrom(Pair.class)) {
            return false;
        } else {
            Pair<?, ?> pair = (Pair<?, ?>) obj;
            return Objects.equals(this.left, pair.left) && Objects.equals(this.right, pair.right);
        }
    }

    @Override
    public int hashCode() {
        return left.hashCode() * right.hashCode();
    }

    @Override
    public String toString() {
        return new StringBuilder().append(left).append(SEPARATOR).append(right).toString();
    }
}