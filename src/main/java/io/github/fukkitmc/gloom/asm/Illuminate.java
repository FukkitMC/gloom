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

package io.github.fukkitmc.gloom.asm;

import io.github.fukkitmc.gloom.definitions.GloomDefinitions;
import io.github.fukkitmc.gloom.emitter.EmitterProvider;
import org.objectweb.asm.ClassVisitor;

/**
 * Analyses classes for references to Gloom injected data
 * and registers them to the provided {@link EmitterProvider}
 */
public class Illuminate {

    private static final InheritanceProvider DEFAULT = new InheritanceProvider() {
        @Override
        public String resolveFieldOwner(String owner, String name, String descriptor) {
            return owner;
        }

        @Override
        public String resolveMethodOwner(String owner, String name, String descriptor) {
            return owner;
        }
    };

    final GloomDefinitions definitions;
    final EmitterProvider<?> provider;
    final InheritanceProvider inheritance;

    public Illuminate(GloomDefinitions definitions, EmitterProvider<?> provider, InheritanceProvider inheritance) {
        this.definitions = definitions;
        this.provider = provider;
        this.inheritance = inheritance == null ? DEFAULT : inheritance;
    }

    public ClassVisitor createVisitor(ClassVisitor visitor) {
        return new IlluminateClassVisitor(visitor, this);
    }
}
