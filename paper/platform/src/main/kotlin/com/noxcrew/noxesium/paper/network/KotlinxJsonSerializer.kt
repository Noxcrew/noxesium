package com.noxcrew.noxesium.paper.network

import com.noxcrew.noxesium.api.network.json.JsonSerializer
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

/** A JSON serializer backed by kotlinx.serialization. */
@OptIn(InternalSerializationApi::class)
public class KotlinxJsonSerializer(
    /** The JSON serializer to use. */
    public val json: Json,
) : JsonSerializer {
    override fun <T : Any> encode(value: T, clazz: Class<T>): String = json.encodeToString(clazz.kotlin.serializer(), value)

    override fun <T : Any> decode(string: String, clazz: Class<T>): T = json.decodeFromString(clazz.kotlin.serializer(), string)
}