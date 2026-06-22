package com.networkmonitor.network

import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for monitoring network connectivity state.
 */
interface NetworkMonitor {
    /**
     * A [StateFlow] that emits the current [NetworkStatus].
     */
    val networkStatus: StateFlow<NetworkStatus>

    /**
     * Suspends until a valid network connection is available.
     * Useful for retrying operations.
     */
    suspend fun awaitConnection()

    /**
     * Synchronously checks if the device is currently connected to the internet.
     */
    fun isConnected(): Boolean
}
