package manoellribeiro.dev.domain.model

data class ActivityPage(
    val items: List<ActivityItem>,
    val nextCursor: String?,
    val hasMore: Boolean,
)
