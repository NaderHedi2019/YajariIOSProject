import SwiftUI
import shared

private struct UserRepositoryKey: EnvironmentKey {
    static let defaultValue: UserRepository = UserRepositoryImpl(apiService: ApiServiceFactory().createApiService())
}

private struct AnnouncementRepositoryKey: EnvironmentKey {
    static let defaultValue: AnnouncementRepository = AnnouncementRepositoryImpl(apiService: ApiServiceFactory().createApiService())
}

extension EnvironmentValues {
    var _userRepository: UserRepository {
        get { self[UserRepositoryKey.self] }
        set { self[UserRepositoryKey.self] = newValue }
    }
    var _announcementRepository: AnnouncementRepository {
        get { self[AnnouncementRepositoryKey.self] }
        set { self[AnnouncementRepositoryKey.self] = newValue }
    }
} 