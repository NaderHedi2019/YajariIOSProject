import SwiftUI
import shared

struct ProfileView: View {
    @Environment(\._userRepository) private var userRepository
    @EnvironmentObject private var flow: AppFlow
    @State private var user: User?
    @State private var isLoading = false
    @State private var showError = false
    @State private var errorMessage = ""
    
    var body: some View {
        Group {
            if isLoading {
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle())
            } else if let user = user {
                List {
                    Section {
                        HStack(spacing: 16) {
                            AsyncImage(url: URL(string: user.avatarUrl ?? "")) { image in
                                image
                                    .resizable()
                                    .aspectRatio(contentMode: .fill)
                            } placeholder: {
                                Image(systemName: "person.circle.fill")
                                    .foregroundColor(.gray)
                            }
                            .frame(width: 80, height: 80)
                            .clipShape(Circle())
                            
                            VStack(alignment: .leading, spacing: 4) {
                                Text(user.name)
                                    .font(.title2)
                                    .fontWeight(.bold)
                                Text(user.email)
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)
                            }
                        }
                        .padding(.vertical, 8)
                    }
                    
                    Section(header: Text("Account")) {
                        NavigationLink(destination: EditProfileView(user: user)) {
                            Label("Edit Profile", systemImage: "person.crop.circle")
                        }
                        NavigationLink(destination: AlertSettingsView()) {
                            Label("Alert Settings", systemImage: "bell.badge")
                        }
                    }
                    
                    Section(header: Text("My Stuff")) {
                        NavigationLink(destination: MyAnnouncementsView()) {
                            Label("My Announcements", systemImage: "megaphone")
                        }
                        NavigationLink(destination: MyFavouritesView()) {
                            Label("My Favourites", systemImage: "star.fill")
                        }
                    }
                    
                    Section(header: Text("Community")) {
                        NavigationLink(destination: CommunityImpactView()) {
                            Label("Community Impact", systemImage: "person.3.fill")
                        }
                        NavigationLink(destination: OurPartnersView()) {
                            Label("Our Partners", systemImage: "hands.sparkles.fill")
                        }
                    }
                    
                    Section {
                        Button(role: .destructive) {
                            flow.handleLogout()
                        } label: {
                            Label("Logout", systemImage: "rectangle.portrait.and.arrow.right")
                        }
                    }
                }
                .listStyle(InsetGroupedListStyle())
            }
        }
        .navigationTitle("Profile")
        .alert("Error", isPresented: $showError) {
            Button("OK", role: .cancel) { }
        } message: {
            Text(errorMessage)
        }
        .onAppear {
            loadUserProfile()
        }
    }
    
    private func loadUserProfile() {
        isLoading = true
        
        userRepository.getCurrentUser { result in
            isLoading = false
            
            switch result {
            case .success(let user):
                self.user = user
            case .failure(let error):
                errorMessage = error.localizedDescription
                showError = true
            }
        }
    }
} 