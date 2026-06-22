package com.networkmonitor.network

import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkAwareInterceptorTest {

    @Test
    fun `proceeds with request when connected`() {
        val monitor = RetryHelperTest.FakeNetworkMonitor(connected = true)
        val interceptor = NetworkAwareInterceptor(monitor)
        
        val request = Request.Builder().url("https://example.com").build()
        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .build()
            
        val chain = object : Interceptor.Chain {
            override fun request(): Request = request
            override fun proceed(request: Request): Response = response
            override fun connection() = null
            override fun call() = throw NotImplementedError()
            override fun connectTimeoutMillis() = 0
            override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
            override fun readTimeoutMillis() = 0
            override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
            override fun writeTimeoutMillis() = 0
            override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
        }

        val result = interceptor.intercept(chain)
        assertEquals(200, result.code)
    }

    @Test(expected = OfflineException::class)
    fun `throws OfflineException when not connected`() {
        val monitor = RetryHelperTest.FakeNetworkMonitor(connected = false)
        val interceptor = NetworkAwareInterceptor(monitor)
        
        val chain = object : Interceptor.Chain {
            override fun request(): Request = Request.Builder().url("https://example.com").build()
            override fun proceed(request: Request): Response = throw AssertionError("Should not proceed")
            override fun connection() = null
            override fun call() = throw NotImplementedError()
            override fun connectTimeoutMillis() = 0
            override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
            override fun readTimeoutMillis() = 0
            override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
            override fun writeTimeoutMillis() = 0
            override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
        }

        interceptor.intercept(chain)
    }
}
