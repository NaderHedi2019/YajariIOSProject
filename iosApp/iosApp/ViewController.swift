import UIKit
import shared

class ViewController: UIViewController {
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .white
        // Example: Use shared module here
        let userRepository = UserRepositoryImpl(apiService: ApiServiceFactory().createApiService())
        // Use userRepository as needed
    }
} 