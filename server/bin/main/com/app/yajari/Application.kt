package com.app.yajari

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
import io.ktor.server.response.*
import io.ktor.server.request.*

fun main() {
    embeddedServer(
        Netty,
        host = "0.0.0.0",  // Listen on all network interfaces
        port = 8080,
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    // Configure CORS
    install(CORS) {
        anyHost()
        allowHeader("Content-Type")
        allowHeader("Authorization")
        allowMethod(io.ktor.http.HttpMethod.Options)
        allowMethod(io.ktor.http.HttpMethod.Put)
        allowMethod(io.ktor.http.HttpMethod.Delete)
        allowMethod(io.ktor.http.HttpMethod.Patch)
    }
    
    // Configure Content Negotiation
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }
    
    // Configure Authentication
    install(Authentication) {
        jwt("auth-jwt") {
            // Configure JWT validation
            verifier(JwtConfig.verifier)
            validate { credential ->
                if (credential.payload.getClaim("username").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
    
    // Configure Routing
    routing {
        // Health check endpoint
        get("/health") {
            call.respond(mapOf("status" to "OK"))
        }
        
        // Include other route modules
        // authRoutes()
        // userRoutes()
        // announcementRoutes()
        // chatRoutes()
    }
} 