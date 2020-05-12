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

package io.github.fukkitmc.gloom.emitter.emitters;

import io.github.fukkitmc.gloom.definitions.SyntheticField;
import io.github.fukkitmc.gloom.emitter.AbstractEmitter;
import org.objectweb.asm.*;

import java.util.HashSet;
import java.util.Set;

public class MixinEmitter extends AbstractEmitter {

    private final String name;
    private final String itf;
    private final String holder;
    private final String mixin;

    public MixinEmitter(String name, String itf, String holder, String mixin) {
        this.name = name;
        this.itf = itf;
        this.holder = holder;
        this.mixin = mixin;
    }

    @Override
    public String getHolder() {
        return holder;
    }

    @Override
    public String getInterface() {
        return itf;
    }

    public String getMixin() {
        return mixin;
    }

    public boolean shouldEmitMixin() {
        return interfaceGets.size() + interfaceSets.size() > 0;
    }

    public boolean shouldEmitInterface() {
        return interfaceGets.size() + interfaceSets.size() > 0;
    }

    public boolean shouldEmitHolder() {
        return holderGets.size() + holderSets.size() > 0;
    }

    public void emitMixin(ClassVisitor visitor) {
        visitor.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, mixin, null, "java/lang/Object", new String[]{itf});

        {
            AnnotationVisitor annotation = visitor.visitAnnotation("Lorg/spongepowered/asm/mixin/Mixin;", false);
            AnnotationVisitor value = annotation.visitArray("value");
            value.visit(null, Type.getObjectType(name));
            value.visitEnd();
            annotation.visit("remap", false);
            annotation.visitEnd();
        }

        {
            MethodVisitor method = visitor.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
            method.visitCode();
            method.visitVarInsn(Opcodes.ALOAD, 0);
            method.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            method.visitInsn(Opcodes.RETURN);
            method.visitMaxs(1, 1);
            method.visitEnd();
        }

        Set<SyntheticField> fields = new HashSet<>();

        interfaceGets.forEach((field, name) -> {
            fields.add(field);

            MethodVisitor method = visitor.visitMethod(Opcodes.ACC_PUBLIC, name, "()" + field.getType().getDescriptor(), null, null);
            method.visitCode();
            method.visitVarInsn(Opcodes.ALOAD, 0);
            method.visitFieldInsn(Opcodes.GETFIELD, mixin, field.getName(), field.getType().getDescriptor());
            method.visitInsn(field.getType().getOpcode(Opcodes.IRETURN));
            method.visitMaxs(1, 1);
            method.visitEnd();
        });

        interfaceSets.forEach((field, name) -> {
            fields.add(field);

            MethodVisitor method = visitor.visitMethod(Opcodes.ACC_PUBLIC, name, "(" + field.getType().getDescriptor() + ")V", null, null);
            method.visitCode();
            method.visitVarInsn(Opcodes.ALOAD, 0);
            method.visitVarInsn(field.getType().getOpcode(Opcodes.ILOAD), 1);
            method.visitFieldInsn(Opcodes.PUTFIELD, mixin, field.getName(), field.getType().getDescriptor());
            method.visitInsn(Opcodes.RETURN);
            method.visitMaxs(2, 2);
            method.visitEnd();
        });

        fields.forEach(f -> {
            FieldVisitor field = visitor.visitField(Opcodes.ACC_PRIVATE, f.getName(), f.getType().getDescriptor(), null, null);
            field.visitEnd();
        });

        visitor.visitEnd();
    }

    public void emitInterface(ClassVisitor visitor) {
        visitor.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT, itf, null, "java/lang/Object", null);

        interfaceGets.forEach((field, name) -> {
            MethodVisitor method = visitor.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, name, "()" + field.getType().getDescriptor(), null, null);
            method.visitEnd();
        });

        interfaceSets.forEach((field, name) -> {
            MethodVisitor method = visitor.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, name, "(" + field.getType().getDescriptor() + ")V", null, null);
            method.visitEnd();
        });

        visitor.visitEnd();
    }

    public void emitHolder(ClassVisitor visitor) {
        visitor.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, holder, null, "java/lang/Object", null);

        {
            MethodVisitor method = visitor.visitMethod(Opcodes.ACC_PRIVATE, "<init>", "()V", null, null);
            method.visitCode();
            method.visitVarInsn(Opcodes.ALOAD, 0);
            method.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            method.visitInsn(Opcodes.RETURN);
            method.visitMaxs(1, 1);
            method.visitEnd();
        }

        Set<SyntheticField> fields = new HashSet<>();

        holderGets.forEach((field, name) -> {
            fields.add(field);

            MethodVisitor method = visitor.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, name, "()" + field.getType().getDescriptor(), null, null);
            method.visitCode();
            method.visitFieldInsn(Opcodes.GETSTATIC, holder, field.getName(), field.getType().getDescriptor());
            method.visitInsn(field.getType().getOpcode(Opcodes.IRETURN));
            method.visitMaxs(1, 0);
            method.visitEnd();
        });

        holderSets.forEach((field, name) -> {
            fields.add(field);

            MethodVisitor method = visitor.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, name, "(" + field.getType().getDescriptor() + ")V", null, null);
            method.visitCode();
            method.visitVarInsn(field.getType().getOpcode(Opcodes.ILOAD), 0);
            method.visitFieldInsn(Opcodes.PUTSTATIC, holder, field.getName(), field.getType().getDescriptor());
            method.visitInsn(Opcodes.RETURN);
            method.visitMaxs(1, 1);
            method.visitEnd();
        });

        fields.forEach(f -> {
            FieldVisitor field = visitor.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, f.getName(), f.getType().getDescriptor(), null, null);
            field.visitEnd();
        });

        visitor.visitEnd();
    }
}
