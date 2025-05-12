import SwiftUI
import shared

struct MyFavouritesView: View {
    @Environment(\._announcementRepository) private var announcementRepository
    @State private var favourites: [Announcement] = []
    @State private var isLoading = false
    @State private var showError = false
    @State private var errorMessage = ""
    
    var body: some View {
        Group {
            if isLoading {
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle())
            } else if favourites.isEmpty {
                VStack(spacing: 16) {
                    Image(systemName: "star.slash")
                        .font(.system(size: 60))
                        .foregroundColor(.gray)
                    Text("No favourites yet")
                        .font(.title2)
                        .foregroundColor(.gray)
                    Text("Items you favourite will appear here")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
            } else {
                List {
                    ForEach(favourites, id: \.id) { announcement in
                        NavigationLink(destination: AnnouncementDetailView(announcement: announcement)) {
                            AnnouncementRowView(announcement: announcement)
                        }
                        .swipeActions(edge: .trailing) {
                            Button(role: .destructive) {
                                removeFromFavourites(announcement)
                            } label: {
                                Label("Remove", systemImage: "star.slash")
                            }
                        }
                    }
                }
            }
        }
        .navigationTitle("My Favourites")
        .alert("Error", isPresented: $showError) {
            Button("OK", role: .cancel) { }
        } message: {
            Text(errorMessage)
        }
        .onAppear {
            loadFavourites()
        }
    }
    
    private func loadFavourites() {
        isLoading = true
        
        announcementRepository.getFavourites { result in
            isLoading = false
            
            switch result {
            case .success(let favourites):
                self.favourites = favourites
            case .failure(let error):
                errorMessage = error.localizedDescription
                showError = true
            }
        }
    }
    
    private func removeFromFavourites(_ announcement: Announcement) {
        isLoading = true
        
        announcementRepository.removeFromFavourites(id: announcement.id) { result in
            isLoading = false
            
            switch result {
            case .success:
                favourites.removeAll { $0.id == announcement.id }
            case .failure(let error):
                errorMessage = error.localizedDescription
                showError = true
            }
        }
    }
} 