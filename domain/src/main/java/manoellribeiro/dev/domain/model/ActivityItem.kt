package manoellribeiro.dev.domain.model

import java.time.Instant

data class ActivityItem(
    val id: String,
    val type: ActivityType,
    val amount: Money,
    val date: Instant,
    val description: String,
    val status: ActivityStatus,
)
