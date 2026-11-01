package manoellribeiro.dev.data.remote.model

import java.time.Instant
import kotlinx.serialization.SerializationException
import manoellribeiro.dev.data.remote.apiJson
import manoellribeiro.dev.domain.model.ActivityPage
import manoellribeiro.dev.domain.model.ActivityType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RemoteActivityPageExtensionsTest {

    @Test
    fun `if the page has items should map them in order`() {
        // Given
        val fakePage = RemoteActivityPage(
            items = listOf(
                createFakeItem(id = "fake-first-id", type = "payout"),
                createFakeItem(id = "fake-second-id", type = "fee"),
            ),
            nextCursor = "fake-next-cursor",
            hasMore = true,
        )

        // When
        val fakeResult = fakePage.toDomain()

        // Then
        assertEquals(listOf("fake-first-id", "fake-second-id"), fakeResult.items.map { it.id })
        assertEquals(listOf(ActivityType.Payout, ActivityType.Fee), fakeResult.items.map { it.type })
        assertEquals("fake-next-cursor", fakeResult.nextCursor)
        assertTrue(fakeResult.hasMore)
    }

    @Test
    fun `if the page is empty should map to a page with no items`() {
        // Given
        val fakePage = RemoteActivityPage(items = emptyList(), nextCursor = null, hasMore = false)

        // When
        val fakeResult = fakePage.toDomain()

        // Then
        assertEquals(ActivityPage(items = emptyList(), nextCursor = null, hasMore = false), fakeResult)
    }

    @Test
    fun `if the json has a null next_cursor should decode it as null`() {
        // Given
        val fakeJson = """{"items": [], "next_cursor": null, "has_more": false}"""

        // When
        val fakeResult = apiJson.decodeFromString<RemoteActivityPage>(fakeJson)

        // Then
        assertNull(fakeResult.nextCursor)
        assertFalse(fakeResult.hasMore)
    }

    @Test
    fun `if the json has no next_cursor should decode it as null`() {
        // Given
        val fakeJson = """{"items": [], "has_more": false}"""

        // When
        val fakeResult = apiJson.decodeFromString<RemoteActivityPage>(fakeJson)

        // Then
        assertNull(fakeResult.nextCursor)
    }

    @Test
    fun `if the json has next_cursor and has_more should decode both`() {
        // Given
        val fakeJson = """{"items": [], "next_cursor": "fake-next-cursor", "has_more": true}"""

        // When
        val fakeResult = apiJson.decodeFromString<RemoteActivityPage>(fakeJson)

        // Then
        assertEquals("fake-next-cursor", fakeResult.nextCursor)
        assertTrue(fakeResult.hasMore)
    }

    @Test
    fun `if the json has no has_more should throw a SerializationException`() {
        // Given
        val fakeJson = """{"items": []}"""

        // When
        val fakeThrown = assertThrows<SerializationException> {
            apiJson.decodeFromString<RemoteActivityPage>(fakeJson)
        }

        // Then
        assertNotNull(fakeThrown)
    }

    private fun createFakeItem(id: String, type: String) = RemoteActivityItem(
        id = id,
        type = type,
        amount = 75_000,
        currency = RemoteCurrency.GBP,
        date = Instant.parse("2026-01-23T10:00:00Z"),
        description = "Fake payment",
        status = "completed",
    )
}
