/*
 * #%L
 * utils-commons
 * %%
 * Copyright (C) 2016 - 2018 Gilles Landel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package fr.landel.calc.function;

import java.util.Objects;
import java.util.function.Function;

/**
 * Represents a throwable function that accepts a single argument, produces a
 * result and handles generic exception. This is the unarity/exception
 * specialization of {@link Function}.
 *
 * <p>
 * This is a <a href="package-summary.html">functional interface</a> whose
 * functional method is {@link #apply(Object)}.
 * </p>
 *
 * @since May 14, 2016
 * @author Gilles
 *
 * @param <T>
 *            The input type
 * @param <R>
 *            The return type
 * @param <E>
 *            The exception type
 */
@FunctionalInterface
public interface FunctionThrowable<T, R, E extends Throwable> extends Function<T, R>, Rethrower {

    /**
     * Performs this operation on the given argument.
     *
     * @param t
     *            the input argument
     * @return The output result
     */
    @Override
    default R apply(final T t) {
        try {
            return applyThrows(t);
        } catch (final Throwable e) {
            rethrowUnchecked(e);
            throw new FunctionalException(e); // never reached normally
        }
    }

    /**
     * Performs this operation on the given argument.
     *
     * @param t
     *            the input argument
     * @return The output result
     * @throws E
     *             On error exception
     */
    R applyThrows(T t) throws E;

    /**
     * Returns a composed function that first applies the {@code before}
     * function to its input, and then applies this function to the result. If
     * evaluation of either function throws an exception, it is relayed to the
     * caller of the composed function.
     *
     * @param <V>
     *            the type of input to the {@code before} function, and to the
     *            composed function
     * @param before
     *            the function to apply before this function is applied
     * @return a composed function that first applies the {@code before}
     *         function and then applies this function
     * @throws NullPointerException
     *             if before is null
     * @throws E
     *             On error exception
     *
     * @see #andThen(Function)
     */
    default <V> FunctionThrowable<V, R, E> composeThrows(final FunctionThrowable<V, T, E> before) throws E {
        Objects.requireNonNull(before, "before");
        return (v) -> applyThrows(before.applyThrows(v));
    }

    /**
     * Returns a composed function that first applies this function to its
     * input, and then applies the {@code after} function to the result. If
     * evaluation of either function throws an exception, it is relayed to the
     * caller of the composed function.
     *
     * @param <O>
     *            the type of output of the {@code after} function, and of the
     *            composed function
     * @param after
     *            the function to apply after this function is applied
     * @return a composed function that first applies this function and then
     *         applies the {@code after} function
     * @throws NullPointerException
     *             if after is null
     * @throws E
     *             On error exception
     *
     * @see #composeThrows(FunctionThrowable)
     */
    default <O> FunctionThrowable<T, O, E> andThen(final FunctionThrowable<R, O, E> after) throws E {
        Objects.requireNonNull(after, "after");
        return (t) -> after.applyThrows(applyThrows(t));
    }
}
