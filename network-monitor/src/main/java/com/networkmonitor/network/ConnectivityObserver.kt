package com.networkmonitor.network

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Utility function to observe network connectivity changes using a [ConnectivityManager.NetworkCallback].
 */
internal fun ConnectivityManager.observeConnectivityAsFlow(): Flow<NetworkStatus> = callbackFlow {
    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            trySend(NetworkStatus.Available)
        }

        override fun onLosing(network: Network, maxMsToLive: Int) {
            super.onLosing(network, maxMsToLive)
            trySend(NetworkStatus.Losing)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            trySend(NetworkStatus.Lost)
        }

        override fun onUnavailable() {
            super.onUnavailable()
            trySend(NetworkStatus.Unavailable)
        }
        
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            val isConnected = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            if (isConnected) {
                trySend(NetworkStatus.Available)
            } else {
                trySend(NetworkStatus.Lost)
            }
        }
    }

    val request = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()

    registerNetworkCallback(request, callback)

    // Initial state check
    val currentNetwork = activeNetwork
    val capabilities = getNetworkCapabilities(currentNetwork)
    val hasInternet = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            
    if (hasInternet) {
        trySend(NetworkStatus.Available)
    } else {
        trySend(NetworkStatus.Unavailable)
    }

    awaitClose {
        unregisterNetworkCallback(callback)
    }
}.distinctUntilChanged()
