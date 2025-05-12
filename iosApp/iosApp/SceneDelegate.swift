import UIKit
import SwiftUI
import shared

class SceneDelegate: UIResponder, UIWindowSceneDelegate {
    var window: UIWindow?

    func scene(_ scene: UIScene, willConnectTo session: UISceneSession, options connectionOptions: UIScene.ConnectionOptions) {
        guard let windowScene = (scene as? UIWindowScene) else { return }
        window = UIWindow(windowScene: windowScene)
        let userRepository = UserRepositoryImpl(apiService: ApiServiceFactory().createApiService())
        let announcementRepository = AnnouncementRepositoryImpl(apiService: ApiServiceFactory().createApiService())
        let mainAppView = MainAppView()
            .environment(\._userRepository, userRepository)
            .environment(\._announcementRepository, announcementRepository)
        window?.rootViewController = UIHostingController(rootView: mainAppView)
        window?.makeKeyAndVisible()
    }
} 