package com.app.yajari.shared.domain.repository

import com.app.yajari.shared.data.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(name: String, email: String, password: String): Result<User>
    suspend fun updateProfile(user: User): Result<User>
    suspend fun getCurrentUser(): Flow<User?>
    suspend fun logout()
} 