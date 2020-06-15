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
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Benchmark)
public class LazyReadBenchmark {

    private int x = 0;
    private final Lazy<Integer> unsafe = Lazy.unsafe().of(this::x);
    private final Lazy<Integer> locking = Lazy.locking().of(this::x);
    private final Lazy<Integer> sync = Lazy.synchronizing().of(this::x);

    int x() {
        return ++x;
    }

    static int apply(Lazy<Integer> lazy) {
        return lazy.apply(v -> 2 * v);
    }

    @Benchmark
    public void unsafeLazy(Blackhole blackhole) {
        blackhole.consume(apply(unsafe));
    }


    @Benchmark
    public void synchronizedLazy(Blackhole blackhole) {
        blackhole.consume(apply(sync));
    }

    @Benchmark
    public void lockingLazy(Blackhole blackhole) {
        blackhole.consume(apply(locking));
    }
    
}
