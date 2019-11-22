/*
 * Copyright (C) 2014 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.moshi.kotlin.reflect.list

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinParametrizedTypeImpl
import com.squareup.moshi.kotlin.reflect.toType
import java.io.IOException
import java.lang.IllegalStateException
import java.lang.reflect.Array
import java.lang.reflect.Type
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure

/**
 * Converts arrays to JSON arrays containing their converted contents. This
 * supports both primitive and object arrays.
 */
internal class ArrayKotlinJsonAdapter(
        private val elementType: KTypeProjection,
        private val elementAdapter: JsonAdapter<Any>
) : JsonAdapter<Any>() {

    @Throws(IOException::class)
    override fun fromJson(reader: JsonReader): Any? {
        val isNullable = elementType.type?.isMarkedNullable == true
        val adapter= if (isNullable) {
            elementAdapter.nullSafe()
        } else {
            elementAdapter
        }
        val list = ArrayList<Any?>()
        reader.beginArray()
        while (reader.hasNext()) {
            val parsedElement = elementAdapter.fromJson(reader)
            if (!isNullable && parsedElement == null) {
                throw IllegalStateException("test FUCK YEAH!!")
            }
            list.add(parsedElement)
        }
        reader.endArray()
        return list
    }

    @Throws(IOException::class)
    override fun toJson(writer: JsonWriter, value: Any?) {
        writer.beginArray()
        var i = 0
        val size = Array.getLength(value)
        while (i < size) {
            elementAdapter.toJson(writer, Array.get(value, i))
            i++
        }
        writer.endArray()
    }

    override fun toString(): String {
        return "$elementAdapter.array()"
    }
}

class ArrayKotlinJsonAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
        val parametrizedType = type as? KotlinParametrizedTypeImpl ?: return null
        val kotlinType = parametrizedType.kotlinType
        val isCollection = kotlinType.jvmErasure.isSubclassOf(List::class)
        if (!isCollection) {
            return null
        }
        val elementKotlinType = kotlinType.arguments[0]
        val elementAdapter = moshi.adapter<Any>(elementKotlinType.toType())
        return ArrayKotlinJsonAdapter(elementKotlinType, elementAdapter).nullSafe()
    }

    private fun Type.toClass(): KClass<*>? {
        return if (this is KClass<*>) {
            this
        } else {
            null
        }
    }

    private inline fun <reified T : Any?> classOfIterable(list: Iterable<T>): KClass<*> = T::class

    private fun arrayComponentType(type: KClass<*>): KClass<Iterable<Any?>>? {
        return type.takeIf { it.isSubclassOf(Iterable::class) }?.let { it as KClass<Iterable<Any?>> }
    }

}
