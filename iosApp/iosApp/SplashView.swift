import SwiftUI
import shared

struct SplashView: View {
    @Environment(\._userRepository) private var userRepository
    @State private var isActive = false
    @State private var showLanguageSelection = false
    @State private var isLoading = true
    @State private var showError = false
    @State private var errorMessage = ""
    
    var body: some View {
        if isActive {
            if showLanguageSelection {
                ChooseLanguageView()
            } else {
                MainAppView()
            }
        } else {
            VStack(spacing: 20) {
                // App Logo
                Image("AppLogo")
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .frame(width: 120, height: 120)
                    .cornerRadius(20)
                
                // App Name
                Text("Yajari")
                    .font(.largeTitle)
                    .fontWeight(.bold)
                
                // Loading Indicator
                if isLoading {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle())
                }
            }
            .alert("Error", isPresented: $showError) {
                Button("Retry", role: .cancel) {
                    checkInitialState()
                }
            } message: {
                Text(errorMessage)
            }
            .onAppear {
                checkInitialState()
            }
        }
    }
    
    private func checkInitialState() {
        isLoading = true
        
        // Check if language is selected
        userRepository.getSelectedLanguage { result in
            switch result {
            case .success(let language):
                if language == nil {
                    showLanguageSelection = true
                }
                isActive = true
            case .failure(let error):
                errorMessage = error.localizedDescription
                showError = true
            }
            isLoading = false
        }
    }
} 