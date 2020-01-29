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

import io.github.fukkitmc.gloom.definitions.ClassDefinition;
import io.github.fukkitmc.gloom.definitions.SelfMember;
import io.github.fukkitmc.gloom.emitter.Emitter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class IlluminateAccessVisitor extends MethodVisitor {

    private final Illuminate illuminate;

    IlluminateAccessVisitor(MethodVisitor visitor, Illuminate illuminate) {
        super(Opcodes.ASM7, visitor);
        this.illuminate = illuminate;
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        ClassDefinition definition = illuminate.definitions.get(owner);

        if (definition != null) {
            boolean isStatic = opcode == Opcodes.GETSTATIC || opcode == Opcodes.PUTSTATIC;
            boolean isPut = opcode == Opcodes.PUTSTATIC || opcode == Opcodes.PUTFIELD;

            SelfMember member = new SelfMember(name, descriptor);
            boolean publicize = definition.getPublicizedFields().contains(member);
            boolean mutate = isPut && !isStatic && definition.getMutableFields().contains(member);

            if (publicize && !mutate) {
                Emitter emitter = illuminate.provider.forClass(owner);
                String accessor = emitter.getAccessor();
                String desc = isPut ? "(" + descriptor + ")V" : "()" + descriptor;

                if (isStatic) {
                    if (isPut) {
                        super.visitMethodInsn(Opcodes.INVOKESTATIC, accessor, emitter.generateAccessorSetStatic(name, descriptor), desc, true);
                    } else {
                        super.visitMethodInsn(Opcodes.INVOKESTATIC, accessor, emitter.generateAccessorGetStatic(name, descriptor), desc, true);
                    }
                } else {
                    if (isPut) {
                        super.visitMethodInsn(Opcodes.INVOKEINTERFACE, accessor, emitter.generateAccessorSet(name, descriptor), desc, true);
                    } else {
                        super.visitMethodInsn(Opcodes.INVOKEINTERFACE, accessor, emitter.generateAccessorGet(name, descriptor), desc, true);
                    }
                }

                return;
            }

            if (mutate) {
                Emitter emitter = illuminate.provider.forClass(owner);

                super.visitMethodInsn(Opcodes.INVOKEINTERFACE, emitter.getInterface(), emitter.generateInterfaceMutableSet(name, descriptor), "(" + descriptor + ")V", true);
                return;
            }
        }

        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        ClassDefinition definition = illuminate.definitions.get(owner);

        if (definition != null && definition.getPublicizedMethods().contains(new SelfMember(name, descriptor))) {
            Emitter emitter = illuminate.provider.forClass(owner);
            String accessor = emitter.getAccessor();
            boolean isStatic = opcode == Opcodes.INVOKESTATIC;

            if (isStatic) {
                super.visitMethodInsn(Opcodes.INVOKESTATIC, accessor, emitter.generateAccessorInvokerStatic(name, descriptor), descriptor, true);
            } else {
                super.visitMethodInsn(Opcodes.INVOKEINTERFACE, accessor, emitter.generateAccessorInvoker(name, descriptor), descriptor, true);
            }

            return;
        }

        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }
}
