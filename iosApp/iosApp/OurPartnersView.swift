import SwiftUI
import shared

struct OurPartnersView: View {
    @Environment(\._communityRepository) private var communityRepository
    @State private var partners: [Partner] = []
    @State private var isLoading = false
    @State private var showError = false
    @State private var errorMessage = ""
    
    var body: some View {
        Group {
            if isLoading {
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle())
            } else if partners.isEmpty {
                VStack(spacing: 16) {
                    Image(systemName: "building.2")
                        .font(.system(size: 60))
                        .foregroundColor(.gray)
                    Text("No partners yet")
                        .font(.title2)
                        .foregroundColor(.gray)
                    Text("Our partners will appear here")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
            } else {
                List {
                    ForEach(partners, id: \.id) { partner in
                        PartnerRowView(partner: partner)
                    }
                }
            }
        }
        .navigationTitle("Our Partners")
        .alert("Error", isPresented: $showError) {
            Button("OK", role: .cancel) { }
        } message: {
            Text(errorMessage)
        }
        .onAppear {
            loadPartners()
        }
    }
    
    private func loadPartners() {
        isLoading = true
        
        communityRepository.getPartners { result in
            isLoading = false
            
            switch result {
            case .success(let partners):
                self.partners = partners
            case .failure(let error):
                errorMessage = error.localizedDescription
                showError = true
            }
        }
    }
}

struct PartnerRowView: View {
    let partner: Partner
    @State private var showDetails = false
    
    var body: some View {
        Button(action: { showDetails = true }) {
            HStack(spacing: 16) {
                AsyncImage(url: URL(string: partner.logoUrl)) { image in
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                } placeholder: {
                    Image(systemName: "building.2")
                        .foregroundColor(.gray)
                }
                .frame(width: 60, height: 60)
                .background(Color(.systemGray6))
                .cornerRadius(8)
                
                VStack(alignment: .leading, spacing: 4) {
                    Text(partner.name)
                        .font(.headline)
                    
                    Text(partner.description)
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                        .lineLimit(2)
                    
                    HStack {
                        Image(systemName: "location.fill")
                            .foregroundColor(.blue)
                        Text(partner.location)
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
            }
            .padding(.vertical, 8)
        }
        .buttonStyle(PlainButtonStyle())
        .sheet(isPresented: $showDetails) {
            PartnerDetailView(partner: partner)
        }
    }
}

struct PartnerDetailView: View {
    let partner: Partner
    @Environment(\.presentationMode) private var presentationMode
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    // Logo
                    AsyncImage(url: URL(string: partner.logoUrl)) { image in
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fit)
                    } placeholder: {
                        Image(systemName: "building.2")
                            .foregroundColor(.gray)
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 200)
                    .background(Color(.systemGray6))
                    .cornerRadius(12)
                    
                    // Name and Location
                    VStack(alignment: .leading, spacing: 8) {
                        Text(partner.name)
                            .font(.title)
                            .fontWeight(.bold)
                        
                        HStack {
                            Image(systemName: "location.fill")
                                .foregroundColor(.blue)
                            Text(partner.location)
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                        }
                    }
                    
                    // Description
                    Text("About")
                        .font(.title2)
                        .fontWeight(.bold)
                    
                    Text(partner.description)
                        .font(.body)
                    
                    // Contact Information
                    if let website = partner.website {
                        Link(destination: URL(string: website)!) {
                            HStack {
                                Image(systemName: "globe")
                                Text("Visit Website")
                            }
                            .padding()
                            .frame(maxWidth: .infinity)
                            .background(Color.blue)
                            .foregroundColor(.white)
                            .cornerRadius(10)
                        }
                    }
                }
                .padding()
            }
            .navigationTitle("Partner Details")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") {
                        presentationMode.wrappedValue.dismiss()
                    }
                }
            }
        }
    }
} 