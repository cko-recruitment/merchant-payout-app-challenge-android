package manoellribeiro.dev.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
internal data class RemoteApiError(
    val code: String? = null,
)
