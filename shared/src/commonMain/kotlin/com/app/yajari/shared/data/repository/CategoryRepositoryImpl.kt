package com.app.yajari.shared.data.repository

import com.app.yajari.shared.data.model.Category
import com.app.yajari.shared.domain.repository.CategoryRepository
import com.app.yajari.shared.network.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CategoryRepositoryImpl(private val apiService: ApiService) : CategoryRepository {
    override suspend fun getCategories(): Flow<List<Category>> = flow {
        // Example: Replace with real API call
        emit(emptyList())
    }

    override suspend fun getCategoryById(id: String): Result<Category> {
        // Example: Replace with real API call
        return Result.failure(Exception("Not implemented"))
    }

    override suspend fun createCategory(category: Category): Result<Category> {
        // Example: Replace with real API call
        return Result.failure(Exception("Not implemented"))
    }

    override suspend fun updateCategory(category: Category): Result<Category> {
        // Example: Replace with real API call
        return Result.failure(Exception("Not implemented"))
    }

    override suspend fun deleteCategory(id: String): Result<Unit> {
        // Example: Replace with real API call
        return Result.failure(Exception("Not implemented"))
    }
} 