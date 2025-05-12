import SwiftUI
import shared

struct HomeView: View {
    @Environment(\._announcementRepository) private var announcementRepository
    @State private var selectedTab = 0
    @State private var donations: [Donation] = []
    @State private var requests: [Request] = []
    @State private var isLoading = false
    @State private var showError = false
    @State private var errorMessage = ""
    
    private let tabs = ["Donations", "Requests"]
    
    var body: some View {
        VStack {
            Picker("Select", selection: $selectedTab) {
                ForEach(0..<tabs.count, id: \.self) { index in
                    Text(tabs[index]).tag(index)
                }
            }
            .pickerStyle(SegmentedPickerStyle())
            .padding()
            
            if isLoading {
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle())
            } else {
                if selectedTab == 0 {
                    DonationListView(donations: donations)
                } else {
                    RequestListView(requests: requests)
                }
            }
        }
        .navigationTitle("Home")
        .background(Color(.systemGroupedBackground).ignoresSafeArea())
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
        
        // Load donations
        announcementRepository.getDonations { result in
            switch result {
            case .success(let donations):
                self.donations = donations
            case .failure(let error):
                errorMessage = error.localizedDescription
                showError = true
            }
            
            // Load requests
            announcementRepository.getRequests { result in
                isLoading = false
                
                switch result {
                case .success(let requests):
                    self.requests = requests
                case .failure(let error):
                    errorMessage = error.localizedDescription
                    showError = true
                }
            }
        }
    }
}

struct DonationListView: View {
    let donations: [Donation]
    
    var body: some View {
        List(donations, id: \.id) { donation in
            NavigationLink(destination: DonationDetailView(donation: donation)) {
                VStack(alignment: .leading, spacing: 8) {
                    Text(donation.title)
                        .font(.headline)
                    Text(donation.description_)
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                        .lineLimit(2)
                    HStack {
                        Image(systemName: "location.fill")
                            .foregroundColor(.blue)
                        Text(donation.location)
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
                .padding(.vertical, 4)
            }
        }
    }
}

struct RequestListView: View {
    let requests: [Request]
    
    var body: some View {
        List(requests, id: \.id) { request in
            NavigationLink(destination: RequestDetailView(request: request)) {
                VStack(alignment: .leading, spacing: 8) {
                    Text(request.title)
                        .font(.headline)
                    Text(request.description_)
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                        .lineLimit(2)
                    HStack {
                        Image(systemName: "person.fill")
                            .foregroundColor(.blue)
                        Text(request.requesterName)
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
                .padding(.vertical, 4)
            }
        }
    }
}

struct DonationDetailView: View {
    let donation: Donation
    
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Text(donation.title)
                    .font(.title)
                    .fontWeight(.bold)
                
                Text(donation.description_)
                    .font(.body)
                
                HStack {
                    Image(systemName: "location.fill")
                    Text(donation.location)
                }
                .foregroundColor(.secondary)
                
                if let imageUrl = donation.imageUrl {
                    AsyncImage(url: URL(string: imageUrl)) { image in
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fit)
                    } placeholder: {
                        ProgressView()
                    }
                }
                
                Button(action: {
                    // Handle contact action
                }) {
                    Text("Contact Donor")
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(10)
                }
            }
            .padding()
        }
        .navigationTitle("Donation Details")
    }
}

struct RequestDetailView: View {
    let request: Request
    
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Text(request.title)
                    .font(.title)
                    .fontWeight(.bold)
                
                Text(request.description_)
                    .font(.body)
                
                HStack {
                    Image(systemName: "person.fill")
                    Text(request.requesterName)
                }
                .foregroundColor(.secondary)
                
                Button(action: {
                    // Handle fulfill action
                }) {
                    Text("Fulfill Request")
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(10)
                }
            }
            .padding()
        }
        .navigationTitle("Request Details")
    }
} 