package com.networkmonitor.network

import java.io.IOException

/**
 * Exception thrown by [NetworkAwareInterceptor] when the device is offline.
 */
class OfflineException(message: String = "No internet connection available") : IOException(message)
