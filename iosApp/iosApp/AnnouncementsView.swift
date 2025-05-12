import SwiftUI
import shared

struct AnnouncementsView: View {
    @State private var announcements: [Announcement] = []
    @State private var isLoading = false
    @State private var errorMessage: String?
    
    var body: some View {
        Group {
            if isLoading {
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle())
            } else if let error = errorMessage {
                VStack(spacing: 20) {
                    Image(systemName: "exclamationmark.triangle.fill")
                        .resizable()
                        .frame(width: 50, height: 50)
                        .foregroundColor(.red)
                    
                    Text(error)
                        .multilineTextAlignment(.center)
                    
                    Button("Try Again") {
                        loadAnnouncements()
                    }
                    .foregroundColor(.blue)
                }
                .padding()
            } else if announcements.isEmpty {
                VStack(spacing: 20) {
                    Image(systemName: "megaphone.slash.fill")
                        .resizable()
                        .frame(width: 50, height: 50)
                        .foregroundColor(.gray)
                    
                    Text("No announcements yet")
                        .foregroundColor(.gray)
                }
            } else {
                List(announcements) { announcement in
                    NavigationLink(destination: AnnouncementDetailView(announcement: announcement)) {
                        AnnouncementRowView(announcement: announcement)
                    }
                }
                .refreshable {
                    await loadAnnouncements()
                }
            }
        }
        .navigationTitle("Announcements")
        .task {
            await loadAnnouncements()
        }
    }
    
    private func loadAnnouncements() async {
        isLoading = true
        errorMessage = nil
        
        do {
            announcements = try await NetworkManager.shared.fetchAnnouncements()
        } catch {
            errorMessage = "Failed to load announcements: \(error.localizedDescription)"
        }
        
        isLoading = false
    }
}

struct AnnouncementRowView: View {
    let announcement: Announcement
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(announcement.title)
                .font(.headline)
            
            Text(announcement.content)
                .font(.subheadline)
                .foregroundColor(.gray)
                .lineLimit(2)
            
            HStack {
                Text(announcement.author)
                    .font(.caption)
                    .foregroundColor(.blue)
                
                Spacer()
                
                Text(announcement.createdAt)
                    .font(.caption)
                    .foregroundColor(.gray)
            }
        }
        .padding(.vertical, 8)
    }
}

struct AnnouncementDetailView: View {
    let announcement: Announcement
    
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                Text(announcement.title)
                    .font(.title)
                    .fontWeight(.bold)
                
                HStack {
                    Text("By \(announcement.author)")
                        .font(.subheadline)
                        .foregroundColor(.blue)
                    
                    Spacer()
                    
                    Text(announcement.createdAt)
                        .font(.subheadline)
                        .foregroundColor(.gray)
                }
                
                Text(announcement.content)
                    .font(.body)
                
                Spacer()
            }
            .padding()
        }
        .navigationTitle("Announcement")
    }
} 