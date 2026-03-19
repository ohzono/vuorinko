import SwiftUI
import SwiftData

@main
struct VuorinkoApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
        .modelContainer(for: [Album.self, PhotoRef.self])
    }
}
