import SwiftUI

@Observable
final class AppState {
    enum Screen {
        case pinSetup
        case pinInput
        case albumList
        case photoManage(albumId: String)
        case childViewer(albumId: String)
    }

    var currentScreen: Screen

    init() {
        currentScreen = PinManager.shared.isPinSet ? .pinInput : .pinSetup
    }

    func navigateToAlbumList() {
        currentScreen = .albumList
    }

    func navigateToPhotoManage(albumId: String) {
        currentScreen = .photoManage(albumId: albumId)
    }

    func navigateToChildViewer(albumId: String) {
        currentScreen = .childViewer(albumId: albumId)
    }

    func navigateBack() {
        currentScreen = .albumList
    }
}
