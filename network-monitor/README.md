# Network Monitor

A production-ready Android library module for real-time internet connectivity monitoring,
exposing network state to Jetpack Compose and ViewModel layers via Kotlin Coroutines `StateFlow`.

---

## Features

-  **Real-time detection** via `ConnectivityManager.NetworkCallback`
-  **StateFlow** exposure for reactive observation
-  **Initial state detection** with `NET_CAPABILITY_VALIDATED`
-  **Compose-ready** with `rememberNetworkStatus()` & `ConnectivityBanner`
-  **Application-level state management** without modifying every screen
-  **OkHttp Interceptor** to block requests when offline
-  **Retry helper** — waits for connection, then retries
-  **Kotlin 2.x compatible**, **Material 3** compatible
-  **Fully unit tested**

---

## Setup

### 1. Add JitPack repository

In your root `settings.gradle.kts` or `build.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### 2. Add the dependency

In your `app/build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.<YourUsername>:network-monitor:<release_tag>")
}
```

---

## Core API

### `NetworkStatus`

```kotlin
sealed interface NetworkStatus {
    data object Available   : NetworkStatus  // Connected & validated
    data object Unavailable : NetworkStatus  // No network at all
    data object Losing      : NetworkStatus  // Connection degrading
    data object Lost        : NetworkStatus  // Connection dropped
}
```

### `NetworkMonitor`

```kotlin
interface NetworkMonitor {
    val networkStatus: StateFlow<NetworkStatus>
    suspend fun awaitConnection()   // Suspends until Available
    fun isConnected(): Boolean      // Sync check
}
```

---

## Usage

### 1. Application Setup

Initialize the `NetworkMonitor` in your `Application` class by implementing `NetworkMonitorProvider`. This allows you to manage the state directly at the application level.

```kotlin
import android.app.Application
import com.networkmonitor.network.DefaultNetworkMonitor
import com.networkmonitor.network.NetworkMonitor
import com.networkmonitor.network.compose.NetworkMonitorProvider

class MyApplication : Application(), NetworkMonitorProvider {
    override val networkMonitor: NetworkMonitor by lazy {
        DefaultNetworkMonitor(this)
    }
}
```

Register it in your `AndroidManifest.xml`:

```xml
<application
    android:name=".MyApplication"
    ...>
```

### 2. Compose — Global Offline Banner

Drop `ConnectivityBanner` at the root of your app to automatically show an offline banner globally without touching individual screen files:

```kotlin
setContent {
    MyTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                // Your app's main navigation / content goes here
                AppNavigation(modifier = Modifier.padding(innerPadding))
                
                // Overlay the banner globally
                ConnectivityBanner(
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}
```

### 3. Compose — Observe Status Manually

If you need to observe the network status manually in Compose:

```kotlin
@Composable
fun MyScreen() {
    val networkStatus = rememberNetworkStatus()

    when (networkStatus) {
        NetworkStatus.Available   -> OnlineContent()
        NetworkStatus.Unavailable,
        NetworkStatus.Lost        -> OfflinePlaceholder()
        NetworkStatus.Losing      -> DegradedBanner()
    }
}
```

### 4. ViewModel Usage

You can pass the `NetworkMonitor` to your ViewModels:

```kotlin
class HomeViewModel(private val networkMonitor: NetworkMonitor) : ViewModel() {

    val networkStatus = networkMonitor.networkStatus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NetworkStatus.Unavailable)

    fun fetchData() {
        viewModelScope.launch {
            retryWhenConnected(networkMonitor) {
                apiService.getData() // retried automatically when connection restores
            }
        }
    }
}
```

---

## OkHttp Integration

Add `NetworkAwareInterceptor` to your `OkHttpClient`:

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(NetworkAwareInterceptor(networkMonitor))
    .build()
```

When offline, the interceptor throws `OfflineException` before the network call is made.

---

## Retry Helper

```kotlin
// Executes block, retries up to 3 times on IOException (including OfflineException),
// waiting for connection to restore between attempts.
val result = retryWhenConnected(
    networkMonitor = networkMonitor,
    maxRetries = 3,
    delayMs = 1000L
) {
    apiService.fetchData()
}
```

---

## Permissions

The module automatically includes the required permissions in its `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.INTERNET" />
```

---

## License

```text
Copyright 2026

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
