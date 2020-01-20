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

class Illuminate(internal val definitions: GloomDefinitions, internal val provider: EmitterProvider<*>) {

    fun createVisitor(classVisitor: ClassVisitor?): ClassVisitor = IlluminateClassVisitor(classVisitor, this)
}

private class IlluminateClassVisitor(visitor: ClassVisitor?, private val illuminate: Illuminate) : ClassVisitor(Opcodes.ASM7, visitor) {

    override fun visitMethod(access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
        return IlluminateAccessVisitor(IlluminateSyntheticVisitor(super.visitMethod(access, name, descriptor, signature, exceptions), illuminate), illuminate)
    }
}

private class IlluminateAccessVisitor(visitor: MethodVisitor, private val illuminate: Illuminate) : MethodVisitor(Opcodes.ASM7, visitor) {

    override fun visitFieldInsn(opcode: Int, owner: String, name: String, descriptor: String) {
        val definition = illuminate.definitions[owner] ?: return super.visitFieldInsn(opcode, owner, name, descriptor)

        val isStatic = opcode == Opcodes.GETSTATIC || opcode == Opcodes.PUTSTATIC
        val isPut = opcode == Opcodes.PUTSTATIC || opcode == Opcodes.PUTFIELD
        val member = Member(definition.type, name, descriptor)
        val publicize = definition.publicizedFields.contains(member)
        val mutate = isPut && !isStatic && definition.publicizedFields.contains(member)

        if (publicize && !mutate) {
            val emitter: Emitter = illuminate.provider[owner]
            val accessor = emitter.accessor
            val desc = if (isPut) "($descriptor)V" else "()$descriptor"

            if (isStatic) {
                if (isPut) {
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, accessor, emitter.generateAccessorSetStatic(name, descriptor), desc, true)
                } else {
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, accessor, emitter.generateAccessorGetStatic(name, descriptor), desc, true)
                }
            } else {
                if (isPut) {
                    super.visitMethodInsn(Opcodes.INVOKEINTERFACE, accessor, emitter.generateAccessorSet(name, descriptor), desc, true)
                } else {
                    super.visitMethodInsn(Opcodes.INVOKEINTERFACE, accessor, emitter.generateAccessorGet(name, descriptor), desc, true)
                }
            }
            return
        }

        if (mutate) {
            val emitter: Emitter = illuminate.provider[owner]
            super.visitMethodInsn(Opcodes.INVOKEINTERFACE, emitter.`interface`, emitter.generateInterfaceMutableSet(name, descriptor), "($descriptor)V", true)
            return
        }

        super.visitFieldInsn(opcode, owner, name, descriptor)
    }

    override fun visitMethodInsn(opcode: Int, owner: String, name: String, descriptor: String, isInterface: Boolean) {
        val definition = illuminate.definitions[owner]

        if (definition != null && definition.publicizedMethods.contains(Member(definition.type, name, descriptor))) {
            val emitter = illuminate.provider[owner]
            val accessor = emitter.accessor
            val isStatic = opcode == Opcodes.INVOKESTATIC

            if (isStatic) {
                super.visitMethodInsn(Opcodes.INVOKESTATIC, accessor, emitter.generateAccessorInvokerStatic(name, descriptor), descriptor, true)
            } else {
                super.visitMethodInsn(Opcodes.INVOKEINTERFACE, accessor, emitter.generateAccessorInvoker(name, descriptor), descriptor, true)
            }

            return
        }

        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
    }
}

private class IlluminateSyntheticVisitor(visitor: MethodVisitor, private val illuminate: Illuminate) : MethodVisitor(Opcodes.ASM7, visitor) {

    override fun visitFieldInsn(opcode: Int, owner: String, name: String, descriptor: String) {
        val field = illuminate.definitions[owner]?.getSyntheticField(name, descriptor)
                ?: return super.visitFieldInsn(opcode, owner, name, descriptor)

        val emitter = illuminate.provider[owner]
        val isStatic = opcode == Opcodes.GETSTATIC || opcode == Opcodes.PUTSTATIC
        val isPut = opcode == Opcodes.PUTSTATIC || opcode == Opcodes.PUTFIELD

        if (isStatic) {
            val holder = emitter.holder

            if (isPut) {
                super.visitMethodInsn(Opcodes.INVOKESTATIC, holder, emitter.generateHolderSyntheticSetAccessor(field), "($descriptor)V", false)
            } else {
                super.visitMethodInsn(Opcodes.INVOKESTATIC, holder, emitter.generateHolderSyntheticGetAccessor(field), "()$descriptor", false)
            }
        } else {
            val itf = emitter.`interface`

            if (isPut) {
                super.visitMethodInsn(Opcodes.INVOKEINTERFACE, itf, emitter.generateInterfaceSyntheticSetAccessor(field), "($descriptor)V", true)
            } else {
                super.visitMethodInsn(Opcodes.INVOKEINTERFACE, itf, emitter.generateInterfaceSyntheticGetAccessor(field), "()$descriptor", true)
            }
        }
    }

    override fun visitMethodInsn(opcode: Int, owner: String, name: String, descriptor: String, isInterface: Boolean) {
        val definition = illuminate.definitions[owner]
                ?: return super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)

        val method = definition.getSyntheticMethod(name, descriptor)

        if (method != null) {
            return super.visitMethodInsn(method.opcode, method.redirect.owner.internalName, method.redirect.name, method.redirect.descriptor, false)
        }

        val ret = Type.getReturnType(descriptor)
        val parameters = Type.getArgumentTypes(descriptor)
        val isStatic = opcode == Opcodes.GETSTATIC || opcode == Opcodes.PUTSTATIC

        if (parameters.size == 1 && ret.sort == Type.VOID) {
            val field = definition.findSyntheticSetter(name, descriptor)

            if (field != null) {
                val emitter = illuminate.provider[owner]

                if (isStatic) {
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, emitter.holder, emitter.generateHolderSyntheticSetAccessor(field), descriptor, false)
                } else {
                    super.visitMethodInsn(Opcodes.INVOKEINTERFACE, emitter.`interface`, emitter.generateInterfaceSyntheticSetAccessor(field), descriptor, true)
                }

                return
            }
        } else if (ret.sort != Type.VOID && parameters.isEmpty()) {
            val field = definition.findSyntheticGetter(name, descriptor)

            if (field != null) {
                val emitter = illuminate.provider[owner]

                if (isStatic) {
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, emitter.holder, emitter.generateHolderSyntheticGetAccessor(field), descriptor, false)
                } else {
                    super.visitMethodInsn(Opcodes.INVOKEINTERFACE, emitter.`interface`, emitter.generateInterfaceSyntheticGetAccessor(field), descriptor, true)
                }

                return
            }
        }

        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
    }
}
