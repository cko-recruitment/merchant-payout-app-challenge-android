package manoellribeiro.dev.data.remote.model

import java.time.Instant
import kotlinx.serialization.SerializationException
import manoellribeiro.dev.data.remote.apiJson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class IsoInstantSerializerTest {

    @Test
    fun `if the date is in UTC should decode it`() {
        // Given
        val fakeJson = "\"2026-01-23T10:00:00Z\""

        // When
        val fakeInstant = apiJson.decodeFromString(IsoInstantSerializer, fakeJson)

        // Then
        assertEquals(Instant.parse("2026-01-23T10:00:00Z"), fakeInstant)
    }

    @Test
    fun `if the date has fractional seconds should keep them`() {
        // Given
        val fakeJson = "\"2026-01-23T10:00:00.123Z\""

        // When
        val fakeInstant = apiJson.decodeFromString(IsoInstantSerializer, fakeJson)

        // Then
        assertEquals(Instant.parse("2026-01-23T10:00:00.123Z"), fakeInstant)
    }

    @Test
    fun `if the date has a positive offset should convert it to the same instant`() {
        // Given
        val fakeJson = "\"2026-01-23T11:00:00+01:00\""

        // When
        val fakeInstant = apiJson.decodeFromString(IsoInstantSerializer, fakeJson)

        // Then
        assertEquals(Instant.parse("2026-01-23T10:00:00Z"), fakeInstant)
    }

    @Test
    fun `if the date has a negative offset should convert it to the same instant`() {
        // Given
        val fakeJson = "\"2026-01-23T07:00:00-03:00\""

        // When
        val fakeInstant = apiJson.decodeFromString(IsoInstantSerializer, fakeJson)

        // Then
        assertEquals(Instant.parse("2026-01-23T10:00:00Z"), fakeInstant)
    }

    @Test
    fun `if the date has no offset should throw a SerializationException`() {
        // Given
        val fakeJson = "\"2026-01-23T10:00:00\""

        // When
        val fakeThrown = assertThrows<SerializationException> {
            apiJson.decodeFromString(IsoInstantSerializer, fakeJson)
        }

        // Then
        assertInstanceOf(java.time.DateTimeException::class.java, fakeThrown.cause)
    }

    @Test
    fun `if the value has only a date should throw a SerializationException`() {
        // Given
        val fakeJson = "\"2026-01-23\""

        // When
        val fakeThrown = assertThrows<SerializationException> {
            apiJson.decodeFromString(IsoInstantSerializer, fakeJson)
        }

        // Then
        assertEquals("Invalid ISO 8601 date", fakeThrown.message)
    }

    @Test
    fun `if the value is empty should throw a SerializationException`() {
        // Given
        val fakeJson = "\"\""

        // When
        val fakeThrown = assertThrows<SerializationException> {
            apiJson.decodeFromString(IsoInstantSerializer, fakeJson)
        }

        // Then
        assertEquals("Invalid ISO 8601 date", fakeThrown.message)
    }

    @Test
    fun `if the value is not a json string should throw a SerializationException`() {
        // Given
        val fakeJson = "1769162400"

        // When
        val fakeThrown = assertThrows<SerializationException> {
            apiJson.decodeFromString(IsoInstantSerializer, fakeJson)
        }

        // Then
        assertInstanceOf(SerializationException::class.java, fakeThrown)
    }

    @Test
    fun `if an instant is encoded should write it as an ISO 8601 string`() {
        // Given
        val fakeInstant = Instant.parse("2026-01-23T10:00:00Z")

        // When
        val fakeJson = apiJson.encodeToString(IsoInstantSerializer, fakeInstant)

        // Then
        assertEquals("\"2026-01-23T10:00:00Z\"", fakeJson)
    }

    @Test
    fun `if an instant is encoded and decoded should return the same instant`() {
        // Given
        val fakeInstant = Instant.parse("2026-03-15T08:45:30.500Z")

        // When
        val fakeDecoded = apiJson.decodeFromString(
            IsoInstantSerializer,
            apiJson.encodeToString(IsoInstantSerializer, fakeInstant),
        )

        // Then
        assertEquals(fakeInstant, fakeDecoded)
    }
}
