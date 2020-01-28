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

/**
 * Represents a method reference to any class
 */
public class RedirectTarget {

    private final String owner;
    private final String name;
    private final String descriptor;
    private final boolean isInterface;

    public RedirectTarget(String owner, String name, String descriptor, boolean isInterface) {
        this.owner = owner;
        this.name = name;
        this.descriptor = descriptor;
        this.isInterface = isInterface;
    }

    public String getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public boolean isInterface() {
        return isInterface;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RedirectTarget that = (RedirectTarget) o;
        return isInterface == that.isInterface &&
                Objects.equals(owner, that.owner) &&
                Objects.equals(name, that.name) &&
                Objects.equals(descriptor, that.descriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, name, descriptor, isInterface);
    }

    @Override
    public String toString() {
        return "RedirectTarget{" +
                "owner='" + owner + '\'' +
                ", name='" + name + '\'' +
                ", descriptor='" + descriptor + '\'' +
                ", isInterface=" + isInterface +
                '}';
    }
}
