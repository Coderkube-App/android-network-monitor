package com.networkmonitor.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * An OkHttp [Interceptor] that checks for network connectivity before proceeding
 * with the request. Throws an [OfflineException] if the device is not connected.
 */
@Singleton
class NetworkAwareInterceptor @Inject constructor(
    private val networkMonitor: NetworkMonitor
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!networkMonitor.isConnected()) {
            throw OfflineException()
        }
        return chain.proceed(chain.request())
    }
}
