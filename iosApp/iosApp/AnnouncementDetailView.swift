import SwiftUI
import shared

struct AnnouncementDetailView: View {
    let announcement: Announcement
    @Environment(\._announcementRepository) private var announcementRepository
    @Environment(\.presentationMode) private var presentationMode
    
    @State private var isFavourite = false
    @State private var isLoading = false
    @State private var showError = false
    @State private var errorMessage = ""
    @State private var showDeleteConfirmation = false
    
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                // Image
                if let imageUrl = announcement.imageUrl {
                    AsyncImage(url: URL(string: imageUrl)) { image in
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                    } placeholder: {
                        Rectangle()
                            .fill(Color.gray.opacity(0.2))
                    }
                    .frame(height: 200)
                    .clipped()
                }
                
                VStack(alignment: .leading, spacing: 12) {
                    // Title
                    Text(announcement.title)
                        .font(.title)
                        .fontWeight(.bold)
                    
                    // Location
                    HStack {
                        Image(systemName: "location.fill")
                            .foregroundColor(.blue)
                        Text(announcement.location)
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                    
                    // Date
                    HStack {
                        Image(systemName: "calendar")
                            .foregroundColor(.blue)
                        Text(formatDate(announcement.createdAt))
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                    
                    Divider()
                    
                    // Description
                    Text("Description")
                        .font(.headline)
                    
                    Text(announcement.description)
                        .font(.body)
                    
                    // Actions
                    HStack(spacing: 20) {
                        Button(action: toggleFavourite) {
                            HStack {
                                Image(systemName: isFavourite ? "star.fill" : "star")
                                Text(isFavourite ? "Favourited" : "Favourite")
                            }
                            .foregroundColor(isFavourite ? .yellow : .blue)
                        }
                        
                        if announcement.isActive {
                            Button(action: { showDeleteConfirmation = true }) {
                                HStack {
                                    Image(systemName: "trash")
                                    Text("Delete")
                                }
                                .foregroundColor(.red)
                            }
                        }
                    }
                    .padding(.top)
                }
                .padding()
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .alert("Error", isPresented: $showError) {
            Button("OK", role: .cancel) { }
        } message: {
            Text(errorMessage)
        }
        .alert("Delete Announcement", isPresented: $showDeleteConfirmation) {
            Button("Cancel", role: .cancel) { }
            Button("Delete", role: .destructive) {
                deleteAnnouncement()
            }
        } message: {
            Text("Are you sure you want to delete this announcement? This action cannot be undone.")
        }
        .onAppear {
            checkFavouriteStatus()
        }
    }
    
    private func formatDate(_ timestamp: Double) -> String {
        let date = Date(timeIntervalSince1970: timestamp)
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .short
        return formatter.string(from: date)
    }
    
    private func checkFavouriteStatus() {
        isLoading = true
        
        announcementRepository.isFavourite(id: announcement.id) { result in
            isLoading = false
            
            switch result {
            case .success(let isFavourite):
                self.isFavourite = isFavourite
            case .failure(let error):
                errorMessage = error.localizedDescription
                showError = true
            }
        }
    }
    
    private func toggleFavourite() {
        isLoading = true
        
        if isFavourite {
            announcementRepository.removeFromFavourites(id: announcement.id) { result in
                handleFavouriteResult(result)
            }
        } else {
            announcementRepository.addToFavourites(id: announcement.id) { result in
                handleFavouriteResult(result)
            }
        }
    }
    
    private func handleFavouriteResult(_ result: Result<Unit, Error>) {
        isLoading = false
        
        switch result {
        case .success:
            isFavourite.toggle()
        case .failure(let error):
            errorMessage = error.localizedDescription
            showError = true
        }
    }
    
    private func deleteAnnouncement() {
        isLoading = true
        
        announcementRepository.deleteAnnouncement(id: announcement.id) { result in
            isLoading = false
            
            switch result {
            case .success:
                presentationMode.wrappedValue.dismiss()
            case .failure(let error):
                errorMessage = error.localizedDescription
                showError = true
            }
        }
    }
} 