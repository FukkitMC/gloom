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

package io.github.fukkitmc.gloom

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import java.util.*

open class MixinEmitter(private val name: String, private val mixin: String, override val holder: String, override val `interface`: String, override val accessor: String) : AbstractEmitter() {

    /**
     * Maps the field name for mutable setter fields
     *
     * @param name       The field name
     * @param descriptor The field descriptor
     * @return The remapped field name
     */
    open fun getField(name: String, descriptor: String): String {
        return name
    }

    /**
     * Maps the field target for accessors
     *
     * @param field The field
     * @return The remapped field name
     */
    open fun getFieldTarget(field: Pair): String {
        return "L" + name + ";" + field.name + ":" + field.desc
    }

    /**
     * Maps the method target for invokers
     *
     * @param method The method
     * @return The remapped method name
     */
    open fun getMethodTarget(method: Pair): String {
        return "L" + name + ";" + method.name + method.desc
    }

    fun shouldEmitMixin(): Boolean {
        return interfaceGets.size + interfaceSets.size + interfaceMutableSets.size > 0
    }

    fun shouldEmitInterface(): Boolean {
        return interfaceGets.size + interfaceSets.size + interfaceMutableSets.size > 0
    }

    fun shouldEmitAccessor(): Boolean {
        return accessorGets.size + accessorSets.size + accessorInvoker.size + accessorStaticGets.size + accessorStaticGets.size + accessorStaticInvoker.size > 0
    }

    fun shouldEmitHolder(): Boolean {
        return holderGets.size + holderSets.size > 0
    }

    fun emitMixin(visitor: ClassVisitor) {
        visitor.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, mixin, null, "java/lang/Object", if (shouldEmitInterface()) arrayOf(`interface`) else null)

        writeMixin(visitor)

        writeConstructor(visitor, Opcodes.ACC_PUBLIC)

        val fields = mutableSetOf<SyntheticField>()

        interfaceGets.forEach { (field, name) ->
            fields.add(field)
            val method = visitor.visitMethod(Opcodes.ACC_PUBLIC, name, "()" + field.type.descriptor, null, null)
            method.visitCode()
            method.visitVarInsn(Opcodes.ALOAD, 0)
            method.visitFieldInsn(Opcodes.GETFIELD, mixin, field.name, field.type.descriptor)
            method.visitInsn(field.type.getOpcode(Opcodes.IRETURN))
            method.visitMaxs(1, 1)
            method.visitEnd()
        }

        interfaceSets.forEach { (field, name) ->
            fields.add(field)
            val method = visitor.visitMethod(Opcodes.ACC_PUBLIC, name, "(" + field.type.descriptor + ")V", null, null)
            method.visitCode()
            method.visitVarInsn(Opcodes.ALOAD, 0)
            method.visitVarInsn(field.type.getOpcode(Opcodes.ILOAD), 1)
            method.visitFieldInsn(Opcodes.PUTFIELD, mixin, field.name, field.type.descriptor)
            method.visitInsn(Opcodes.RETURN)
            method.visitMaxs(2, 2)
            method.visitEnd()
        }

        interfaceMutableSets.forEach { (f, name) ->
            val mapped = getField(f.name, f.desc)

            run {
                val field = visitor.visitField(Opcodes.ACC_PRIVATE, mapped, f.desc, null, null)
                field.visitAnnotation("Lorg/spongepowered/asm/mixin/Shadow;", true).visitEnd()
                field.visitAnnotation("Lorg/spongepowered/asm/mixin/Mutable;", true).visitEnd()
                field.visitAnnotation("Lorg/spongepowered/asm/mixin/Final;", true).visitEnd()
                field.visitEnd()
            }

            run {
                val method = visitor.visitMethod(Opcodes.ACC_PUBLIC, name, "(" + f.desc + ")V", null, null)
                method.visitCode()
                method.visitVarInsn(Opcodes.ALOAD, 0)
                method.visitVarInsn(Type.getType(f.desc).getOpcode(Opcodes.ILOAD), 1)
                method.visitFieldInsn(Opcodes.PUTFIELD, mixin, mapped, f.desc)
                method.visitInsn(Opcodes.RETURN)
                method.visitEnd()
            }
        }

        fields.forEach { f ->
            val field = visitor.visitField(Opcodes.ACC_PRIVATE, f.name, f.type.descriptor, null, null)
            field.visitEnd()
        }

