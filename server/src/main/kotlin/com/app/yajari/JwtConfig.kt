package com.app.yajari

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm

object JwtConfig {
    private const val secret = "your-secret-key" // TODO: Use a secure, environment-based secret in production
    private const val issuer = "com.app.yajari"
    private const val validityInMs = 36_000_00 * 24 // 24 hours
    private val algorithm = Algorithm.HMAC256(secret)

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .build()

    fun generateToken(username: String): String = JWT.create()
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withClaim("username", username)
        .withExpiresAt(getExpiration())
        .sign(algorithm)

    private fun getExpiration() = java.util.Date(System.currentTimeMillis() + validityInMs)
} 