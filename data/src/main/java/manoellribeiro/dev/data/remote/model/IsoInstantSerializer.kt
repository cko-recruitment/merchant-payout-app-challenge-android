package manoellribeiro.dev.data.remote.model

import java.time.DateTimeException
import java.time.Instant
import java.time.OffsetDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/** Uses OffsetDateTime because Instant.parse rejects ISO 8601 offsets such as +01:00 on older APIs. */
internal object IsoInstantSerializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("IsoInstant", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant = try {
        OffsetDateTime.parse(decoder.decodeString()).toInstant()
    } catch (e: DateTimeException) {
        throw SerializationException("Invalid ISO 8601 date", e)
    }

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }
}
