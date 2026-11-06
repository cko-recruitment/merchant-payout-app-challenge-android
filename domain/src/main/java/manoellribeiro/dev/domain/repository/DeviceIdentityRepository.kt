package manoellribeiro.dev.domain.repository

interface DeviceIdentityRepository {
    /** Returns the ID generated on first use, and the same ID on every later call and launch. */
    suspend fun getDeviceId(): Result<String>
}
