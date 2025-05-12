import SwiftUI
import shared

struct PartnersView: View {
    @State private var partners: [Partner] = []
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
                        loadPartners()
                    }
                    .foregroundColor(.blue)
                }
                .padding()
            } else if partners.isEmpty {
                VStack(spacing: 20) {
                    Image(systemName: "person.2.slash.fill")
                        .resizable()
                        .frame(width: 50, height: 50)
                        .foregroundColor(.gray)
                    
                    Text("No partners found")
                        .foregroundColor(.gray)
                }
            } else {
                List(partners) { partner in
                    NavigationLink(destination: PartnerDetailView(partner: partner)) {
                        PartnerRowView(partner: partner)
                    }
                }
                .refreshable {
                    await loadPartners()
                }
            }
        }
        .navigationTitle("Our Partners")
        .task {
            await loadPartners()
        }
    }
    
    private func loadPartners() async {
        isLoading = true
        errorMessage = nil
        
        do {
            partners = try await NetworkManager.shared.fetchPartners()
        } catch {
            errorMessage = "Failed to load partners: \(error.localizedDescription)"
        }
        
        isLoading = false
    }
}

struct PartnerRowView: View {
    let partner: Partner
    
    var body: some View {
        HStack(spacing: 15) {
            AsyncImage(url: URL(string: partner.logoUrl)) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fit)
            } placeholder: {
                Image(systemName: "building.2.fill")
                    .foregroundColor(.gray)
            }
            .frame(width: 50, height: 50)
            .cornerRadius(8)
            
            VStack(alignment: .leading, spacing: 4) {
                Text(partner.name)
                    .font(.headline)
                
                Text(partner.description)
                    .font(.subheadline)
                    .foregroundColor(.gray)
                    .lineLimit(2)
            }
        }
        .padding(.vertical, 8)
    }
}

struct PartnerDetailView: View {
    let partner: Partner
    
    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                AsyncImage(url: URL(string: partner.logoUrl)) { image in
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                } placeholder: {
                    Image(systemName: "building.2.fill")
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .foregroundColor(.gray)
                }
                .frame(height: 200)
                .cornerRadius(12)
                
                Text(partner.name)
                    .font(.title)
                    .fontWeight(.bold)
                
                Text(partner.description)
                    .font(.body)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal)
                
                Link(destination: URL(string: partner.websiteUrl)!) {
                    Text("Visit Website")
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.blue)
                        .cornerRadius(10)
                }
                .padding(.horizontal)
            }
            .padding()
        }
        .navigationTitle("Partner Details")
    }
} 