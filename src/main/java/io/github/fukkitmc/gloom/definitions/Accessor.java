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

public class Accessor {

    private final int access;
    private final Type type;
    private final String name;
    private final String signature;

    public Accessor(int access, Type type, String name, String signature) {
        this.access = access;
        this.type = type;
        this.name = name;
        this.signature = signature;
    }

    public int getAccess() {
        return access;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getSignature() {
        return signature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Accessor accessor = (Accessor) o;
        return access == accessor.access &&
                Objects.equals(type, accessor.type) &&
                Objects.equals(name, accessor.name) &&
                Objects.equals(signature, accessor.signature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(access, type, name, signature);
    }

    @Override
    public String toString() {
        return "Accessor{" +
                "access=" + access +
                ", type=" + type +
                ", name='" + name + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}
