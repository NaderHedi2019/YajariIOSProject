package com.app.yajari.shared.network

actual class ApiServiceFactory {
    actual fun createApiService(): ApiService {
        return createApiService(NetworkConfig.createHttpClient())
    }
} 