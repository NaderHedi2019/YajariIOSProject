import SwiftUI
import shared
import PhotosUI

struct AnnouncementSubmissionView: View {
    @Environment(\._announcementRepository) private var announcementRepository
    @Environment(\.presentationMode) private var presentationMode
    
    @State private var title: String = ""
    @State private var description: String = ""
    @State private var location: String = ""
    @State private var selectedImage: UIImage?
    @State private var isImagePickerPresented = false
    @State private var isLoading = false
    @State private var showError = false
    @State private var errorMessage = ""
    @State private var showSuccess = false
    
    var body: some View {
        Form {
            Section(header: Text("Announcement Details")) {
                TextField("Title", text: $title)
                    .textContentType(.none)
                
                TextEditor(text: $description)
                    .frame(minHeight: 100)
                
                TextField("Location", text: $location)
                    .textContentType(.fullStreetAddress)
            }
            
            Section(header: Text("Image")) {
                if let image = selectedImage {
                    Image(uiImage: image)
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(maxHeight: 200)
                        .cornerRadius(8)
                }
                
                Button(action: { isImagePickerPresented = true }) {
                    HStack {
                        Image(systemName: selectedImage == nil ? "photo" : "photo.fill")
                        Text(selectedImage == nil ? "Add Image" : "Change Image")
                    }
                }
            }
            
            Section {
                Button(action: submitAnnouncement) {
                    if isLoading {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle())
                    } else {
                        Text("Submit Announcement")
                    }
                }
                .disabled(isLoading || !isValidInput)
            }
        }
        .navigationTitle("New Announcement")
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
            Text("Announcement submitted successfully")
        }
        .sheet(isPresented: $isImagePickerPresented) {
            ImagePicker(image: $selectedImage)
        }
    }
    
    private var isValidInput: Bool {
        !title.isEmpty && !description.isEmpty && !location.isEmpty
    }
    
    private func submitAnnouncement() {
        isLoading = true
        
        // First upload image if selected
        if let image = selectedImage {
            uploadImage(image) { result in
                switch result {
                case .success(let imageUrl):
                    createAnnouncement(imageUrl: imageUrl)
                case .failure(let error):
                    handleError(error)
                }
            }
        } else {
            createAnnouncement(imageUrl: nil)
        }
    }
    
    private func uploadImage(_ image: UIImage, completion: @escaping (Result<String, Error>) -> Void) {
        // Convert UIImage to Data
        guard let imageData = image.jpegData(compressionQuality: 0.8) else {
            completion(.failure(NSError(domain: "", code: -1, userInfo: [NSLocalizedDescriptionKey: "Failed to process image"])))
            return
        }
        
        // Upload image using repository
        announcementRepository.uploadImage(imageData: imageData) { result in
            switch result {
            case .success(let imageUrl):
                completion(.success(imageUrl))
            case .failure(let error):
                completion(.failure(error))
            }
        }
    }
    
    private func createAnnouncement(imageUrl: String?) {
        let announcement = Announcement(
            id: UUID().uuidString,
            title: title,
            description: description,
            location: location,
            imageUrl: imageUrl,
            createdAt: Date().timeIntervalSince1970,
            isActive: true
        )
        
        announcementRepository.createAnnouncement(announcement: announcement) { result in
            isLoading = false
            
            switch result {
            case .success:
                showSuccess = true
            case .failure(let error):
                handleError(error)
            }
        }
    }
    
    private func handleError(_ error: Error) {
        isLoading = false
        errorMessage = error.localizedDescription
        showError = true
    }
}

struct ImagePicker: UIViewControllerRepresentable {
    @Binding var image: UIImage?
    @Environment(\.presentationMode) private var presentationMode
    
    func makeUIViewController(context: Context) -> PHPickerViewController {
        var config = PHPickerConfiguration()
        config.filter = .images
        config.selectionLimit = 1
        
        let picker = PHPickerViewController(configuration: config)
        picker.delegate = context.coordinator
        return picker
    }
    
    func updateUIViewController(_ uiViewController: PHPickerViewController, context: Context) {}
    
    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }
    
    class Coordinator: NSObject, PHPickerViewControllerDelegate {
        let parent: ImagePicker
        
        init(_ parent: ImagePicker) {
            self.parent = parent
        }
        
        func picker(_ picker: PHPickerViewController, didFinishPicking results: [PHPickerResult]) {
            parent.presentationMode.wrappedValue.dismiss()
            
            guard let provider = results.first?.itemProvider else { return }
            
            if provider.canLoadObject(ofClass: UIImage.self) {
                provider.loadObject(ofClass: UIImage.self) { image, _ in
                    DispatchQueue.main.async {
                        self.parent.image = image as? UIImage
                    }
                }
            }
        }
    }
} 