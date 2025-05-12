package com.app.yajari.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.jwt.*

object JwtConfig {
    private const val secret = "your-secret-key" // In production, use environment variable
    private const val issuer = "yajari-app"
    private const val audience = "yajari-users"
    private const val realm = "yajari app"
    
    val verifier = JWT.require(Algorithm.HMAC256(secret))
        .withIssuer(issuer)
        .build()
    
    fun makeToken(username: String): String {
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("username", username)
            .sign(Algorithm.HMAC256(secret))
    }
} 