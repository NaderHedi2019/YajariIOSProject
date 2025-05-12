import SwiftUI
import shared

struct EditProfileView: View {
    @Environment(\._userRepository) private var userRepository
    @Environment(\.presentationMode) private var presentationMode
    
    let user: User
    
    @State private var name: String
    @State private var email: String
    @State private var phone: String
    @State private var isLoading = false
    @State private var showError = false
    @State private var errorMessage = ""
    @State private var showSuccess = false
    
    init(user: User) {
        self.user = user
        _name = State(initialValue: user.name)
        _email = State(initialValue: user.email)
        _phone = State(initialValue: user.phone ?? "")
    }
    
    var body: some View {
        Form {
            Section(header: Text("Profile Information")) {
                TextField("Name", text: $name)
                    .textContentType(.name)
                
                TextField("Email", text: $email)
                    .textContentType(.emailAddress)
                    .keyboardType(.emailAddress)
                    .autocapitalization(.none)
                    .disableAutocorrection(true)
                
                TextField("Phone", text: $phone)
                    .textContentType(.telephoneNumber)
                    .keyboardType(.phonePad)
            }
            
            Section {
                Button(action: saveProfile) {
                    if isLoading {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle())
                    } else {
                        Text("Save Changes")
                    }
                }
                .disabled(isLoading || !isValidInput)
            }
        }
        .navigationTitle("Edit Profile")
        .alert("Error", isPresented: $showError) {
            Button("OK", role: .cancel) { }
        } message: {
            Text(errorMessage)
        }
        .alert("Success", isPresented: $showSuccess) {
            Button("OK") {
                presentationMode.wrappedValue.dismiss()
            }
        } message: {
            Text("Profile updated successfully")
        }
    }
    
    private var isValidInput: Bool {
        !name.isEmpty && !email.isEmpty && email.contains("@")
    }
    
    private func saveProfile() {
        isLoading = true
        
        let updatedUser = User(
            id: user.id,
            name: name,
            email: email,
            phone: phone.isEmpty ? nil : phone,
            avatarUrl: user.avatarUrl
        )
        
        userRepository.updateProfile(user: updatedUser) { result in
            isLoading = false
            
            switch result {
            case .success:
                showSuccess = true
            case .failure(let error):
                errorMessage = error.localizedDescription
                showError = true
            }
        }
    }
} 