package com.networkmonitor.network

/**
 * Represents the current state of network connectivity.
 */
sealed interface NetworkStatus {
    /** Network is available and validated for internet. */
    data object Available : NetworkStatus
    
    /** Network is completely unavailable. */
    data object Unavailable : NetworkStatus
    
    /** Network is losing connection. */
    data object Losing : NetworkStatus
    
    /** Network connection was lost. */
    data object Lost : NetworkStatus
}
