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

import org.objectweb.asm.Type;

import java.util.Objects;

/**
 * Represents a synthetic field which can be added to a class
 */
public class SyntheticField {

    private final int access;
    private final String name;
    private final Type type;
    private final String signature;
    private final Accessor getter;
    private final Accessor setter;

    public SyntheticField(int access, String name, Type type, String signature, Accessor getter, Accessor setter) {
        this.access = access;
        this.name = name;
        this.type = type;
        this.signature = signature;
        this.getter = getter;
        this.setter = setter;
    }

    public int getAccess() {
        return access;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public String getSignature() {
        return signature;
    }

    public Accessor getGetter() {
        return getter;
    }

    public Accessor getSetter() {
        return setter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SyntheticField that = (SyntheticField) o;
        return access == that.access &&
                Objects.equals(name, that.name) &&
                Objects.equals(type, that.type) &&
                Objects.equals(signature, that.signature) &&
                Objects.equals(getter, that.getter) &&
                Objects.equals(setter, that.setter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(access, name, type, signature, getter, setter);
    }

    @Override
    public String toString() {
        return "SyntheticField{" +
                "access=" + access +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", signature='" + signature + '\'' +
                ", getter=" + getter +
                ", setter=" + setter +
                '}';
    }
}
