import Foundation
import shared

class NetworkManager {
    static let shared = NetworkManager()
    
    #if DEBUG
    private let baseURL = "http://localhost:8080"
    #else
    // Replace this with your public IP or domain
    private let baseURL = "http://YOUR_PUBLIC_IP:8080"
    #endif
    
    private var authToken: String? {
        get {
            try? KeychainService.shared.getToken()
        }
        set {
            if let token = newValue {
                try? KeychainService.shared.saveToken(token)
            } else {
                try? KeychainService.shared.deleteToken()
            }
        }
    }
    
    private init() {}
    
    // MARK: - Authentication
    
    func login(username: String, password: String) async throws -> String {
        let url = URL(string: "\(baseURL)/auth/login")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let body = ["username": username, "password": password]
        request.httpBody = try JSONSerialization.data(withJSONObject: body)
        
        let (data, response) = try await URLSession.shared.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse else {
            throw NetworkError.invalidResponse
        }
        
        guard httpResponse.statusCode == 200 else {
            throw NetworkError.serverError(httpResponse.statusCode)
        }
        
        let token = try JSONDecoder().decode(TokenResponse.self, from: data)
        self.authToken = token.token
        return token.token
    }
    
    func register(username: String, password: String) async throws {
        let url = URL(string: "\(baseURL)/auth/register")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let body = ["username": username, "password": password]
        request.httpBody = try JSONSerialization.data(withJSONObject: body)
        
        let (_, response) = try await URLSession.shared.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse else {
            throw NetworkError.invalidResponse
        }
        
        guard httpResponse.statusCode == 201 else {
            throw NetworkError.serverError(httpResponse.statusCode)
        }
    }
    
    func logout() {
        authToken = nil
    }
    
    // MARK: - Data Fetching
    
    func fetchPartners() async throws -> [Partner] {
        let url = URL(string: "\(baseURL)/partners")!
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        
        if let token = authToken {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        
        let (data, response) = try await URLSession.shared.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse else {
            throw NetworkError.invalidResponse
        }
        
        guard httpResponse.statusCode == 200 else {
            throw NetworkError.serverError(httpResponse.statusCode)
        }
        
        return try JSONDecoder().decode([Partner].self, from: data)
    }
    
    func fetchAnnouncements() async throws -> [Announcement] {
        let url = URL(string: "\(baseURL)/announcements")!
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        
        if let token = authToken {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        
        let (data, response) = try await URLSession.shared.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse else {
            throw NetworkError.invalidResponse
        }
        
        guard httpResponse.statusCode == 200 else {
            throw NetworkError.serverError(httpResponse.statusCode)
        }
        
        return try JSONDecoder().decode([Announcement].self, from: data)
    }
    
    // MARK: - Error Handling
    
    enum NetworkError: Error {
        case invalidResponse
        case serverError(Int)
        case decodingError
        case unauthorized
    }
}

// MARK: - Response Models

struct TokenResponse: Codable {
    let token: String
}

struct Partner: Codable {
    let id: Int
    let name: String
    let description: String
    let logoUrl: String
    let websiteUrl: String
}

struct Announcement: Codable {
    let id: Int
    let title: String
    let content: String
    let createdAt: String
    let author: String
} 