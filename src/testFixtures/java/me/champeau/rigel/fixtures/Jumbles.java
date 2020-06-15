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

import me.champeau.rigel.hash.MPHBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

public class Jumbles {
    private final MPHBuilder.MinimalPerfectHasher<String> hashFunction;
    private final String[] index;

    private Jumbles(MPHBuilder.MinimalPerfectHasher<String> hashFunction, String[] index) {
        this.hashFunction = hashFunction;
        this.index = index;
    }

    public static Jumbles of() throws IOException {
        return of("/usr/share/dict/words");
    }

    public static Jumbles of(String dictionary) throws IOException {
        File dic = new File(dictionary);
        MPHBuilder<String> builder = new MPHBuilder<>(50000, Jumbles::hash);
        forEachWord(dic, word -> builder.add(sort(word)));
        MPHBuilder.MinimalPerfectHasher<String> hashFunction = builder.build();
        // build the index
        String[] index = new String[hashFunction.size()];
        forEachWord(dic, word -> index[hashFunction.applyAsInt(sort(word))] = word);
        return new Jumbles(hashFunction, index);
    }

    public Optional<String> guess(String word) {
        String query = sort(word);
        int hash = hashFunction.applyAsInt(query);
        String answer = this.index[hash];
        // Because we're using a minimal perfect hash, we will
        // always return a hash within the 0..index size range
        // so we need to validate that it's a valid answer
        if (query.equals(sort(answer))) {
            return Optional.of(answer);
        }
        return Optional.empty();
    }

    private static int hash(String str, int seed) {
        Random rnd = new Random(seed);
        int i = 0;
        for (char c : str.toCharArray()) {
            i = 37 * i + c + rnd.nextInt();
        }
        return i;
    }

    public static String sort(String str) {
        char[] chars = str.toCharArray();
        Arrays.sort(chars);
        return new String(chars);
    }

    public static void forEachWord(File path, Consumer<String> consumer) throws IOException {
        Files.readAllLines(path.toPath())
                .stream()
                .map(String::toLowerCase)
                .filter(word -> word.length() == 5 || word.length() == 6)
                .forEach(consumer);
    }
}
