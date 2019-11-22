package com.squareup.moshi.kotlin.reflect

import com.squareup.moshi.internal.Util
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.KType

internal class KotlinParametrizedTypeImpl(
        val kotlinType: KType,
        private val parametrizedJavaType: ParameterizedType
) : ParameterizedType, Util.CustomCanonizeType {

    override fun getRawType(): Type = parametrizedJavaType.rawType

    override fun getOwnerType(): Type? = parametrizedJavaType.ownerType

    override fun getActualTypeArguments(): Array<Type?> = parametrizedJavaType.actualTypeArguments
}