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

import io.github.fukkitmc.gloom.definitions.*;
import org.objectweb.asm.*;

import java.util.Set;

/**
 * Injects {@link GloomDefinitions definitions} into classes
 */
public class GloomInjector extends ClassVisitor {

    private final GloomDefinitions definitions;
    private ClassDefinition definition;

    public GloomInjector(ClassVisitor delegate, GloomDefinitions definitions) {
        super(Opcodes.ASM7, delegate);
        this.definitions = definitions;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        definition = definitions.get(name);

        if (definition != null) {
            Set<String> inject = definition.getInjectInterfaces();

            if (!inject.isEmpty()) {
                String[] i = inject.toArray(new String[0]);

                if (interfaces == null) {
                    interfaces = i;
                } else {
                    String[] copy = interfaces;
                    interfaces = new String[interfaces.length + i.length];
                    System.arraycopy(copy, 0, interfaces, 0, interfaces.length);
                    System.arraycopy(i, 0, interfaces, interfaces.length, i.length);
                }
            }
        }

        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        if (definition != null) {
            access = definition.getFieldAccess(access, name, descriptor);
        }

        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (definition != null) {
            access = definition.getMethodAccess(access, name, descriptor);
        }

        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        if (definition != null) {
            for (SyntheticField field : definition.getSyntheticFields()) {
                super.visitField(field.getAccess(), field.getName(), field.getType().getDescriptor(), field.getSignature(), null).visitEnd();

                Accessor getter = field.getGetter();
                Accessor setter = field.getSetter();

                if (getter != null) {
                    int access = getter.getAccess();
                    // TODO: These can be null
                    MethodVisitor visitor = super.visitMethod(access, getter.getName(), Type.getMethodDescriptor(getter.getType()), getter.getSignature(), null);
                    visitor.visitCode();

                    if ((access & Opcodes.ACC_STATIC) != 0) {
                        visitor.visitFieldInsn(Opcodes.GETSTATIC, definition.getName(), field.getName(), field.getType().getDescriptor());
                    } else {
                        visitor.visitVarInsn(Opcodes.ALOAD, 0);
                        visitor.visitFieldInsn(Opcodes.GETFIELD, definition.getName(), field.getName(), field.getType().getDescriptor());
                    }

                    visitor.visitInsn(getter.getType().getOpcode(Opcodes.IRETURN));
                    visitor.visitEnd();
                }

                if (setter != null) {
                    int access = setter.getAccess();
                    MethodVisitor visitor = super.visitMethod(access, setter.getName(), Type.getMethodDescriptor(Type.VOID_TYPE, setter.getType()), setter.getSignature(), null);
                    visitor.visitCode();

                    if ((access & Opcodes.ACC_STATIC) != 0) {
                        visitor.visitVarInsn(setter.getType().getOpcode(Opcodes.ILOAD), 0);
                    } else {
                        visitor.visitVarInsn(Opcodes.ALOAD, 0);
                        visitor.visitVarInsn(setter.getType().getOpcode(Opcodes.ILOAD), 1);
                    }

                    visitor.visitFieldInsn((field.getAccess() & Opcodes.ACC_STATIC) != 0 ? Opcodes.PUTSTATIC : Opcodes.PUTFIELD, definition.getName(), field.getName(), field.getType().getDescriptor());
                    visitor.visitInsn(Opcodes.RETURN);
                    visitor.visitEnd();
                }
            }

            for (SyntheticMethod method : definition.getSyntheticMethods()) {
                RedirectTarget redirect = method.getRedirect();

                MethodVisitor visitor = super.visitMethod(method.getAccess(), method.getName(), method.getDescriptor(), method.getSignature(), null);
                visitor.visitCode();

                int counter = 0;

                if ((method.getAccess() & Opcodes.ACC_STATIC) == 0) {
                    visitor.visitVarInsn(Opcodes.ALOAD, counter++);
                }

                for (Type argument : Type.getArgumentTypes(method.getDescriptor())) {
                    visitor.visitVarInsn(argument.getOpcode(Opcodes.ILOAD), counter++);
                }

                visitor.visitMethodInsn(method.getOpcode(), redirect.getOwner(), redirect.getName(), redirect.getDescriptor(), redirect.isInterface());
                visitor.visitInsn(Type.getReturnType(method.getDescriptor()).getOpcode(Opcodes.IRETURN));
                visitor.visitEnd();
            }
        }

        super.visitEnd();
    }
}
