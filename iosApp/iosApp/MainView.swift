import SwiftUI
import shared

struct MainView: View {
    @State private var isAuthenticated = false
    
    var body: some View {
        Group {
            if isAuthenticated {
                TabView {
                    NavigationView {
                        PartnersView()
                    }
                    .tabItem {
                        Label("Partners", systemImage: "person.2.fill")
                    }
                    
                    NavigationView {
                        AnnouncementsView()
                    }
                    .tabItem {
                        Label("Announcements", systemImage: "megaphone.fill")
                    }
                    
                    NavigationView {
                        ProfileView(onLogout: {
                            NetworkManager.shared.logout()
                            isAuthenticated = false
                        })
                    }
                    .tabItem {
                        Label("Profile", systemImage: "person.circle.fill")
                    }
                }
            } else {
                LoginView(onLoginSuccess: {
                    isAuthenticated = true
                })
            }
        }
        .onAppear {
            // Check if we have a stored token
            if let _ = try? KeychainService.shared.getToken() {
                isAuthenticated = true
            }
        }
    }
}

struct ProfileView: View {
    let onLogout: () -> Void
    
    var body: some View {
        VStack(spacing: 20) {
            Image(systemName: "person.circle.fill")
                .resizable()
                .frame(width: 100, height: 100)
                .foregroundColor(.blue)
            
            Text("Profile")
                .font(.title)
                .fontWeight(.bold)
            
            Button(action: onLogout) {
                Text("Logout")
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.red)
                    .cornerRadius(10)
            }
            .padding(.horizontal)
        }
        .padding()
        .navigationTitle("Profile")
    }
} 