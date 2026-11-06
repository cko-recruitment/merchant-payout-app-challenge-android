package manoellribeiro.dev.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RemoteDevice(
    @SerialName("device_id") val deviceId: String,
)
