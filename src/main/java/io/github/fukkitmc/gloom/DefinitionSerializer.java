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

package io.github.fukkitmc.gloom;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.github.fukkitmc.gloom.definitions.ClassDefinition;
import io.github.fukkitmc.gloom.definitions.GloomDefinitions;
import org.objectweb.asm.Type;

import java.util.Set;

/**
 * Requires Gson to be on the classpath
 */
public class DefinitionSerializer {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Type.class, new TypeSerializer())
            .setPrettyPrinting()
            .create();
    private static final java.lang.reflect.Type CLASS_SET = new TypeToken<Set<ClassDefinition>>() {}.getType();

    public static String toString(GloomDefinitions definitions) {
        return GSON.toJson(definitions.getDefinitions());
    }

    public static GloomDefinitions fromString(String json) {
        return new GloomDefinitions(GSON.fromJson(json, CLASS_SET));
    }

    private static class TypeSerializer implements JsonSerializer<Type>, JsonDeserializer<Type> {
        @Override
        public JsonElement serialize(Type src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getDescriptor());
        }

        @Override
        public Type deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return Type.getType(json.getAsString());
        }
    }
}
