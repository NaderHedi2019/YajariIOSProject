package com.app.yajari.shared.network

import io.ktor.client.*

expect class ApiServiceFactory {
    fun createApiService(): ApiService
}

internal fun createApiService(httpClient: HttpClient): ApiService {
    return when (Platform.OS) {
        Platform.OS.ANDROID -> AndroidApiService(httpClient)
        Platform.OS.IOS -> IosApiService(httpClient)
        else -> throw IllegalStateException("Unsupported platform: ${Platform.OS}")
    }
} 