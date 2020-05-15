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

import java.util.Objects;
import java.util.Set;

/**
 * Encapsulates Gloom data pertaining to a certain class
 */
public class ClassDefinition {

    private final String name;
    private final Set<String> injectInterfaces;
    private final Set<SyntheticField> syntheticFields;
    private final Set<SyntheticMethod> syntheticMethods;

    public ClassDefinition(String name, Set<String> injectInterfaces, Set<SyntheticField> syntheticFields, Set<SyntheticMethod> syntheticMethods) {
        this.name = name;
        this.injectInterfaces = injectInterfaces;
        this.syntheticFields = syntheticFields;
        this.syntheticMethods = syntheticMethods;
    }

    public String getName() {
        return name;
    }

    public Set<String> getInjectInterfaces() {
        return injectInterfaces;
    }

    public Set<SyntheticField> getSyntheticFields() {
        return syntheticFields;
    }

    public Set<SyntheticMethod> getSyntheticMethods() {
        return syntheticMethods;
    }

    public SyntheticField findSyntheticField(String name, String descriptor) {
        for (SyntheticField field : syntheticFields) {
            if (field.getName().equals(name) && field.getType().getDescriptor().equals(descriptor)) {
                return field;
            }
        }

        return null;
    }

    public SyntheticMethod findSyntheticMethod(String name, String descriptor) {
        for (SyntheticMethod method : syntheticMethods) {
            if (method.getName().equals(name) && method.getDescriptor().equals(descriptor)) {
                return method;
            }
        }

        return null;
    }

    public SyntheticField findSyntheticGetter(String name, String descriptor) {
        for (SyntheticField field : syntheticFields) {
            if (field.getName().equals(name)) {
                Accessor accessor = field.getGetter();

                if (accessor != null && accessor.getType().getDescriptor().equals(descriptor)) {
                    return field;
                }
            }
        }

        return null;
    }

    public SyntheticField findSyntheticSetter(String name, String descriptor) {
        for (SyntheticField field : syntheticFields) {
            if (field.getName().equals(name)) {
                Accessor accessor = field.getSetter();

                if (accessor != null && accessor.getType().getDescriptor().equals(descriptor)) {
                    return field;
                }
            }
        }

        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassDefinition that = (ClassDefinition) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(syntheticFields, that.syntheticFields) &&
                Objects.equals(syntheticMethods, that.syntheticMethods);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, syntheticFields, syntheticMethods);
    }

    @Override
    public String toString() {
        return "ClassDefinition{" +
                "name='" + name + '\'' +
                ", syntheticFields=" + syntheticFields +
                ", syntheticMethods=" + syntheticMethods +
                '}';
    }
}
