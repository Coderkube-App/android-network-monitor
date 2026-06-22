package com.networkmonitor.network.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.networkmonitor.network.NetworkMonitor
import com.networkmonitor.network.NetworkStatus

/**
 * Interface that the Application class should implement to provide the [NetworkMonitor].
 */
interface NetworkMonitorProvider {
    val networkMonitor: NetworkMonitor
}

/**
 * A Composable function that observes and returns the current [NetworkStatus].
 * It retrieves the [NetworkMonitor] from the Application context.
 */
@Composable
fun rememberNetworkStatus(): NetworkStatus {
    val context = LocalContext.current
    val networkMonitor = remember(context) {
        val applicationContext = context.applicationContext
        if (applicationContext is NetworkMonitorProvider) {
            applicationContext.networkMonitor
        } else {
            error("Application must implement NetworkMonitorProvider to use rememberNetworkStatus()")
        }
    }
    
    val status by networkMonitor.networkStatus.collectAsStateWithLifecycle()
    return status
}
