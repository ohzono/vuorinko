package com.komakoma.vuorinko.util

import java.security.MessageDigest
import java.security.SecureRandom

actual object PinHasher {
    private const val SALT_BYTE_SIZE = 16

    actual fun hashPin(pin: String): String {
        val salt = ByteArray(SALT_BYTE_SIZE).also { SecureRandom().nextBytes(it) }
        val saltHex = salt.toHexString()
        val hash = sha256(saltHex + pin)
        return "$saltHex:$hash"
    }

    actual fun verifyPin(pin: String, storedHash: String): Boolean {
        val parts = storedHash.split(":")
        if (parts.size != 2) return false
        val saltHex = parts[0]
        val expectedHash = parts[1]
        val actualHash = sha256(saltHex + pin)
        return actualHash == expectedHash
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(input.toByteArray(Charsets.UTF_8)).toHexString()
    }

    private fun ByteArray.toHexString(): String =
        joinToString("") { "%02x".format(it) }
}
