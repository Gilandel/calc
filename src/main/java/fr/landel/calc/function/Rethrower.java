/*-
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

import fr.landel.calc.utils.CastUtils;

/**
 * Interface utility to rethrow an unchecked exception
 *
 * @since Apr 17, 2017
 * @author Gilles
 */
public interface Rethrower {

    /**
     * Rethrow an exception as unchecked
     * 
     * @param throwable
     *            the exception to throw
     * @param <X>
     *            the throwable type
     * @throws X
     *             the provided exception if not {@code null}
     */
    default <X extends Throwable> void rethrowUnchecked(final Throwable throwable) throws X {
        throw CastUtils.<X> cast(Objects.requireNonNull(throwable, "throwable"));
    }
}
