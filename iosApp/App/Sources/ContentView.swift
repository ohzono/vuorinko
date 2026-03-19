import SwiftUI
import shared

struct ContentView: View {
    @State private var appState = AppState()

    var body: some View {
        Group {
            switch appState.currentScreen {
            case .welcome:
                WelcomeView(onNext: { appState.currentScreen = .pinSetup })
            case .pinSetup:
                PinSetupView(onPinSet: { appState.currentScreen = .guidedAccessGuide })
            case .guidedAccessGuide:
                GuidedAccessGuideView(
                    onNext: { appState.navigateToAlbumList() },
                    onSkip: { appState.navigateToAlbumList() }
                )
            case .pinInput:
                PinInputView(onAuthenticated: { appState.navigateToAlbumList() })
            case .albumList:
                AlbumListView(
                    onAlbumTap: { albumId in appState.navigateToPhotoManage(albumId: albumId) },
                    onChildMode: { albumId in appState.navigateToChildViewer(albumId: albumId) }
                )
            case .photoManage(let albumId):
                PhotoManageView(albumId: albumId, onBack: { appState.navigateBack() })
            case .childViewer(let albumId):
                ChildViewerView(albumId: albumId, onExit: { appState.navigateBack() })
            }
        }
    }
}

#Preview {
    ContentView()
}
