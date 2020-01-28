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
 * Represents a synthetic method which can be added to a class
 */
public class SyntheticMethod {

    private final int opcode;
    private final int access;
    private final String name;
    private final String descriptor;
    private final String signature;
    private final RedirectTarget redirect;

    public SyntheticMethod(int opcode, int access, String name, String descriptor, String signature, RedirectTarget redirect) {
        this.opcode = opcode;
        this.access = access;
        this.name = name;
        this.descriptor = descriptor;
        this.signature = signature;
        this.redirect = redirect;
    }

    public int getOpcode() {
        return opcode;
    }

    public int getAccess() {
        return access;
    }

    public String getName() {
        return name;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public String getSignature() {
        return signature;
    }

    public RedirectTarget getRedirect() {
        return redirect;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SyntheticMethod that = (SyntheticMethod) o;
        return opcode == that.opcode &&
                access == that.access &&
                Objects.equals(name, that.name) &&
                Objects.equals(descriptor, that.descriptor) &&
                Objects.equals(signature, that.signature) &&
                Objects.equals(redirect, that.redirect);
    }

    @Override
    public int hashCode() {
        return Objects.hash(opcode, access, name, descriptor, signature, redirect);
    }

    @Override
    public String toString() {
        return "SyntheticMethod{" +
                "opcode=" + opcode +
                ", access=" + access +
                ", name='" + name + '\'' +
                ", descriptor='" + descriptor + '\'' +
                ", signature='" + signature + '\'' +
                ", redirect=" + redirect +
                '}';
    }
}
