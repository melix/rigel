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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.SwitchPoint;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class MethodHandleLazy<T> implements Lazy<T> {
    private final static MethodHandle INIT_METHOD;
    private final static MethodHandle GET_METHOD;

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle initialize;
        MethodHandle getValue;
        try {
            MethodType type = MethodType.methodType(Object.class);
            initialize = lookup.findVirtual(MethodHandleLazy.class,
                    "initialize",
                    type);
            getValue = lookup.findVirtual(MethodHandleLazy.class,
                    "getValue",
                    type);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException("Method Handle based lazy isn't supported on this system");
        }
        INIT_METHOD = initialize;
        GET_METHOD = getValue;
    }

    private final SwitchPoint[] switchPoints = new SwitchPoint[]{new SwitchPoint()};
    private final MethodHandle getter;
    private volatile Supplier<T> supplier;
    private T value;

    public MethodHandleLazy(Supplier<T> supplier) {
        this.supplier = supplier;
        getter = switchPoints[0].guardWithTest(INIT_METHOD, GET_METHOD).bindTo(this);
    }

    private T initialize() {
        if (supplier == null) {
            return value;
        }
        synchronized (this) {
            if (supplier == null) {
                return value;
            }
            performInitialization();
        }
        return value;
    }

    private void performInitialization() {
        value = supplier.get();
        supplier = null;
        SwitchPoint.invalidateAll(switchPoints);
    }

    private T getValue() {
        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get() {
        try {
            return (T) getter.invokeExact();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    @Override
    public void use(Consumer<? super T> consumer) {
        consumer.accept(get());
    }

    @Override
    public <V> V apply(Function<? super T, V> function) {
        return function.apply(get());
    }
}