        visitor.visitEnd()
    }

    fun emitInterface(visitor: ClassVisitor) {
        visitor.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC or Opcodes.ACC_INTERFACE or Opcodes.ACC_ABSTRACT, `interface`, null, "java/lang/Object", null)

        interfaceGets.forEach { (field, name) ->
            val method = visitor.visitMethod(Opcodes.ACC_PUBLIC or Opcodes.ACC_ABSTRACT, name, "()" + field.type.descriptor, null, null)
            method.visitEnd()
        }

        interfaceSets.forEach { (field, name) ->
            val method = visitor.visitMethod(Opcodes.ACC_PUBLIC or Opcodes.ACC_ABSTRACT, name, "(" + field.type.descriptor + ")V", null, null)
            method.visitEnd()
        }

        interfaceMutableSets.forEach { (f, name) ->
            val method = visitor.visitMethod(Opcodes.ACC_PUBLIC or Opcodes.ACC_ABSTRACT, name, "(" + f.desc + ")V", null, null)
            method.visitEnd()
        }

        visitor.visitEnd()
    }

    fun emitAccessor(visitor: ClassVisitor) {
        visitor.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC or Opcodes.ACC_INTERFACE or Opcodes.ACC_ABSTRACT, accessor, null, "java/lang/Object", null)

        writeMixin(visitor)

        accessorGets.forEach { (field, name) ->
            val method = visitor.visitMethod(Opcodes.ACC_PUBLIC or Opcodes.ACC_ABSTRACT, name, "()" + field.desc, null, null)
            val annotation = method.visitAnnotation("Lorg/spongepowered/asm/mixin/gen/Accessor;", true)
            annotation.visit("value", getFieldTarget(field))
            annotation.visit("remap", false)
            annotation.visitEnd()
            method.visitEnd()
        }

        accessorSets.forEach { (field, name) ->
            val method = visitor.visitMethod(Opcodes.ACC_PUBLIC or Opcodes.ACC_ABSTRACT, name, "(" + field.desc + ")V", null, null)
            val annotation = method.visitAnnotation("Lorg/spongepowered/asm/mixin/gen/Accessor;", true)
            annotation.visit("value", getFieldTarget(field))
            annotation.visit("remap", false)
            annotation.visitEnd()
            method.visitEnd()
        }
        accessorInvoker.forEach { (m, name) ->
            val method = visitor.visitMethod(Opcodes.ACC_PUBLIC or Opcodes.ACC_ABSTRACT, name, m.desc, null, null)
            val annotation = method.visitAnnotation("Lorg/spongepowered/asm/mixin/gen/Invoker;", true)
            annotation.visit("value", getMethodTarget(m))
            annotation.visit("remap", false)
            annotation.visitEnd()
            method.visitEnd()
        }

        accessorStaticGets.forEach { (field, name) ->
            val method = visitor.visitMethod(Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC, name, "()" + field.desc, null, null)
            val annotation = method.visitAnnotation("Lorg/spongepowered/asm/mixin/gen/Accessor;", true)
            annotation.visit("value", getFieldTarget(field))
            annotation.visit("remap", false)
            annotation.visitEnd()
            method.visitCode()
            unimplemented(method)
            method.visitMaxs(3, 0)
            method.visitEnd()
        }

        accessorStaticSets.forEach { (field, name) ->
            val method = visitor.visitMethod(Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC, name, "(" + field.desc + ")V", null, null)
            val annotation = method.visitAnnotation("Lorg/spongepowered/asm/mixin/gen/Accessor;", true)
            annotation.visit("value", getFieldTarget(field))
            annotation.visit("remap", false)
            annotation.visitEnd()
            method.visitCode()
            unimplemented(method)
            method.visitMaxs(3, 1)
            method.visitEnd()
        }

        accessorStaticInvoker.forEach { (m, name) ->
            val method = visitor.visitMethod(Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC, name, m.desc, null, null)
            val annotation = method.visitAnnotation("Lorg/spongepowered/asm/mixin/gen/Invoker;", true)
            annotation.visit("value", getMethodTarget(m))
            annotation.visit("remap", false)
            annotation.visitEnd()
            method.visitCode()
            unimplemented(method)
            method.visitMaxs(3, Type.getArgumentTypes(m.desc).size)
            method.visitEnd()
        }

        visitor.visitEnd()
    }

    fun emitHolder(visitor: ClassVisitor) {
        visitor.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC or Opcodes.ACC_FINAL, holder, null, "java/lang/Object", null)
        writeConstructor(visitor, Opcodes.ACC_PRIVATE)

        val fields = mutableSetOf<SyntheticField>()
        holderGets.forEach { (field, name) ->
            fields.add(field)
            val method = visitor.visitMethod(Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC, name, "()" + field.type.descriptor, null, null)
            method.visitCode()
            method.visitFieldInsn(Opcodes.GETSTATIC, holder, field.name, field.type.descriptor)
            method.visitInsn(field.type.getOpcode(Opcodes.IRETURN))
            method.visitMaxs(1, 0)
            method.visitEnd()
        }

        holderSets.forEach { (field, name) ->
            fields.add(field)
            val method = visitor.visitMethod(Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC, name, "(" + field.type.descriptor + ")V", null, null)
            method.visitCode()
            method.visitVarInsn(field.type.getOpcode(Opcodes.ILOAD), 0)
            method.visitFieldInsn(Opcodes.PUTSTATIC, holder, field.name, field.type.descriptor)
            method.visitInsn(Opcodes.RETURN)
            method.visitMaxs(1, 1)
            method.visitEnd()
        }

        fields.forEach { (name, type) ->
            val field = visitor.visitField(Opcodes.ACC_PRIVATE or Opcodes.ACC_STATIC, name, type.descriptor, null, null)
            field.visitEnd()
        }

        visitor.visitEnd()
    }

    private fun writeMixin(visitor: ClassVisitor) {
        val annotation = visitor.visitAnnotation("Lorg/spongepowered/asm/mixin/Mixin;", false)
        val value = annotation.visitArray("value")
        value.visit(null, Type.getObjectType(name))
        value.visitEnd()
        annotation.visit("remap", false)
        annotation.visitEnd()
    }

    companion object {
        private fun writeConstructor(visitor: ClassVisitor, access: Int) {
            val method = visitor.visitMethod(access, "<init>", "()V", null, null)
            method.visitCode()
            method.visitVarInsn(Opcodes.ALOAD, 0)
            method.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
            method.visitInsn(Opcodes.RETURN)
            method.visitMaxs(1, 1)
            method.visitEnd()
        }

        private fun unimplemented(method: MethodVisitor) {
            method.visitTypeInsn(Opcodes.NEW, "java/lang/AssertionError")
            method.visitInsn(Opcodes.DUP)
            method.visitLdcInsn("Not implemented")
            method.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/AssertionError", "<init>", "(Ljava/lang/String;)V", false)
            method.visitInsn(Opcodes.ATHROW)
        }
    }
}
