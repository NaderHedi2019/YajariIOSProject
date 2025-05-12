import SwiftUI
import shared

struct CommunityImpactView: View {
    @Environment(\._communityRepository) private var communityRepository
    @State private var impactStats: CommunityImpactStats?
    @State private var recentActivities: [CommunityActivity] = []
    @State private var isLoading = false
    @State private var showError = false
    @State private var errorMessage = ""
    
    var body: some View {
        Group {
            if isLoading {
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle())
            } else if let stats = impactStats {
                ScrollView {
                    VStack(spacing: 24) {
                        // Impact Stats Section
                        VStack(spacing: 16) {
                            Text("Community Impact")
                                .font(.title)
                                .fontWeight(.bold)
                            
                            HStack(spacing: 20) {
                                StatCard(
                                    title: "Donations",
                                    value: "\(stats.totalDonations)",
                                    icon: "gift.fill",
                                    color: .blue
                                )
                                
                                StatCard(
                                    title: "Requests",
                                    value: "\(stats.totalRequests)",
                                    icon: "hand.raised.fill",
                                    color: .green
                                )
                            }
                            
                            HStack(spacing: 20) {
                                StatCard(
                                    title: "Users",
                                    value: "\(stats.totalUsers)",
                                    icon: "person.3.fill",
                                    color: .orange
                                )
                                
                                StatCard(
                                    title: "Impact",
                                    value: "\(stats.impactScore)",
                                    icon: "chart.line.uptrend.xyaxis",
                                    color: .purple
                                )
                            }
                        }
                        .padding()
                        .background(Color(.systemBackground))
                        .cornerRadius(12)
                        .shadow(radius: 2)
                        
                        // Recent Activities Section
                        VStack(alignment: .leading, spacing: 16) {
                            Text("Recent Activities")
                                .font(.title2)
                                .fontWeight(.bold)
                            
                            ForEach(recentActivities, id: \.id) { activity in
                                ActivityRowView(activity: activity)
                            }
                        }
                        .padding()
                        .background(Color(.systemBackground))
                        .cornerRadius(12)
                        .shadow(radius: 2)
                    }
                    .padding()
                }
                .background(Color(.systemGroupedBackground))
            }
        }
        .navigationTitle("Community Impact")
        .alert("Error", isPresented: $showError) {
            Button("OK", role: .cancel) { }
        } message: {
            Text(errorMessage)
        }
        .onAppear {
            loadData()
        }
    }
    
    private func loadData() {
        isLoading = true
        
        // Load impact stats
        communityRepository.getImpactStats { result in
            switch result {
            case .success(let stats):
                self.impactStats = stats
                
                // Load recent activities
                communityRepository.getRecentActivities { result in
                    isLoading = false
                    
                    switch result {
                    case .success(let activities):
                        self.recentActivities = activities
                    case .failure(let error):
                        errorMessage = error.localizedDescription
                        showError = true
                    }
                }
            case .failure(let error):
                isLoading = false
                errorMessage = error.localizedDescription
                showError = true
            }
        }
    }
}

struct StatCard: View {
    let title: String
    let value: String
    let icon: String
    let color: Color
    
    var body: some View {
        VStack(spacing: 8) {
            Image(systemName: icon)
                .font(.system(size: 24))
                .foregroundColor(color)
            
            Text(value)
                .font(.title2)
                .fontWeight(.bold)
            
            Text(title)
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(color.opacity(0.1))
        .cornerRadius(8)
    }
}

struct ActivityRowView: View {
    let activity: CommunityActivity
    
    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: activity.icon)
                .font(.system(size: 24))
                .foregroundColor(.blue)
                .frame(width: 40, height: 40)
                .background(Color.blue.opacity(0.1))
                .cornerRadius(8)
            
            VStack(alignment: .leading, spacing: 4) {
                Text(activity.title)
                    .font(.headline)
                
                Text(activity.description)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                
                Text(formatDate(activity.timestamp))
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
        }
        .padding(.vertical, 4)
    }
    
    private func formatDate(_ timestamp: Double) -> String {
        let date = Date(timeIntervalSince1970: timestamp)
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .short
        return formatter.string(from: date)
    }
} 