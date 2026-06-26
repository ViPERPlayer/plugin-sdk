package com.viperplayer.plugin.protocol

import kotlinx.serialization.json.Json

/**
 * The payload codec — a deliberately tolerant JSON serializer carried as bytes inside Bundles.
 *
 * It is NOT part of the binder ABI, which is why the wire can stay frozen while payloads evolve:
 *  - [Json.ignoreUnknownKeys]: a newer sender may add fields an older receiver doesn't know — they
 *    are dropped instead of throwing.
 *  - default values on every model field: an older sender omits a field a newer receiver expects —
 *    the receiver fills the default.
 *  - [Json.explicitNulls] = false: nulls are omitted (smaller payloads); absent decodes to the
 *    field default (null for nullable fields).
 *
 * Together these give safe evolution in both directions without versioning individual payloads.
 */
object Codec {
    val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
        isLenient = true
        classDiscriminator = "#t"
    }

    inline fun <reified T> encode(value: T): ByteArray =
        json.encodeToString(value).encodeToByteArray()

    inline fun <reified T> decode(bytes: ByteArray): T =
        json.decodeFromString(bytes.decodeToString())
}
