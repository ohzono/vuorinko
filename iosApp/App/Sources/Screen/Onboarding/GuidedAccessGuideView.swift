import SwiftUI

struct GuidedAccessGuideView: View {
    let onNext: () -> Void
    let onSkip: () -> Void

    var body: some View {
        VStack(spacing: 24) {
            Spacer()
            Text(String(localized: "guided_access_title"))
                .font(.title)
            Text(String(localized: "guided_access_desc"))
                .font(.body)
                .multilineTextAlignment(.center)
                .foregroundStyle(.secondary)

            VStack(alignment: .leading, spacing: 8) {
                Text(String(localized: "guided_access_steps"))
                    .font(.callout)
            }
            .padding()
            .background(Color(uiColor: .systemGray6))
            .clipShape(RoundedRectangle(cornerRadius: 12))

            Spacer()

            VStack(spacing: 8) {
                Button(action: onNext) {
                    Text(String(localized: "common_next"))
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.borderedProminent)
                .controlSize(.large)

                Button(action: onSkip) {
                    Text(String(localized: "common_skip"))
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.bordered)
                .controlSize(.large)
            }
        }
        .padding(32)
    }
}
