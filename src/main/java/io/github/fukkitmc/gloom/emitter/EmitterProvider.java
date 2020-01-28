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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Holds a map of {@link Emitter emitters}
 *
 * @param <E> The emitter type
 */
public class EmitterProvider<E extends Emitter> {

    private final Map<String, E> emitters = new HashMap<>();
    private final Function<String, E> provider;

    public EmitterProvider(Function<String, E> provider) {
        this.provider = provider;
    }

    public Emitter forClass(String name) {
        return emitters.computeIfAbsent(name, provider);
    }

    public Map<String, E> getEmitters() {
        return emitters;
    }
}
