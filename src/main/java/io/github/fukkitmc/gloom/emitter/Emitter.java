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

package io.github.fukkitmc.gloom.emitter;

import io.github.fukkitmc.gloom.definitions.SyntheticField;

/**
 * A listener for usages of Gloom injected data. Invoked via
 * {@link io.github.fukkitmc.gloom.asm.Illuminate}
 */
public interface Emitter {

    /**
     * @return The static holder class's internal name
     */
    String getHolder();

    /**
     * @return The interface's internal name
     */
    String getInterface();

    /**
     * @return The accessor's internal name
     */
    String getAccessor();

    /**
     * @param name       Field name
     * @param descriptor Field descriptor
     * @return Accessor method name
     */
    String generateAccessorSetStatic(String name, String descriptor);

    /**
     * @param name       Field name
     * @param descriptor Field descriptor
     * @return Accessor method name
     */
    String generateAccessorSet(String name, String descriptor);

    /**
     * @param name       Field name
     * @param descriptor Field descriptor
     * @return Accessor method name
     */
    String generateAccessorGetStatic(String name, String descriptor);

    /**
     * @param name       Field name
     * @param descriptor Field descriptor
     * @return Accessor method name
     */
    String generateAccessorGet(String name, String descriptor);

    /**
     * @param name       Method name
     * @param descriptor Method descriptor
     * @return Accessor method name
     */
    String generateAccessorInvokerStatic(String name, String descriptor);

    /**
     * @param name       Method name
     * @param descriptor Method descriptor
     * @return Accessor method name
     */
    String generateAccessorInvoker(String name, String descriptor);

    /**
     * @param field Field
     * @return Holder setter method name
     */
    String generateHolderSyntheticSetAccessor(SyntheticField field);

    /**
     * @param field Field
     * @return Holder getter method name
     */
    String generateHolderSyntheticGetAccessor(SyntheticField field);

    /**
     * @param field Field
     * @return Holder setter method name
     */
    String generateInterfaceSyntheticSetAccessor(SyntheticField field);

    /**
     * @param field Field
     * @return Holder getter method name
     */
    String generateInterfaceSyntheticGetAccessor(SyntheticField field);

    /**
     * @param name       Field name
     * @param descriptor Field descriptor
     * @return Mutable setter name
     */
    String generateInterfaceMutableSet(String name, String descriptor);
}
