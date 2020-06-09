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
package me.champeau.rigel.internal;

import me.champeau.rigel.Lazy;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class LockingLazy<T> implements Lazy<T> {
    private final Lock lock = new ReentrantLock();
    private Supplier<T> supplier;
    private T value;

    public LockingLazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T get() {
        return ensureValue();
    }

    private T ensureValue() {
        if (supplier != null) {
            lock.lock();
            try {
                maybeInitialize();
            } finally {
                lock.unlock();
            }
        }
        return value;
    }

    private void maybeInitialize() {
        if (supplier != null) {
            value = supplier.get();
            supplier = null;
        }
    }

    @Override
    public void use(Consumer<? super T> consumer) {
        consumer.accept(ensureValue());
    }

    @Override
    public <V> V apply(Function<? super T, V> function) {
        return function.apply(ensureValue());
    }

    @Override
    public <V> Lazy<V> map(Function<? super T, V> mapper) {
        return Lazy.unsafe(() -> mapper.apply(get()));
    }
}