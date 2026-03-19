import Foundation
import CryptoKit
import Security

final class PinManager {
    static let shared = PinManager()

    private let pinHashKey = "com.komakoma.vuorinko.pinHash"
    private let failedAttemptsKey = "com.komakoma.vuorinko.failedAttempts"
    private let lastFailedTimeKey = "com.komakoma.vuorinko.lastFailedTime"

    private init() {}

    var isPinSet: Bool {
        getKeychainValue(pinHashKey) != nil
    }

    func setPin(_ pin: String) {
        let hash = hashPin(pin)
        setKeychainValue(hash, forKey: pinHashKey)
        resetFailedAttempts()
    }

    func verifyPin(_ pin: String) -> Bool {
        guard let stored = getKeychainValue(pinHashKey) else { return false }
        let parts = stored.split(separator: ":")
        guard parts.count == 2, let salt = parts.first else { return false }
        let candidate = "\(salt)\(pin)"
        let hash = SHA256.hash(data: Data(candidate.utf8)).map { String(format: "%02x", $0) }.joined()
        return hash == String(parts[1])
    }

    var failedAttempts: Int {
        get { UserDefaults.standard.integer(forKey: failedAttemptsKey) }
        set { UserDefaults.standard.set(newValue, forKey: failedAttemptsKey) }
    }

    var lastFailedTime: Date? {
        get { UserDefaults.standard.object(forKey: lastFailedTimeKey) as? Date }
        set { UserDefaults.standard.set(newValue, forKey: lastFailedTimeKey) }
    }

    func incrementFailedAttempts() {
        failedAttempts += 1
        lastFailedTime = Date()
    }

    func resetFailedAttempts() {
        failedAttempts = 0
        lastFailedTime = nil
    }

    func lockoutRemainingSeconds() -> Int {
        guard failedAttempts >= AppConfig.maxPinAttempts,
              let lastFailed = lastFailedTime else { return 0 }
        let elapsed = Int(Date().timeIntervalSince(lastFailed))
        let remaining = AppConfig.lockoutDurationSeconds - elapsed
        if remaining <= 0 {
            resetFailedAttempts()
            return 0
        }
        return remaining
    }

    func resetAllData() {
        deleteKeychainValue(pinHashKey)
        resetFailedAttempts()
    }

    // MARK: - Hashing

    private func hashPin(_ pin: String) -> String {
        var saltBytes = [UInt8](repeating: 0, count: 16)
        _ = SecRandomCopyBytes(kSecRandomDefault, saltBytes.count, &saltBytes)
        let salt = saltBytes.map { String(format: "%02x", $0) }.joined()
        let input = "\(salt)\(pin)"
        let hash = SHA256.hash(data: Data(input.utf8)).map { String(format: "%02x", $0) }.joined()
        return "\(salt):\(hash)"
    }

    // MARK: - Keychain

    private func setKeychainValue(_ value: String, forKey key: String) {
        deleteKeychainValue(key)
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key,
            kSecValueData as String: Data(value.utf8),
            kSecAttrAccessible as String: kSecAttrAccessibleWhenUnlockedThisDeviceOnly,
        ]
        SecItemAdd(query as CFDictionary, nil)
    }

    private func getKeychainValue(_ key: String) -> String? {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key,
            kSecReturnData as String: true,
            kSecMatchLimit as String: kSecMatchLimitOne,
        ]
        var result: AnyObject?
        let status = SecItemCopyMatching(query as CFDictionary, &result)
        guard status == errSecSuccess, let data = result as? Data else { return nil }
        return String(data: data, encoding: .utf8)
    }

    private func deleteKeychainValue(_ key: String) {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key,
        ]
        SecItemDelete(query as CFDictionary)
    }
}
