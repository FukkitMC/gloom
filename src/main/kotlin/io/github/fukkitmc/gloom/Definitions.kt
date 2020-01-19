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

import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.json.Json
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

@Serializer(forClass = Type::class)
object TypeSerializer: KSerializer<Type> {
    override val descriptor: SerialDescriptor = StringDescriptor

    override fun deserialize(decoder: Decoder): Type = Type.getType(decoder.decodeString())

    override fun serialize(encoder: Encoder, obj: Type) = encoder.encodeString(obj.descriptor)
}

@Serializable
data class Member(@Serializable(with = TypeSerializer::class) val owner: Type, val name: String, val descriptor: String)

@Serializable
data class GloomDefinitions(val definitions: Map<String, ClassDefinition>) {

    init {
        definitions.forEach { (name, definition) ->
            assert(definition.type.internalName == name)
        }
    }

    operator fun get(name: String): ClassDefinition? = definitions[name]
}

@Serializable
data class ClassDefinition(
        @Serializable(with = TypeSerializer::class) val type: Type,
        val publicizedFields: Set<Member>,
        val publicizedMethods: Set<Member>,
        val mutableFields: Set<Member>,
        val syntheticFields: Set<SyntheticField>,
        val syntheticMethods: Set<SyntheticMethod>
) {

    init {
        for (field in publicizedFields) {
            assert(field.owner === type)
        }

        for (method in publicizedMethods) {
            assert(method.owner === type)
            assert(method.name != "<clinit>")
        }

        for (field in syntheticFields) {
            val access = field.access
            val getter = field.getter
            val setter = field.setter

            if (getter != null) {
                val a = getter.access
                assert(access and Opcodes.ACC_STATIC == a and Opcodes.ACC_STATIC)
            }

            if (setter != null) {
                val a = setter.access
                assert(access and Opcodes.ACC_STATIC == a and Opcodes.ACC_STATIC)
                assert(access and Opcodes.ACC_FINAL == 0)
            }
        }
    }

    fun getFieldAccess(field: Member, a: Int): Int {
        var access = a

        if (publicizedFields.contains(field)) {
            access = access or Opcodes.ACC_PUBLIC
            access = access and 0b110.inv()
        }

        if (access and Opcodes.ACC_STATIC == 0 && mutableFields.contains(field)) {
            access = access and Opcodes.ACC_FINAL.inv()
        }

        return access
    }

    fun getMethodAccess(method: Member, a: Int): Int {
        var access = a

        if (publicizedMethods.contains(method)) {
            access = access or Opcodes.ACC_PUBLIC
            access = access and 6.inv()
        }

        return access
    }

    fun getSyntheticField(name: String, descriptor: String): SyntheticField? {
        for (field in syntheticFields) {
            if (field.name == name && field.type.descriptor == descriptor) {
                return field
            }
        }

        return null
    }

    fun getSyntheticMethod(name: String, descriptor: String): SyntheticMethod? {
        for (method in syntheticMethods) {
            if (method.name == name && method.descriptor == descriptor) {
                return method
            }
        }

        return null
    }

    fun findSyntheticSetter(name: String, descriptor: String): SyntheticField? {
        for (field in syntheticFields) {
            if (field.name == name) {
                val getter = field.setter
                if (getter != null && getter.type.descriptor == descriptor) {
                    return field
                }
            }
        }

        return null
    }

    fun findSyntheticGetter(name: String, descriptor: String): SyntheticField? {
        for (field in syntheticFields) {
            if (field.name == name) {
                val getter = field.getter
                if (getter != null && getter.type.descriptor == descriptor) {
                    return field
                }
            }
        }

        return null
    }
}


@Serializable
data class SyntheticField(val name: String, @Serializable(with = TypeSerializer::class) val type: Type, val access: Int, val getter: Accessor?, val setter: Accessor?)

@Serializable
data class SyntheticMethod(val opcode: Int, val name: String, val descriptor: String, val access: Int, val redirect: Member)

@Serializable
data class Accessor(val access: Int, @Serializable(with = TypeSerializer::class) val type: Type, val name: String)

fun fromString(string: String): GloomDefinitions {
    return Json.parse(GloomDefinitions.serializer(), string)
}

fun toString(definitions: GloomDefinitions): String {
    return Json.stringify(GloomDefinitions.serializer(), definitions)
}
