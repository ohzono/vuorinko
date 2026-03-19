import SwiftUI

struct WelcomeView: View {
    let onNext: () -> Void

    var body: some View {
        VStack(spacing: 48) {
            Spacer()
            VStack(spacing: 16) {
                Text("Vuorinko")
                    .font(.system(size: 40, weight: .bold))
                Text(String(localized: "welcome_subtitle"))
                    .font(.body)
                    .multilineTextAlignment(.center)
                    .foregroundStyle(.secondary)
            }
            Spacer()
            Button(action: onNext) {
                Text(String(localized: "welcome_start"))
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .controlSize(.large)
        }
        .padding(32)
    }
}
