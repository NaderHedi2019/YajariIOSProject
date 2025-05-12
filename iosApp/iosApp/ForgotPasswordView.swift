import SwiftUI
import shared

struct ForgotPasswordView: View {
    @State private var email: String = ""
    @State private var showAlert: Bool = false
    @State private var alertMessage: String = ""
    let userRepository: UserRepository

    var body: some View {
        VStack(spacing: 20) {
            TextField("Email", text: $email)
                .textFieldStyle(RoundedBorderTextFieldStyle())
                .autocapitalization(.none)
                .keyboardType(.emailAddress)

            Button("Reset Password") {
                resetPassword()
            }
            .padding()
            .background(Color.blue)
            .foregroundColor(.white)
            .cornerRadius(8)
        }
        .padding()
        .alert(isPresented: $showAlert) {
            Alert(title: Text("Reset Password"), message: Text(alertMessage), dismissButton: .default(Text("OK")))
        }
    }

    private func resetPassword() {
        // Example: Call userRepository.resetPassword(email: email) { result in ... }
        alertMessage = "Password reset link sent to \(email)"
        showAlert = true
    }
} 