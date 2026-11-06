package manoellribeiro.dev.data.service

import android.net.ConnectivityManager
import android.net.Network
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import manoellribeiro.dev.domain.service.ConnectivityService

internal class ConnectivityServiceImpl(
    private val connectivityManager: ConnectivityManager,
) : ConnectivityService {

    override fun observeIsOnline(): Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            // For the default network this only fires when nothing replaces it, so moving from Wi-Fi to mobile data
            // doesn't look like going offline.
            override fun onLost(network: Network) {
                trySend(false)
            }
        }
        connectivityManager.registerDefaultNetworkCallback(callback)
        // The callback stays silent when there's no network, so the starting value has to be read. It's read after
        // registering, so a network lost in between still reaches the callback.
        trySend(connectivityManager.activeNetwork != null)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()
}
