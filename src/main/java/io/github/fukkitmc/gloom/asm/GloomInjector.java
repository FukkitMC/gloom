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

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Injects {@link GloomDefinitions definitions} into classes
 */
public class GloomInjector extends ClassVisitor {

    private final GloomDefinitions definitions;
    private ClassDefinition definition;

    public GloomInjector(ClassVisitor delegate, GloomDefinitions definitions) {
        super(Opcodes.ASM8, delegate);
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
                    interfaces = Stream.concat(Arrays.stream(interfaces), Arrays.stream(i)).toArray(String[]::new);
                }

                if (signature != null) {
                    for (String itf : i) {
                        signature += "L" + itf + ";";
                    }
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
                    boolean isStatic = (access & Opcodes.ACC_STATIC) != 0;

                    // TODO: These can be null
                    MethodVisitor visitor = super.visitMethod(access, getter.getName(), Type.getMethodDescriptor(getter.getType()), getter.getSignature(), null);
                    visitor.visitCode();

                    if (isStatic) {
                        visitor.visitFieldInsn(Opcodes.GETSTATIC, definition.getName(), field.getName(), field.getType().getDescriptor());
                    } else {
                        visitor.visitVarInsn(Opcodes.ALOAD, 0);
                        visitor.visitFieldInsn(Opcodes.GETFIELD, definition.getName(), field.getName(), field.getType().getDescriptor());
                    }

                    visitor.visitInsn(getter.getType().getOpcode(Opcodes.IRETURN));
                    visitor.visitMaxs(1, isStatic ? 0 : 1);
                    visitor.visitEnd();
                }

                if (setter != null) {
                    int access = setter.getAccess();
                    boolean isStatic = (access & Opcodes.ACC_STATIC) != 0;

                    MethodVisitor visitor = super.visitMethod(access, setter.getName(), Type.getMethodDescriptor(Type.VOID_TYPE, setter.getType()), setter.getSignature(), null);
                    visitor.visitCode();

                    if (isStatic) {
                        visitor.visitVarInsn(setter.getType().getOpcode(Opcodes.ILOAD), 0);
                    } else {
                        visitor.visitVarInsn(Opcodes.ALOAD, 0);
                        visitor.visitVarInsn(setter.getType().getOpcode(Opcodes.ILOAD), 1);
                    }

                    visitor.visitFieldInsn((field.getAccess() & Opcodes.ACC_STATIC) != 0 ? Opcodes.PUTSTATIC : Opcodes.PUTFIELD, definition.getName(), field.getName(), field.getType().getDescriptor());
                    visitor.visitInsn(Opcodes.RETURN);

                    if (isStatic) {
                        visitor.visitMaxs(1, 1);
                    } else {
                        visitor.visitMaxs(2, 2);
                    }

                    visitor.visitEnd();
                }
            }

            for (SyntheticMethod method : definition.getSyntheticMethods()) {
                Type returnType = Type.getReturnType(method.getDescriptor());
                RedirectTarget redirect = method.getRedirect();

                MethodVisitor visitor = super.visitMethod(method.getAccess(), method.getName(), method.getDescriptor(), method.getSignature(), null);
                visitor.visitCode();

                int counter = 0;

                if ((method.getAccess() & Opcodes.ACC_STATIC) == 0) {
                    visitor.visitVarInsn(Opcodes.ALOAD, counter++);
                }

                for (Type argument : Type.getArgumentTypes(method.getDescriptor())) {
                    visitor.visitVarInsn(argument.getOpcode(Opcodes.ILOAD), counter);
                    counter += argument.getSize();
                }

                counter += returnType.getSize();

                visitor.visitMethodInsn(method.getOpcode(), redirect.getOwner(), redirect.getName(), redirect.getDescriptor(), redirect.isInterface());
                visitor.visitInsn(returnType.getOpcode(Opcodes.IRETURN));
                visitor.visitMaxs(counter, counter);
                visitor.visitEnd();
            }
        }

        super.visitEnd();
    }
}
