package com.networkmonitor.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultNetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) : NetworkMonitor {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val coroutineScope = CoroutineScope(SupervisorJob() + defaultDispatcher)

    override val networkStatus: StateFlow<NetworkStatus> =
        connectivityManager.observeConnectivityAsFlow()
            .stateIn(
                scope = coroutineScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = getCurrentNetworkStatus()
            )

    override suspend fun awaitConnection() {
        if (isConnected()) return
        networkStatus.first { it == NetworkStatus.Available }
    }

    override fun isConnected(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun getCurrentNetworkStatus(): NetworkStatus {
        return if (isConnected()) {
            NetworkStatus.Available
        } else {
            NetworkStatus.Unavailable
        }
    }
}
