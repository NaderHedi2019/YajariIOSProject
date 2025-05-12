import SwiftUI

struct MainAppView: View {
    @Environment(\._userRepository) private var userRepository
    @Environment(\._announcementRepository) private var announcementRepository
    var body: some View {
        TabView {
            NavigationView { HomeView() }
                .tabItem {
                    Image(systemName: "house.fill")
                    Text("Home")
                }
            NavigationView { AnnouncementListView(announcementRepository: announcementRepository) }
                .tabItem {
                    Image(systemName: "megaphone.fill")
                    Text("Announcements")
                }
            NavigationView { ChatListView() }
                .tabItem {
                    Image(systemName: "bubble.left.and.bubble.right.fill")
                    Text("Chat")
                }
            NavigationView { ProfileView(userRepository: userRepository) }
                .tabItem {
                    Image(systemName: "person.crop.circle")
                    Text("Profile")
                }
        }
        .accentColor(.blue)
    }
}

// Placeholder HomeView
struct HomeView: View {
    var body: some View {
        VStack(spacing: 24) {
            Text("Home Screen")
                .font(.largeTitle)
                .fontWeight(.bold)
            Text("Welcome to Yajari!")
                .font(.title3)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color(.systemGroupedBackground))
    }
}

// Placeholder ChatListView
struct ChatListView: View {
    var body: some View {
        VStack(spacing: 24) {
            Text("Chat List Screen")
                .font(.largeTitle)
                .fontWeight(.bold)
            Text("Your recent conversations will appear here.")
                .font(.title3)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color(.systemGroupedBackground))
    }
}

// Placeholder ProfileView
struct ProfileView: View {
    let userRepository: UserRepository
    var body: some View {
        VStack(spacing: 24) {
            Text("Profile Screen")
                .font(.largeTitle)
                .fontWeight(.bold)
            Text("Manage your account and settings.")
                .font(.title3)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color(.systemGroupedBackground))
    }
} 