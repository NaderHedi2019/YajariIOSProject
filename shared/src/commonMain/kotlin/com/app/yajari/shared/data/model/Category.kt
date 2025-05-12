package com.app.yajari.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String,
    val name: String,
    val description: String? = null,
    val image: String? = null,
    val parentId: String? = null,
    val isActive: Boolean = true
)

@Serializable
data class CategoryResponse(
    val success: Boolean,
    val message: String? = null,
    val data: List<Category>? = null
) 