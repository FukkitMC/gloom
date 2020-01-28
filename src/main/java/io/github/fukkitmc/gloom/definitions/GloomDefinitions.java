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

package io.github.fukkitmc.gloom.definitions;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A holder for multiple {@link ClassDefinition class definitions}
 */
public class GloomDefinitions {

    private final Map<String, ClassDefinition> definitions;

    public GloomDefinitions(Set<ClassDefinition> definitions) {
        this.definitions = definitions.stream().collect(Collectors.toMap(ClassDefinition::getName, Function.identity()));
    }

    public Collection<ClassDefinition> getDefinitions() {
        return definitions.values();
    }

    public ClassDefinition get(String name) {
        return definitions.get(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GloomDefinitions that = (GloomDefinitions) o;
        return Objects.equals(definitions, that.definitions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(definitions);
    }

    @Override
    public String toString() {
        return "GloomDefinitions{" +
                definitions.values() +
                '}';
    }
}
