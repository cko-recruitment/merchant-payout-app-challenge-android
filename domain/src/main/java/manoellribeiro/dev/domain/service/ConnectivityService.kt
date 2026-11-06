package manoellribeiro.dev.domain.service

import kotlinx.coroutines.flow.Flow

interface ConnectivityService {
    /** Emits the current connectivity straight away, then every time it changes. */
    fun observeIsOnline(): Flow<Boolean>
}
