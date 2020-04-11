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
import io.github.fukkitmc.gloom.definitions.RedirectTarget;
import io.github.fukkitmc.gloom.definitions.SyntheticField;
import io.github.fukkitmc.gloom.definitions.SyntheticMethod;
import io.github.fukkitmc.gloom.emitter.Emitter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

class IlluminateSyntheticVisitor extends MethodVisitor {

    private final Illuminate illuminate;

    IlluminateSyntheticVisitor(MethodVisitor visitor, Illuminate illuminate) {
        super(Opcodes.ASM8, visitor);
        this.illuminate = illuminate;
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        ClassDefinition definition = illuminate.definitions.get(owner);

        if (definition == null) {
            super.visitFieldInsn(opcode, owner, name, descriptor);
            return;
        }

        SyntheticField field = definition.findSyntheticField(name, descriptor);

        if (field == null) {
            super.visitFieldInsn(opcode, owner, name, descriptor);
            return;
        }

        Emitter emitter = illuminate.provider.forClass(owner);
        boolean isStatic = opcode == Opcodes.GETSTATIC || opcode == Opcodes.PUTSTATIC;
        boolean isPut = opcode == Opcodes.PUTSTATIC || opcode == Opcodes.PUTFIELD;

        if (isStatic) {
            String holder = emitter.getHolder();

            if (isPut) {
                super.visitMethodInsn(Opcodes.INVOKESTATIC, holder, emitter.generateHolderSyntheticSetAccessor(field), Type.getMethodDescriptor(Type.VOID_TYPE, field.getType()), false);
            } else {
                super.visitMethodInsn(Opcodes.INVOKESTATIC, holder, emitter.generateHolderSyntheticGetAccessor(field), Type.getMethodDescriptor(field.getType()), false);
            }
        } else {
            String itf = emitter.getInterface();

            if (isPut) {
                super.visitMethodInsn(Opcodes.INVOKEINTERFACE, itf, emitter.generateHolderSyntheticSetAccessor(field), Type.getMethodDescriptor(Type.VOID_TYPE, field.getType()), true);
            } else {
                super.visitMethodInsn(Opcodes.INVOKEINTERFACE, itf, emitter.generateHolderSyntheticGetAccessor(field), Type.getMethodDescriptor(field.getType()), true);
            }
        }
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        ClassDefinition definition = illuminate.definitions.get(owner);

        if (definition == null) {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            return;
        }

        {
            SyntheticMethod method = definition.findSyntheticMethod(name, descriptor);

            if (method != null) {
                RedirectTarget target = method.getRedirect();
                super.visitMethodInsn(method.getOpcode(), target.getOwner(), target.getName(), target.getDescriptor(), target.isInterface());
                return;
            }
        }

        Type ret = Type.getReturnType(descriptor);
        Type[] parameters = Type.getArgumentTypes(descriptor);
        boolean isStatic = opcode == Opcodes.GETSTATIC || opcode == Opcodes.PUTSTATIC;

        if (parameters.length == 1 && ret.getSort() == Type.VOID) {
            SyntheticField field = definition.findSyntheticSetter(name, descriptor);

            if (field != null) {
                Emitter emitter = illuminate.provider.forClass(owner);

                if (isStatic) {
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, emitter.getHolder(), emitter.generateHolderSyntheticSetAccessor(field), descriptor, false);
                } else {
                    super.visitMethodInsn(Opcodes.INVOKEINTERFACE, emitter.getInterface(), emitter.generateInterfaceSyntheticSetAccessor(field), descriptor, true);
                }

                return;
            }
        } else if (parameters.length == 0 && ret.getSort() != Type.VOID) {
            SyntheticField field = definition.findSyntheticGetter(name, descriptor);

            if (field != null) {
                Emitter emitter = illuminate.provider.forClass(owner);

                if (isStatic) {
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, emitter.getHolder(), emitter.generateHolderSyntheticGetAccessor(field), descriptor, false);
                } else {
                    super.visitMethodInsn(Opcodes.INVOKEINTERFACE, emitter.getInterface(), emitter.generateInterfaceSyntheticGetAccessor(field), descriptor, true);
                }

                return;
            }
        }

        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }
}
