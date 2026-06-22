package com.networkmonitor.app

import android.app.Application
import com.networkmonitor.network.DefaultNetworkMonitor
import com.networkmonitor.network.NetworkMonitor
import com.networkmonitor.network.compose.NetworkMonitorProvider

class MainApplication : Application(), NetworkMonitorProvider {
    
    override val networkMonitor: NetworkMonitor by lazy {
        DefaultNetworkMonitor(this)
    }
}
