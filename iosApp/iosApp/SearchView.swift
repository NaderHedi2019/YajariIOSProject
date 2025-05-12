import SwiftUI
import shared

struct SearchView: View {
    @Environment(\._searchRepository) private var searchRepository
    @State private var searchText = ""
    @State private var searchResults: SearchResults?
    @State private var isLoading = false
    @State private var showError = false
    @State private var errorMessage = ""
    @State private var selectedScope = 0
    
    var body: some View {
        VStack(spacing: 0) {
            // Search Scope
            Picker("Search Scope", selection: $selectedScope) {
                Text("All").tag(0)
                Text("Announcements").tag(1)
                Text("Users").tag(2)
            }
            .pickerStyle(SegmentedPickerStyle())
            .padding()
            
            if isLoading {
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle())
            } else if let results = searchResults {
                if results.isEmpty {
                    emptyResultsView
                } else {
                    searchResultsView(results)
                }
            } else {
                VStack(spacing: 16) {
                    Image(systemName: "magnifyingglass")
                        .font(.system(size: 60))
                        .foregroundColor(.gray)
                    Text("Search for announcements or users")
                        .font(.title2)
                        .foregroundColor(.gray)
                    Text("Type something to begin searching")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
            }
        }
        .searchable(text: $searchText, prompt: "Search")
        .onChange(of: searchText) { newValue in
            if !newValue.isEmpty {
                performSearch()
            } else {
                searchResults = nil
            }
        }
        .alert("Error", isPresented: $showError) {
            Button("OK", role: .cancel) { }
        } message: {
            Text(errorMessage)
        }
    }
    
    private var emptyResultsView: some View {
        VStack(spacing: 16) {
            Image(systemName: "magnifyingglass")
                .font(.system(size: 60))
                .foregroundColor(.gray)
            Text("No results found")
                .font(.title2)
                .foregroundColor(.gray)
            Text("Try different search terms")
                .font(.subheadline)
                .foregroundColor(.secondary)
        }
    }
    
    private func searchResultsView(_ results: SearchResults) -> some View {
        List {
            if !results.announcements.isEmpty && (selectedScope == 0 || selectedScope == 1) {
                Section(header: Text("Announcements")) {
                    ForEach(results.announcements, id: \.id) { announcement in
                        NavigationLink(destination: AnnouncementDetailView(announcement: announcement)) {
                            AnnouncementRowView(announcement: announcement)
                        }
                    }
                }
            }
            
            if !results.users.isEmpty && (selectedScope == 0 || selectedScope == 2) {
                Section(header: Text("Users")) {
                    ForEach(results.users, id: \.id) { user in
                        NavigationLink(destination: UserProfileView(user: user)) {
                            UserRowView(user: user)
                        }
                    }
                }
            }
        }
    }
    
    private func performSearch() {
        isLoading = true
        
        let scope: SearchScope
        switch selectedScope {
        case 1:
            scope = .announcements
        case 2:
            scope = .users
        default:
            scope = .all
        }
        
        searchRepository.search(query: searchText, scope: scope) { result in
            isLoading = false
            
            switch result {
            case .success(let results):
                self.searchResults = results
            case .failure(let error):
                errorMessage = error.localizedDescription
                showError = true
            }
        }
    }
}

struct UserRowView: View {
    let user: User
    
    var body: some View {
        HStack(spacing: 12) {
            AsyncImage(url: URL(string: user.profileImageUrl)) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } placeholder: {
                Image(systemName: "person.circle.fill")
                    .foregroundColor(.gray)
            }
            .frame(width: 40, height: 40)
            .clipShape(Circle())
            
            VStack(alignment: .leading, spacing: 4) {
                Text(user.name)
                    .font(.headline)
                
                Text(user.email)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }
        }
        .padding(.vertical, 4)
    }
} 