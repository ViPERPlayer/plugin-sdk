package com.viperplayer.plugin.protocol

import com.viperplayer.plugin.model.HomeSection
import com.viperplayer.plugin.model.MediaItem
import com.viperplayer.plugin.model.PlayableItem
import com.viperplayer.plugin.model.StreamSource
import com.viperplayer.plugin.model.UnknownMediaItem
import com.viperplayer.plugin.model.UnknownPlayableItem
import com.viperplayer.plugin.model.UnknownSection
import com.viperplayer.plugin.model.UnknownStream
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

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
 *  - [Json.coerceInputValues]: an unknown enum constant from a newer peer decodes to the field's
 *    default instead of throwing.
 *  - polymorphic default deserializers: an unknown sealed subtype (a newer peer's new MediaItem /
 *    StreamSource kind) decodes to an Unknown* fallback instead of failing the whole payload.
 *
 * Together these give safe evolution in both directions without versioning individual payloads.
 */
object Codec {
    val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
        isLenient = true
        coerceInputValues = true
        classDiscriminator = "#t"
        serializersModule = SerializersModule {
            polymorphicDefaultDeserializer(MediaItem::class) { UnknownMediaItem.serializer() }
            polymorphicDefaultDeserializer(PlayableItem::class) { UnknownPlayableItem.serializer() }
            polymorphicDefaultDeserializer(StreamSource::class) { UnknownStream.serializer() }
            polymorphicDefaultDeserializer(HomeSection::class) { UnknownSection.serializer() }
        }
    }

    inline fun <reified T> encode(value: T): ByteArray =
        json.encodeToString(value).encodeToByteArray()

    inline fun <reified T> decode(bytes: ByteArray): T =
        json.decodeFromString(bytes.decodeToString())
}
