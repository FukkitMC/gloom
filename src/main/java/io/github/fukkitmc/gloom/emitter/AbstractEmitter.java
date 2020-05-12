/*
 * Copyright 2020 ramidzkh
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

package io.github.fukkitmc.gloom.emitter;

import io.github.fukkitmc.gloom.definitions.SyntheticField;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

public abstract class AbstractEmitter implements Emitter {

    private static final char[] RANDOM_CHARACTERS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
            'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
            'y', 'z'};

    protected final Map<SyntheticField, String> holderGets = new HashMap<>();
    protected final Map<SyntheticField, String> holderSets = new HashMap<>();
    protected final Map<SyntheticField, String> interfaceGets = new HashMap<>();
    protected final Map<SyntheticField, String> interfaceSets = new HashMap<>();

    private final Random random = new Random("The loom is gloomier".hashCode());

    private final Function<SyntheticField, String> computeHG = random(holderGets, "getStatic");
    private final Function<SyntheticField, String> computeHS = random(holderSets, "setStatic");
    private final Function<SyntheticField, String> computeIG = random(interfaceGets, "getSynthetic");
    private final Function<SyntheticField, String> computeIS = random(interfaceSets, "setSynthetic");

    @Override
    public String generateHolderSyntheticSetAccessor(SyntheticField field) {
        return holderSets.computeIfAbsent(field, computeHS);
    }

    @Override
    public String generateHolderSyntheticGetAccessor(SyntheticField field) {
        return holderGets.computeIfAbsent(field, computeHG);
    }

    @Override
    public String generateInterfaceSyntheticSetAccessor(SyntheticField field) {
        return interfaceSets.computeIfAbsent(field, computeIS);
    }

    @Override
    public String generateInterfaceSyntheticGetAccessor(SyntheticField field) {
        return interfaceGets.computeIfAbsent(field, computeIG);
    }

    private <T> Function<T, String> random(Map<T, String> map, String prefix) {
        return t -> {
            while (true) {
                String s = prefix + getRandomString();

                if (!map.containsValue(s)) {
                    return s;
                }
            }
        };
    }

    private String getRandomString() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            builder.append(RANDOM_CHARACTERS[random.nextInt(RANDOM_CHARACTERS.length)]);
        }

        return builder.toString();
    }

}
