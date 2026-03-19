import SwiftUI
import LocalAuthentication

struct PinInputView: View {
    let onAuthenticated: () -> Void

    @State private var pin = ""
    @State private var error: String?
    @State private var isLockedOut = false
    @State private var lockoutSeconds = 0

    private let pinManager = PinManager.shared

    var body: some View {
        VStack(spacing: 32) {
            Text("PINを入力")
                .font(.title)

            PinDotsView(length: pin.count, total: AppConfig.pinLength)

            if isLockedOut {
                Text("ロック中...\(lockoutSeconds)秒後に再試行")
                    .foregroundStyle(.red)
            } else if let error {
                Text(error)
                    .foregroundStyle(.red)
            }

            NumberPadView(
                onDigit: appendDigit,
                onDelete: deleteDigit,
                enabled: !isLockedOut
            )
        }
        .padding(32)
        .onAppear { attemptBiometric() }
    }

    private func appendDigit(_ digit: Int) {
        guard pin.count < AppConfig.pinLength else { return }
        pin += "\(digit)"
        error = nil
        if pin.count == AppConfig.pinLength {
            verifyPin()
        }
    }

    private func deleteDigit() {
        guard !pin.isEmpty else { return }
        pin.removeLast()
        error = nil
    }

    private func verifyPin() {
        let lockout = pinManager.lockoutRemainingSeconds()
        if lockout > 0 {
            startLockout(lockout)
            return
        }

        if pinManager.verifyPin(pin) {
            pinManager.resetFailedAttempts()
            onAuthenticated()
        } else {
            pinManager.incrementFailedAttempts()
            let remaining = AppConfig.maxPinAttempts - pinManager.failedAttempts
            if remaining <= 0 {
                startLockout(AppConfig.lockoutDurationSeconds)
            } else {
                pin = ""
                error = "PINが違います（残り\(remaining)回）"
            }
        }
    }

    private func startLockout(_ seconds: Int) {
        pin = ""
        isLockedOut = true
        lockoutSeconds = seconds
        error = nil
        Task {
            for i in stride(from: seconds, through: 1, by: -1) {
                lockoutSeconds = i
                try? await Task.sleep(for: .seconds(1))
            }
            isLockedOut = false
            lockoutSeconds = 0
            pinManager.resetFailedAttempts()
        }
    }

    private func attemptBiometric() {
        let context = LAContext()
        var biometricError: NSError?
        guard context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &biometricError) else { return }

        context.evaluatePolicy(
            .deviceOwnerAuthenticationWithBiometrics,
            localizedReason: "親モードに戻ります"
        ) { success, _ in
            DispatchQueue.main.async {
                if success { onAuthenticated() }
            }
        }
    }
}
