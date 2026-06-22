package com.networkmonitor.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class RetryHelperTest {

    class FakeNetworkMonitor(var connected: Boolean = false) : NetworkMonitor {
        override val networkStatus: StateFlow<NetworkStatus> =
            MutableStateFlow(if (connected) NetworkStatus.Available else NetworkStatus.Unavailable)

        override suspend fun awaitConnection() {
            if (connected) return
            networkStatus.first { it == NetworkStatus.Available }
        }

        override fun isConnected(): Boolean = connected
    }

    @Test
    fun `retryWhenConnected returns result on first try if successful`() = runTest {
        val monitor = FakeNetworkMonitor(connected = true)
        var attempts = 0
        
        val result = retryWhenConnected(monitor) {
            attempts++
            "Success"
        }
        
        assertEquals("Success", result)
        assertEquals(1, attempts)
    }

    @Test
    fun `retryWhenConnected retries on IOException and succeeds when connected`() = runTest {
        val monitor = FakeNetworkMonitor(connected = true)
        var attempts = 0
        
        val result = retryWhenConnected(monitor, delayMs = 1L) {
            attempts++
            if (attempts == 1) throw OfflineException()
            "Recovered"
        }
        
        assertEquals("Recovered", result)
        assertEquals(2, attempts)
    }

    @Test(expected = IOException::class)
    fun `retryWhenConnected throws after max retries`() = runTest {
        val monitor = FakeNetworkMonitor(connected = true)
        var attempts = 0
        
        retryWhenConnected(monitor, maxRetries = 2, delayMs = 1L) {
            attempts++
            throw IOException("Always fails")
        }
    }
}
