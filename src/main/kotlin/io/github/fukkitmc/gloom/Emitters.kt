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

import java.util.*
import java.util.function.Function

/**
 * Holds references to all [emitters][Emitter] which have generated code
 *
 * @param <E> The emitter type
 * @see Emitter
 */
class EmitterProvider<E : Emitter>(private val provider: Function<String, E>) {
    val emitters = mutableMapOf<String, E>()

    operator fun get(clazz: String): Emitter {
        return emitters.computeIfAbsent(clazz, provider)
    }
}

/**
 * Invoked by [illuminate][Illuminate] for underlying code generation
 *
 * @see AbstractEmitter
 *
 * @see MixinEmitter
 */
interface Emitter {
    /**
     * @return The static holder class's internal name
     */
    val holder: String

    /**
     * @return The interface's internal name
     */
    val `interface`: String

    /**
     * @return The accessor's internal name
     */
    val accessor: String

    /**
     * @param name       Field name
     * @param descriptor Field descriptor
     * @return Accessor method name
     */
    fun generateAccessorSetStatic(name: String, descriptor: String): String

    /**
     * @param name       Field name
     * @param descriptor Field descriptor
     * @return Accessor method name
     */
    fun generateAccessorSet(name: String, descriptor: String): String

    /**
     * @param name       Field name
     * @param descriptor Field descriptor
     * @return Accessor method name
     */
    fun generateAccessorGetStatic(name: String, descriptor: String): String

    /**
     * @param name       Field name
     * @param descriptor Field descriptor
     * @return Accessor method name
     */
    fun generateAccessorGet(name: String, descriptor: String): String

    /**
     * @param name       Method name
     * @param descriptor Method descriptor
     * @return Accessor method name
     */
    fun generateAccessorInvokerStatic(name: String, descriptor: String): String

    /**
     * @param name       Method name
     * @param descriptor Method descriptor
     * @return Accessor method name
     */
    fun generateAccessorInvoker(name: String, descriptor: String): String

    /**
     * @param field Field
     * @return Holder setter method name
     */
    fun generateHolderSyntheticSetAccessor(field: SyntheticField): String

    /**
     * @param field Field
     * @return Holder getter method name
     */
    fun generateHolderSyntheticGetAccessor(field: SyntheticField): String

    /**
     * @param field Field
     * @return Holder setter method name
     */
    fun generateInterfaceSyntheticSetAccessor(field: SyntheticField): String

    /**
     * @param field Field
     * @return Holder getter method name
     */
    fun generateInterfaceSyntheticGetAccessor(field: SyntheticField): String

    /**
     * @param name       Field name
     * @param descriptor Field descriptor
     * @return Mutable setter name
     */
    fun generateInterfaceMutableSet(name: String, descriptor: String): String
}

data class Pair(val name: String, val desc: String)

/**
 * Provides a base for emitters to be implemented. Keeps track of
 * already generated members so an implementation may generate code
 */
abstract class AbstractEmitter: Emitter {

    protected val accessorSets = mutableMapOf<Pair, String>()
    protected val accessorGets = mutableMapOf<Pair, String>()
    protected val accessorInvoker = mutableMapOf<Pair, String>()
    protected val accessorStaticSets = mutableMapOf<Pair, String>()
    protected val accessorStaticGets = mutableMapOf<Pair, String>()
    protected val accessorStaticInvoker = mutableMapOf<Pair, String>()
    protected val holderGets = mutableMapOf<SyntheticField, String>()
    protected val holderSets = mutableMapOf<SyntheticField, String>()
    protected val interfaceGets = mutableMapOf<SyntheticField, String>()
    protected val interfaceSets = mutableMapOf<SyntheticField, String>()
    protected val interfaceMutableSets = mutableMapOf<Pair, String>()

    private val random = Random("The loom is gloomier".hashCode().toLong())
    private val computeAS = random(accessorSets, "setInstance")
    private val computeAG = random(accessorGets, "getInstance")
    private val computeAI = random(accessorInvoker, "invokeInstance")
    private val computeASS = random(accessorStaticSets, "setStatic")
    private val computeASG = random(accessorStaticGets, "getStatic")
    private val computeASI = random(accessorStaticInvoker, "invokeStatic")
    private val computeHG = random(holderGets, "getStatic")
    private val computeHS = random(holderSets, "setStatic")
    private val computeIG = random(interfaceGets, "getSynthetic")
    private val computeIS = random(interfaceSets, "setSynthetic")
    private val computeMS = random(interfaceMutableSets, "setMutable")

    override fun generateAccessorSetStatic(name: String, descriptor: String): String {
        return accessorStaticSets.computeIfAbsent(Pair(name, descriptor), computeASS)
    }

    override fun generateAccessorSet(name: String, descriptor: String): String {
        return accessorSets.computeIfAbsent(Pair(name, descriptor), computeAS)
    }

    override fun generateAccessorGetStatic(name: String, descriptor: String): String {
        return accessorStaticGets.computeIfAbsent(Pair(name, descriptor), computeASG)
    }

    override fun generateAccessorGet(name: String, descriptor: String): String {
        return accessorGets.computeIfAbsent(Pair(name, descriptor), computeAG)
    }

    override fun generateAccessorInvokerStatic(name: String, descriptor: String): String {
        return accessorStaticInvoker.computeIfAbsent(Pair(name, descriptor), computeASI)
    }

    override fun generateAccessorInvoker(name: String, descriptor: String): String {
        return accessorInvoker.computeIfAbsent(Pair(name, descriptor), computeAI)
    }

    override fun generateHolderSyntheticSetAccessor(field: SyntheticField): String {
        return holderSets.computeIfAbsent(field, computeHS)
    }

    override fun generateHolderSyntheticGetAccessor(field: SyntheticField): String {
        return holderGets.computeIfAbsent(field, computeHG)
    }

    override fun generateInterfaceSyntheticSetAccessor(field: SyntheticField): String {
        return interfaceSets.computeIfAbsent(field, computeIS)
    }

    override fun generateInterfaceSyntheticGetAccessor(field: SyntheticField): String {
        return interfaceGets.computeIfAbsent(field, computeIG)
    }

    override fun generateInterfaceMutableSet(name: String, descriptor: String): String {
        return interfaceMutableSets.computeIfAbsent(Pair(name, descriptor), computeMS)
    }

    private fun <T> random(map: Map<T, String>, prefix: String): Function<T, String> {
        return Function {
            while (true) {
                val s = prefix + getRandomString()

                if (!map.containsValue(s)) {
                    return@Function s
                }
            }

            error("How did we get here?")
        }
    }

    private fun getRandomString(): String {
        val builder = StringBuilder()

        for (i in 0..5) {
            builder.append(RANDOM_CHARACTERS[random.nextInt(RANDOM_CHARACTERS.size)])
        }

        return builder.toString()
    }

    companion object {
        private val RANDOM_CHARACTERS = charArrayOf(
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
                'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
                'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
                'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
                'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
                'y', 'z')
    }
}
