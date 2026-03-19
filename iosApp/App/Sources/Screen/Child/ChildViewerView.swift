import SwiftUI
import SwiftData

struct ChildViewerView: View {
    let albumId: String
    let onExit: () -> Void

    @Environment(\.modelContext) private var modelContext
    @State private var showPinDialog = false
    @State private var tapCount = 0
    @State private var currentPage = 0

    private var album: Album? {
        let descriptor = FetchDescriptor<Album>(predicate: #Predicate { $0.id == albumId })
        return try? modelContext.fetch(descriptor).first
    }

    private var photos: [PhotoRef] {
        (album?.photos ?? []).sorted { $0.sortOrder < $1.sortOrder }
    }

    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()

            if photos.isEmpty {
                Text("写真がありません")
                    .foregroundStyle(.white)
            } else {
                TabView(selection: $currentPage) {
                    ForEach(Array(photos.enumerated()), id: \.element.id) { index, photo in
                        // Photo placeholder - PHAsset loading would go here
                        Rectangle()
                            .fill(Color.gray.opacity(0.3))
                            .overlay {
                                Image(systemName: "photo")
                                    .font(.system(size: 60))
                                    .foregroundStyle(.white.opacity(0.5))
                            }
                            .tag(index)
                    }
                }
                .tabViewStyle(.page(indexDisplayMode: .automatic))
            }
        }
        .ignoresSafeArea()
        .statusBarHidden()
        .persistentSystemOverlays(.hidden)
        .onTapGesture(count: 1) { location in
            // Triple tap on top area to show PIN
            if location.y < 100 {
                tapCount += 1
                if tapCount >= 3 {
                    showPinDialog = true
                    tapCount = 0
                }
            } else {
                tapCount = 0
            }
        }
        .sheet(isPresented: $showPinDialog) {
            ExitPinSheet(onAuthenticated: {
                showPinDialog = false
                onExit()
            })
        }
        .prefersHomeIndicatorAutoHidden(true)
    }
}

private struct ExitPinSheet: View {
    let onAuthenticated: () -> Void
    @Environment(\.dismiss) private var dismiss

    @State private var pin = ""
    @State private var error: String?
    private let pinManager = PinManager.shared

    var body: some View {
        NavigationStack {
            VStack(spacing: 24) {
                Text("親モードに戻る")
                    .font(.title2)

                PinDotsView(length: pin.count, total: AppConfig.pinLength)

                if let error {
                    Text(error).foregroundStyle(.red)
                }

                NumberPadView(
                    onDigit: { digit in
                        guard pin.count < AppConfig.pinLength else { return }
                        pin += "\(digit)"
                        error = nil
                        if pin.count == AppConfig.pinLength {
                            if pinManager.verifyPin(pin) {
                                pinManager.resetFailedAttempts()
                                onAuthenticated()
                            } else {
                                pinManager.incrementFailedAttempts()
                                let remaining = AppConfig.maxPinAttempts - pinManager.failedAttempts
                                pin = ""
                                error = remaining <= 0
                                    ? "ロック中（\(AppConfig.lockoutDurationSeconds)秒）"
                                    : "PINが違います（残り\(remaining)回）"
                            }
                        }
                    },
                    onDelete: {
                        guard !pin.isEmpty else { return }
                        pin.removeLast()
                        error = nil
                    },
                    enabled: true
                )
            }
            .padding()
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("キャンセル") { dismiss() }
                }
            }
        }
    }
}

// MARK: - Home Indicator Auto Hidden

private struct HomeIndicatorHiddenModifier: ViewModifier {
    let hidden: Bool
    func body(content: Content) -> some View {
        content
    }
}

extension View {
    func prefersHomeIndicatorAutoHidden(_ hidden: Bool) -> some View {
        modifier(HomeIndicatorHiddenModifier(hidden: hidden))
    }
}
