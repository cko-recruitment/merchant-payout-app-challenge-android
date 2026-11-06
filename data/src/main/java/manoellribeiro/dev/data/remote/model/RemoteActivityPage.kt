package manoellribeiro.dev.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import manoellribeiro.dev.domain.model.ActivityPage

@Serializable
internal data class RemoteActivityPage(
    val items: List<RemoteActivityItem>,
    @SerialName("next_cursor") val nextCursor: String? = null,
    @SerialName("has_more") val hasMore: Boolean,
)

internal fun RemoteActivityPage.toDomain() = ActivityPage(
    items = items.map { it.toDomain() },
    nextCursor = nextCursor,
    hasMore = hasMore,
)
