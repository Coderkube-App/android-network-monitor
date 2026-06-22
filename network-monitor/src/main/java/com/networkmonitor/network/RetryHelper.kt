package com.networkmonitor.network

import kotlinx.coroutines.delay
import java.io.IOException

/**
 * Executes the given [block]. If an [IOException] (like [OfflineException]) occurs,
 * it waits for the network to be connected using [NetworkMonitor.awaitConnection]
 * and then retries the operation.
 *
 * @param networkMonitor The [NetworkMonitor] to wait for connection on.
 * @param maxRetries The maximum number of retry attempts. Defaults to 3.
 * @param delayMs The delay in milliseconds between retries. Defaults to 1000ms.
 * @param block The suspend function to execute and potentially retry.
 * @return The result of the [block].
 * @throws Exception if the maximum number of retries is exceeded or a non-IOException occurs.
 */
suspend fun <T> retryWhenConnected(
    networkMonitor: NetworkMonitor,
    maxRetries: Int = 3,
    delayMs: Long = 1000L,
    block: suspend () -> T
): T {
    var currentAttempt = 0
    while (true) {
        try {
            return block()
        } catch (e: IOException) {
            currentAttempt++
            if (currentAttempt > maxRetries) {
                throw e
            }
            // Wait for network connection to be restored before retrying
            networkMonitor.awaitConnection()
            delay(delayMs)
        }
    }
}
