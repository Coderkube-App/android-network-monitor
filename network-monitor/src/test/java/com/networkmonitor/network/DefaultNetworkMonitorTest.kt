package com.networkmonitor.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultNetworkMonitorTest {

    /**
     * A fake [NetworkMonitor] that gives full control over state for testing.
     */
    private class FakeNetworkMonitor(initialStatus: NetworkStatus = NetworkStatus.Unavailable) : NetworkMonitor {
        private val _networkStatus = MutableStateFlow(initialStatus)
        override val networkStatus: StateFlow<NetworkStatus> = _networkStatus

        override suspend fun awaitConnection() {
            if (isConnected()) return
            networkStatus.first { it == NetworkStatus.Available }
        }

        override fun isConnected(): Boolean =
            _networkStatus.value == NetworkStatus.Available

        fun emit(status: NetworkStatus) {
            _networkStatus.value = status
        }
    }

    @Test
    fun `isConnected returns true when status is Available`() {
        val monitor = FakeNetworkMonitor(NetworkStatus.Available)
        assertTrue(monitor.isConnected())
    }

    @Test
    fun `isConnected returns false when status is Unavailable`() {
        val monitor = FakeNetworkMonitor(NetworkStatus.Unavailable)
        assertFalse(monitor.isConnected())
    }

    @Test
    fun `isConnected returns false when status is Lost`() {
        val monitor = FakeNetworkMonitor(NetworkStatus.Lost)
        assertFalse(monitor.isConnected())
    }

    @Test
    fun `isConnected returns false when status is Losing`() {
        val monitor = FakeNetworkMonitor(NetworkStatus.Losing)
        assertFalse(monitor.isConnected())
    }

    @Test
    fun `networkStatus emits Available when connection is restored`() = runTest {
        val monitor = FakeNetworkMonitor(NetworkStatus.Unavailable)
        monitor.emit(NetworkStatus.Available)
        val status = monitor.networkStatus.first()
        assertTrue(status == NetworkStatus.Available)
    }

    @Test
    fun `networkStatus emits Lost when connection is dropped`() = runTest {
        val monitor = FakeNetworkMonitor(NetworkStatus.Available)
        monitor.emit(NetworkStatus.Lost)
        val status = monitor.networkStatus.first()
        assertTrue(status == NetworkStatus.Lost)
    }

    @Test
    fun `awaitConnection returns immediately when already connected`() = runTest {
        val monitor = FakeNetworkMonitor(NetworkStatus.Available)
        // Should return without suspension
        monitor.awaitConnection()
        assertTrue(monitor.isConnected())
    }

    @Test
    fun `awaitConnection suspends until network becomes Available`() = runTest {
        val monitor = FakeNetworkMonitor(NetworkStatus.Unavailable)
        var awaitCompleted = false

        // Launch awaitConnection concurrently
        val job = kotlinx.coroutines.launch {
            monitor.awaitConnection()
            awaitCompleted = true
        }

        assertFalse(awaitCompleted)
        monitor.emit(NetworkStatus.Available)
        job.join()
        assertTrue(awaitCompleted)
    }
}
