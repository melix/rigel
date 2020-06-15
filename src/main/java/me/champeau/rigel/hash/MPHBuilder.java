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
package me.champeau.rigel.hash;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A minimal perfect hash function builder, based on the Hash, Displace and Compress
 * algorithm (http://cmph.sourceforge.net/papers/esa09.pdf) except that the current
 * implementation is pretty naive and doesn't do compress. Therefore it requires a larger
 * initial number of buckets (roughly half the number of keys) to find the perfect hash
 * function.
 *
 * @param <T> the type of data to hash
 */
public class MPHBuilder<T> {
    private static final Comparator<Bucket<?>> BUCKET_COMPARATOR = Comparator.comparingInt(Bucket::size);
    private final int firstLevelSize;
    private final Bucket<T>[] buckets;
    private final RandomizedHasher<T> hasher;
    private final int maxFailures = 100_000;
    private int size;

    public MPHBuilder(int firstLevelSize, RandomizedHasher<T> hasher) {
        this.firstLevelSize = firstLevelSize;
        //noinspection unchecked
        this.buckets = (Bucket<T>[]) new Bucket[firstLevelSize];
        this.hasher = hasher;
        for (int i = 0; i < firstLevelSize; i++) {
            buckets[i] = new Bucket<>(i);
        }
    }

    public void add(T key) {
        if (buckets[firstLevelHash(key, firstLevelSize)].add(key)) {
            size++;
        }
    }

    static <T> int firstLevelHash(T key, int firstLevelSize) {
        return (key.hashCode() >>> 1) % firstLevelSize;
    }

    static <T> int secondLevelHash(T key, RandomizedHasher<T> hasher, int size, int seed) {
        return (hasher.hash(key, seed) >>> 1) % size;
    }

    public MinimalPerfectHasher<T> build() {
        Arrays.sort(buckets, BUCKET_COMPARATOR.reversed());
        boolean[] assigned = new boolean[size];
        for (Bucket<T> bucket : buckets) {
            int seed = 0;
            int cpt = 0;
            int failures = 0;
            while (cpt != bucket.size()) {
                boolean[] cur = new boolean[size];
                cpt = 0;
                seed++;
                for (T key : bucket) {
                    int hash = secondLevelHash(key, hasher, size, seed);
                    if (assigned[hash] || cur[hash]) {
                        if (++failures == maxFailures) {
                            throw new IllegalStateException("Can't build minimal perfect hash function. Try increasing the number of initial buckets.");
                        }
                        break;
                    }
                    cur[hash] = true;
                    cpt++;
                }
            }
            for (T key : bucket) {
                int hash = secondLevelHash(key, hasher, size, seed);
                assigned[hash] = true;
            }
            bucket.seed = seed;
        }
        int[] seeds = new int[firstLevelSize];
        for (Bucket<T> bucket : buckets) {
            seeds[bucket.idx] = bucket.seed;
        }
        // reset bucket order
        Arrays.sort(buckets, Comparator.comparingInt(Bucket::getIndex));
        return new MPHHash<>(hasher, firstLevelSize, size, seeds);
    }

    private static class Bucket<T> implements Iterable<T> {
        final Set<T> entries = new HashSet<>();
        final int idx;
        int seed;

        private Bucket(int idx) {
            this.idx = idx;
        }

        public boolean add(T key) {
            return entries.add(key);
        }

        public int getIndex() {
            return idx;
        }

        public int size() {
            return entries.size();
        }

        @Override
        public Iterator<T> iterator() {
            return entries.iterator();
        }

        @Override
        public String toString() {
            return "Bucket size " + size();
        }
    }

    private final static class MPHHash<T> implements MinimalPerfectHasher<T> {
        private final RandomizedHasher<T> hasher;
        private final int primarySize;
        private final int totalSize;
        private final int[] seeds;

        private MPHHash(RandomizedHasher<T> hasher, int primarySize, int totalSize, int[] seeds) {
            this.hasher = hasher;
            this.primarySize = primarySize;
            this.totalSize = totalSize;
            this.seeds = seeds;
        }

        @Override
        public int applyAsInt(T key) {
            int p = firstLevelHash(key, primarySize);
            return secondLevelHash(key, hasher, totalSize, seeds[p]);
        }

        @Override
        public String toString() {
            return "Minimal Perfect Hash function for " + totalSize + " keys";
        }

        @Override
        public int size() {
            return totalSize;
        }
    }

    public interface RandomizedHasher<T> {
        int hash(T element, int seed);
    }
}
