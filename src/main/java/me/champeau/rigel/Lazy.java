/*
 * Copyright 2003-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.champeau.rigel;

import me.champeau.rigel.internal.LockingLazy;
import me.champeau.rigel.internal.UnsafeLazy;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A wrapper around a value computed lazily. Multiple implementations
 * are possible and creating a lazy provider can be done by calling
 * one of the factory methods:
 * <ul>
 *     <li>{@link #unsafe(Supplier)} would create a lazy wrapper which performs no synchronization at all when calling the supplier: it may be called several times concurrently by different threads. Not thread safe!</li>
 *     <li>{@link #unsafe(Supplier)} would create a lazy wrapper which performs locking when calling the supplier: the supplier will only be called once. Reading is done without locking once initialized.</li>
 * </ul>
 *
 * @param <T> the type of the lazy value
 */
public interface Lazy<T> {

    /**
     * @return the value this lazy wraps
     */
    T get();

    /**
     * Executes an operation on the lazily computed value
     * @param consumer the consumer
     */
    void use(Consumer<? super T> consumer);

    /**
     * Applies a function to the lazily computed value and returns its value
     * @param function the value to apply to the lazily computed value
     * @param <V> the return type
     * @return the result of the function, applied on the lazily computed value
     */
    <V> V apply(Function<? super T, V> function);

    /**
     * Creates another lazy wrapper which will eventually apply the supplied
     * function to the lazily computed value
     * @param mapper the mapping function
     * @param <V> the type of the result of the function
     * @return a new lazy wrapper
     */
    <V> Lazy<V> map(Function<? super T, V> mapper);

    /**
     * Creates an unsafe lazy value provider, which is not thread-safe.
     */
    static <T> Lazy<T> unsafe(Supplier<T> supplier) {
        return new UnsafeLazy<>(supplier);
    }

    /**
     * Creates a thread-safe lazy value provider which performs synchronization
     * and only executes the supplier once.
     */
    static <T> Lazy<T> locking(Supplier<T> supplier) {
        return new LockingLazy<>(supplier);
    }
}