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
package me.champeau.rigel.jmh;

import me.champeau.rigel.Lazy;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class LazyBenchmark {

    int x = 0;

    @Benchmark
    public int unsafeLazy() {
        return Lazy.unsafe(() -> x + 1).apply(v -> 2 * v);
    }

    @Benchmark
    public int synchronizedLazy() {
        return Lazy.locking(() -> x + 1).apply(v -> 2 * v);
    }
}
