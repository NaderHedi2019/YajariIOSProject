import SwiftUI
import shared

struct AnnouncementListView: View {
    @State private var announcements: [Announcement] = []
    let announcementRepository: AnnouncementRepository

    var body: some View {
        List(announcements, id: \.id) { announcement in
            NavigationLink(destination: AnnouncementDetailView(announcement: announcement, announcementRepository: announcementRepository)) {
                VStack(alignment: .leading) {
                    Text(announcement.title)
                        .font(.headline)
                    Text(announcement.description)
                        .font(.subheadline)
                        .foregroundColor(.gray)
                }
            }
        }
        .onAppear {
            loadAnnouncements()
        }
    }

    private func loadAnnouncements() {
        announcementRepository.getAnnouncements { result in
            switch result {
            case .success(let loadedAnnouncements):
                announcements = loadedAnnouncements
            case .failure(let error):
                print("Failed to load announcements: \(error.localizedDescription)")
            }
        }
    }
} 