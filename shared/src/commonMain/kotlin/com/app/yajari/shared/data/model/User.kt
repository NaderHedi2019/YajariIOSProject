package com.app.yajari.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String? = null,
    val profileImage: String? = null,
    val token: String? = null
) 