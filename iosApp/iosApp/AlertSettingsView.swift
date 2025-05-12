import SwiftUI
import shared

struct AlertSettingsView: View {
    @Environment(\._userRepository) private var userRepository
    @State private var settings: AlertSettings?
    @State private var isLoading = false
    @State private var showError = false
    @State private var errorMessage = ""
    @State private var showSuccess = false
    
    var body: some View {
        Group {
            if isLoading {
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle())
            } else if let settings = settings {
                Form {
                    Section(header: Text("Notifications")) {
                        Toggle("Email Notifications", isOn: Binding(
                            get: { settings.emailNotifications },
                            set: { newValue in
                                updateSettings(settings.withEmailNotifications(enabled: newValue))
                            }
                        ))
                        
                        Toggle("Push Notifications", isOn: Binding(
                            get: { settings.pushNotifications },
                            set: { newValue in
                                updateSettings(settings.withPushNotifications(enabled: newValue))
                            }
                        ))
                    }
                    
                    Section(header: Text("Alert Types")) {
                        Toggle("New Messages", isOn: Binding(
                            get: { settings.newMessageAlerts },
                            set: { newValue in
                                updateSettings(settings.withNewMessageAlerts(enabled: newValue))
                            }
                        ))
                        
                        Toggle("New Announcements", isOn: Binding(
                            get: { settings.newAnnouncementAlerts },
                            set: { newValue in
                                updateSettings(settings.withNewAnnouncementAlerts(enabled: newValue))
                            }
                        ))
                        
                        Toggle("Donation Updates", isOn: Binding(
                            get: { settings.donationAlerts },
                            set: { newValue in
                                updateSettings(settings.withDonationAlerts(enabled: newValue))
                            }
                        ))
                    }
                }
            }
        }
        .navigationTitle("Alert Settings")
        .alert("Error", isPresented: $showError) {
            Button("OK", role: .cancel) { }
        } message: {
            Text(errorMessage)
        }
        .alert("Success", isPresented: $showSuccess) {
            Button("OK", role: .cancel) { }
        } message: {
            Text("Settings updated successfully")
        }
        .onAppear {
            loadSettings()
        }
    }
    
    private func loadSettings() {
        isLoading = true
        
        userRepository.getAlertSettings { result in
            isLoading = false
            
            switch result {
            case .success(let settings):
                self.settings = settings
            case .failure(let error):
                errorMessage = error.localizedDescription
                showError = true
            }
        }
    }
    
    private func updateSettings(_ newSettings: AlertSettings) {
        isLoading = true
        
        userRepository.updateAlertSettings(settings: newSettings) { result in
            isLoading = false
            
            switch result {
            case .success:
                self.settings = newSettings
                showSuccess = true
            case .failure(let error):
                errorMessage = error.localizedDescription
                showError = true
            }
        }
    }
} 