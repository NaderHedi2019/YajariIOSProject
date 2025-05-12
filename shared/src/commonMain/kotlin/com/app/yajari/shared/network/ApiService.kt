package com.app.yajari.shared.network

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

interface ApiService {
    suspend fun get(endpoint: String): HttpResponse
    suspend fun post(endpoint: String, body: Any): HttpResponse
    suspend fun put(endpoint: String, body: Any): HttpResponse
    suspend fun delete(endpoint: String): HttpResponse
    
    suspend fun getWithAuth(endpoint: String, token: String): HttpResponse
    suspend fun postWithAuth(endpoint: String, body: Any, token: String): HttpResponse
    suspend fun putWithAuth(endpoint: String, body: Any, token: String): HttpResponse
    suspend fun deleteWithAuth(endpoint: String, token: String): HttpResponse
} 