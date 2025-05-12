import SwiftUI
import shared

enum AppScreen {
    case splash
    case chooseLanguage
    case login
    case forgotPassword
    case main
}

class AppFlow: ObservableObject {
    @Published var currentScreen: AppScreen = .splash
    @Published var isAuthenticated = false
    @Published var selectedLanguage = "English"
    
    let userRepository: UserRepository
    
    init(userRepository: UserRepository) {
        self.userRepository = userRepository
    }
    
    func checkAuthStatus() {
        // Check if user is logged in
        userRepository.getCurrentUser { [weak self] result in
            DispatchQueue.main.async {
                switch result {
                case .success(let user):
                    self?.isAuthenticated = user != nil
                    self?.currentScreen = user != nil ? .main : .login
                case .failure:
                    self?.isAuthenticated = false
                    self?.currentScreen = .login
                }
            }
        }
    }
    
    func handleLanguageSelection(_ language: String) {
        selectedLanguage = language
        currentScreen = .login
    }
    
    func handleLogin() {
        isAuthenticated = true
        currentScreen = .main
    }
    
    func handleLogout() {
        userRepository.logout()
        isAuthenticated = false
        currentScreen = .login
    }
}

struct AppFlowView: View {
    @StateObject private var flow: AppFlow
    @Environment(\._userRepository) private var userRepository
    
    init() {
        _flow = StateObject(wrappedValue: AppFlow(userRepository: userRepository))
    }
    
    var body: some View {
        Group {
            switch flow.currentScreen {
            case .splash:
                SplashView()
                    .onAppear {
                        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                            flow.checkAuthStatus()
                        }
                    }
            case .chooseLanguage:
                ChooseLanguageView(selectedLanguage: $flow.selectedLanguage) {
                    flow.handleLanguageSelection(flow.selectedLanguage)
                }
            case .login:
                LoginView(userRepository: userRepository) {
                    flow.handleLogin()
                }
            case .forgotPassword:
                ForgotPasswordView(userRepository: userRepository)
            case .main:
                MainAppView()
            }
        }
        .environmentObject(flow)
    }
} 