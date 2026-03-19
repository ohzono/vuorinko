import SwiftUI

struct PinSetupView: View {
    let onPinSet: () -> Void

    @State private var step = 0 // 0: first, 1: confirm
    @State private var firstPin = ""
    @State private var currentPin = ""
    @State private var error: String?

    var body: some View {
        VStack(spacing: 32) {
            Text(step == 0 ? "PINを設定してください" : "もう一度入力してください")
                .font(.title)

            PinDotsView(length: currentPin.count, total: AppConfig.pinLength)

            if let error {
                Text(error)
                    .foregroundStyle(.red)
            }

            NumberPadView(
                onDigit: { digit in
                    guard currentPin.count < AppConfig.pinLength else { return }
                    currentPin += "\(digit)"
                    error = nil
                    if currentPin.count == AppConfig.pinLength {
                        if step == 0 {
                            firstPin = currentPin
                            currentPin = ""
                            step = 1
                        } else {
                            if currentPin == firstPin {
                                PinManager.shared.setPin(currentPin)
                                onPinSet()
                            } else {
                                error = "PINが一致しません。やり直してください"
                                currentPin = ""
                                firstPin = ""
                                step = 0
                            }
                        }
                    }
                },
                onDelete: {
                    guard !currentPin.isEmpty else { return }
                    currentPin.removeLast()
                    error = nil
                },
                enabled: true
            )
        }
        .padding(32)
    }
}
