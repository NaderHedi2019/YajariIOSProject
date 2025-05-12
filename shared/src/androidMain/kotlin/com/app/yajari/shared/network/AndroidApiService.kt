package com.app.yajari.shared.network

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class AndroidApiService(private val httpClient: HttpClient) : ApiService {
    override suspend fun get(endpoint: String): HttpResponse {
        return httpClient.get(endpoint)
    }

    override suspend fun post(endpoint: String, body: Any): HttpResponse {
        return httpClient.post(endpoint) {
            setBody(body)
        }
    }

    override suspend fun put(endpoint: String, body: Any): HttpResponse {
        return httpClient.put(endpoint) {
            setBody(body)
        }
    }

    override suspend fun delete(endpoint: String): HttpResponse {
        return httpClient.delete(endpoint)
    }

    override suspend fun getWithAuth(endpoint: String, token: String): HttpResponse {
        return httpClient.get(endpoint) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }

    override suspend fun postWithAuth(endpoint: String, body: Any, token: String): HttpResponse {
        return httpClient.post(endpoint) {
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(body)
        }
    }

    override suspend fun putWithAuth(endpoint: String, body: Any, token: String): HttpResponse {
        return httpClient.put(endpoint) {
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(body)
        }
    }

    override suspend fun deleteWithAuth(endpoint: String, token: String): HttpResponse {
        return httpClient.delete(endpoint) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }
} 