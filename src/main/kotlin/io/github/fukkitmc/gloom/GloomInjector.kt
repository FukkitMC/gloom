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

import org.objectweb.asm.*

class GloomInjector(visitor: ClassVisitor?, private val definitions: GloomDefinitions): ClassVisitor(Opcodes.ASM7, visitor) {

    private lateinit var owner: String
    private lateinit var type: Type
    private var definition: ClassDefinition? = null

    override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String?, interfaces: Array<out String>?) {
        owner = name
        type = Type.getObjectType(name)
        definition = definitions[name]

        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitField(access: Int, name: String, descriptor: String, signature: String?, value: Any?): FieldVisitor? {
        return super.visitField(definition?.getFieldAccess(Member(type, name, descriptor), access) ?: access, name, descriptor, signature, value)
    }

    override fun visitMethod(access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
        return super.visitMethod(definition?.getMethodAccess(Member(type, name, descriptor), access) ?: access, name, descriptor, signature, exceptions)
    }

    override fun visitEnd() {
        if (definition == null) {
            super.visitEnd()
            return
        }

        finish()
        super.visitEnd()
    }

    private fun finish() {
        val definition = definition ?: return

        for ((name, type, access, getter, setter) in definition.syntheticFields) {
            super.visitField(access, name, type.descriptor, null, null).visitEnd()

            getter?.let { (a, t, n) ->
                val visitor = super.visitMethod(a, n, Type.getMethodDescriptor(t), null, null)

                if (visitor != null) {
                    visitor.visitCode()

                    if (a and Opcodes.ACC_STATIC != 0) {
                        visitor.visitFieldInsn(Opcodes.GETSTATIC, owner, name, t.descriptor)
                    } else {
                        visitor.visitVarInsn(Opcodes.ALOAD, 0)
                        visitor.visitFieldInsn(if (access and Opcodes.ACC_STATIC != 0) Opcodes.GETSTATIC else Opcodes.GETFIELD, owner, name, t.descriptor)
                    }

                    visitor.visitInsn(t.getOpcode(Opcodes.IRETURN))
                    visitor.visitEnd()
                }
            }

            setter?.let { (a, t, n) ->
                val visitor = super.visitMethod(a, n, Type.getMethodDescriptor(Type.VOID_TYPE, t), null, null)

                if (visitor != null) {
                    visitor.visitCode()

                    if (a and Opcodes.ACC_STATIC != 0) {
                        visitor.visitVarInsn(t.getOpcode(Opcodes.ILOAD), 0)
                    } else {
                        visitor.visitVarInsn(Opcodes.ALOAD, 0)
                        visitor.visitVarInsn(t.getOpcode(Opcodes.ILOAD), 1)
                    }

                    visitor.visitFieldInsn(if (access and Opcodes.ACC_STATIC != 0) Opcodes.PUTSTATIC else Opcodes.PUTFIELD, name, name, t.descriptor)
                    visitor.visitInsn(Opcodes.RETURN)
                    visitor.visitEnd()
                }
            }
        }

        for ((opcode, name, descriptor, access, redirect) in definition.syntheticMethods) {
            val visitor = super.visitMethod(access, name, descriptor, null, null)

            if (visitor != null) {
                visitor.visitCode()

                var counter = 0

                if (access and Opcodes.ACC_STATIC == 0) {
                    visitor.visitVarInsn(Opcodes.ALOAD, 0)
                    counter = 1
                }

                for (t in Type.getArgumentTypes(descriptor)) {
                    visitor.visitVarInsn(t.getOpcode(Opcodes.ILOAD), counter++)
                }

                visitor.visitMethodInsn(opcode, redirect.owner.internalName, redirect.name, redirect.descriptor, false)
                visitor.visitInsn(Type.getReturnType(descriptor).getOpcode(Opcodes.IRETURN))
                visitor.visitEnd()
            }
        }
    }
}
