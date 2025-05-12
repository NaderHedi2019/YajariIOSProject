import SwiftUI
import shared

struct ChooseLanguageView: View {
    @Environment(\._userRepository) private var userRepository
    @State private var selectedLanguage: Language?
    @State private var isLoading = false
    @State private var showError = false
    @State private var errorMessage = ""
    
    let languages: [Language] = [
        Language(id: "en", name: "English", code: "en"),
        Language(id: "es", name: "Español", code: "es"),
        Language(id: "fr", name: "Français", code: "fr"),
        Language(id: "de", name: "Deutsch", code: "de"),
        Language(id: "it", name: "Italiano", code: "it"),
        Language(id: "pt", name: "Português", code: "pt"),
        Language(id: "ru", name: "Русский", code: "ru"),
        Language(id: "zh", name: "中文", code: "zh"),
        Language(id: "ja", name: "日本語", code: "ja"),
        Language(id: "ko", name: "한국어", code: "ko")
    ]
    
    var body: some View {
        NavigationView {
            List {
                Section(header: Text("Select Language")) {
                    ForEach(languages, id: \.id) { language in
                        Button(action: { selectLanguage(language) }) {
                            HStack {
                                Text(language.name)
                                    .foregroundColor(.primary)
                                
                                Spacer()
                                
                                if selectedLanguage?.id == language.id {
                                    Image(systemName: "checkmark")
                                        .foregroundColor(.blue)
                                }
                            }
                        }
                    }
                }
                
                Section {
                    Button(action: saveLanguage) {
                        if isLoading {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle())
                        } else {
                            Text("Continue")
                                .frame(maxWidth: .infinity)
                        }
                    }
                    .disabled(selectedLanguage == nil || isLoading)
                }
            }
            .navigationTitle("Choose Language")
            .alert("Error", isPresented: $showError) {
                Button("OK", role: .cancel) { }
            } message: {
                Text(errorMessage)
            }
        }
    }
    
    private func selectLanguage(_ language: Language) {
        selectedLanguage = language
    }
    
    private func saveLanguage() {
        guard let language = selectedLanguage else { return }
        
        isLoading = true
        
        userRepository.setSelectedLanguage(language: language) { result in
            isLoading = false
            
            switch result {
            case .success:
                // Language saved successfully, app will automatically navigate to MainAppView
                break
            case .failure(let error):
                errorMessage = error.localizedDescription
                showError = true
            }
        }
    }
} 