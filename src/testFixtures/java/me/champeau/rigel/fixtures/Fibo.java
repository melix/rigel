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
package me.champeau.rigel.fixtures;

import me.champeau.rigel.Lazy;

/**
 * An example of how to use the lazy initializer.
 */
public class Fibo {
    private final Lazy<Long> value;

    public Fibo(long n) {
        this.value = Lazy.unsafe().of(() -> fibo(n));
    }

    public String directUse() {
        return value.apply(v -> "Result is " + v);
    }

    public String mappedUse() {
        return value.map(v -> "Mapped is " + v).get();
    }

    // Intentionally slow ;)
    private static long fibo(long n) {
        if (n<2) {
            return n;
        }
        return fibo(n-1) + fibo(n-2);
    }
}
