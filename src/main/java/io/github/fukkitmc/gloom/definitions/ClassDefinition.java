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

import org.objectweb.asm.Opcodes;

import java.util.Objects;
import java.util.Set;

/**
 * Encapsulates Gloom data pertaining to a certain class
 */
public class ClassDefinition {

    private final String name;
    private final Set<String> injectInterfaces;
    private final Set<SelfMember> publicizedFields;
    private final Set<SelfMember> publicizedMethods;
    private final Set<SelfMember> mutableFields;
    private final Set<SyntheticField> syntheticFields;
    private final Set<SyntheticMethod> syntheticMethods;

    public ClassDefinition(String name, Set<String> injectInterfaces, Set<SelfMember> publicizedFields, Set<SelfMember> publicizedMethods, Set<SelfMember> mutableFields, Set<SyntheticField> syntheticFields, Set<SyntheticMethod> syntheticMethods) {
        this.name = name;
        this.injectInterfaces = injectInterfaces;
        this.publicizedFields = publicizedFields;
        this.publicizedMethods = publicizedMethods;
        this.mutableFields = mutableFields;
        this.syntheticFields = syntheticFields;
        this.syntheticMethods = syntheticMethods;
    }

    public String getName() {
        return name;
    }

    public Set<String> getInjectInterfaces() {
        return injectInterfaces;
    }

    public Set<SelfMember> getPublicizedFields() {
        return publicizedFields;
    }

    public Set<SelfMember> getPublicizedMethods() {
        return publicizedMethods;
    }

    public Set<SelfMember> getMutableFields() {
        return mutableFields;
    }

    public Set<SyntheticField> getSyntheticFields() {
        return syntheticFields;
    }

    public Set<SyntheticMethod> getSyntheticMethods() {
        return syntheticMethods;
    }

    public int getFieldAccess(int access, String name, String descriptor) {
        SelfMember member = new SelfMember(name, descriptor);

        if (publicizedFields.contains(member)) {
            access |= Opcodes.ACC_PUBLIC;
            access &= ~Opcodes.ACC_PRIVATE;
            access &= ~Opcodes.ACC_PROTECTED;
        }

        if (mutableFields.contains(member)) {
            access &= ~Opcodes.ACC_FINAL;
        }

        return access;
    }

    public int getMethodAccess(int access, String name, String descriptor) {
        SelfMember member = new SelfMember(name, descriptor);

        if (publicizedMethods.contains(member)) {
            access |= Opcodes.ACC_PUBLIC;
            access &= ~Opcodes.ACC_PRIVATE;
            access &= ~Opcodes.ACC_PROTECTED;
        }

        return access;
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
                Objects.equals(injectInterfaces, that.injectInterfaces) &&
                Objects.equals(publicizedFields, that.publicizedFields) &&
                Objects.equals(publicizedMethods, that.publicizedMethods) &&
                Objects.equals(mutableFields, that.mutableFields) &&
                Objects.equals(syntheticFields, that.syntheticFields) &&
                Objects.equals(syntheticMethods, that.syntheticMethods);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, injectInterfaces, publicizedFields, publicizedMethods, mutableFields, syntheticFields, syntheticMethods);
    }

    @Override
    public String toString() {
        return "ClassDefinition{" +
                "name='" + name + '\'' +
                ", injectInterfaces=" + injectInterfaces +
                ", publicizedFields=" + publicizedFields +
                ", publicizedMethods=" + publicizedMethods +
                ", mutableFields=" + mutableFields +
                ", syntheticFields=" + syntheticFields +
                ", syntheticMethods=" + syntheticMethods +
                '}';
    }
}
