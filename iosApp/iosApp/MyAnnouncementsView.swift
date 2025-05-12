import SwiftUI
import shared

struct MyAnnouncementsView: View {
    @Environment(\._announcementRepository) private var announcementRepository
    @State private var announcements: [Announcement] = []
    @State private var isLoading = false
    @State private var showError = false
    @State private var errorMessage = ""
    @State private var showDeleteConfirmation = false
    @State private var announcementToDelete: Announcement?
    
    var body: some View {
        Group {
            if isLoading {
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle())
            } else if announcements.isEmpty {
                VStack(spacing: 16) {
                    Image(systemName: "megaphone")
                        .font(.system(size: 60))
                        .foregroundColor(.gray)
                    Text("No announcements yet")
                        .font(.title2)
                        .foregroundColor(.gray)
                    NavigationLink(destination: AnnouncementSubmissionView()) {
                        Text("Create Announcement")
                            .padding()
                            .background(Color.blue)
                            .foregroundColor(.white)
                            .cornerRadius(10)
                    }
                }
            } else {
                List {
                    ForEach(announcements, id: \.id) { announcement in
                        NavigationLink(destination: AnnouncementDetailView(announcement: announcement)) {
                            AnnouncementRowView(announcement: announcement)
                        }
                        .swipeActions(edge: .trailing) {
                            Button(role: .destructive) {
                                announcementToDelete = announcement
                                showDeleteConfirmation = true
                            } label: {
                                Label("Delete", systemImage: "trash")
                            }
                        }
                    }
                }
            }
        }
        .navigationTitle("My Announcements")
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                NavigationLink(destination: AnnouncementSubmissionView()) {
                    Image(systemName: "plus")
                }
            }
        }
        .alert("Error", isPresented: $showError) {
            Button("OK", role: .cancel) { }
        } message: {
            Text(errorMessage)
        }
        .alert("Delete Announcement", isPresented: $showDeleteConfirmation) {
            Button("Cancel", role: .cancel) { }
            Button("Delete", role: .destructive) {
                if let announcement = announcementToDelete {
                    deleteAnnouncement(announcement)
                }
            }
        } message: {
            Text("Are you sure you want to delete this announcement?")
        }
        .onAppear {
            loadAnnouncements()
        }
    }
    
    private func loadAnnouncements() {
        isLoading = true
        
        announcementRepository.getMyAnnouncements { result in
            isLoading = false
            
            switch result {
            case .success(let announcements):
                self.announcements = announcements
            case .failure(let error):
                errorMessage = error.localizedDescription
                showError = true
            }
        }
    }
    
    private func deleteAnnouncement(_ announcement: Announcement) {
        isLoading = true
        
        announcementRepository.deleteAnnouncement(id: announcement.id) { result in
            isLoading = false
            
            switch result {
            case .success:
                announcements.removeAll { $0.id == announcement.id }
            case .failure(let error):
                errorMessage = error.localizedDescription
                showError = true
            }
        }
    }
}

struct AnnouncementRowView: View {
    let announcement: Announcement
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(announcement.title)
                .font(.headline)
            
            Text(announcement.description_)
                .font(.subheadline)
                .foregroundColor(.secondary)
                .lineLimit(2)
            
            HStack {
                Image(systemName: "calendar")
                    .foregroundColor(.blue)
                Text(formatDate(announcement.createdAt))
                    .font(.caption)
                    .foregroundColor(.secondary)
                
                Spacer()
                
                if announcement.isActive {
                    Text("Active")
                        .font(.caption)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(Color.green.opacity(0.2))
                        .foregroundColor(.green)
                        .cornerRadius(4)
                }
            }
        }
        .padding(.vertical, 4)
    }
    
    private func formatDate(_ timestamp: Double) -> String {
        let date = Date(timeIntervalSince1970: timestamp)
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        return formatter.string(from: date)
    }
} 