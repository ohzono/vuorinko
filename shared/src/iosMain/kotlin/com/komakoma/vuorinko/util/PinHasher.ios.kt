package com.komakoma.vuorinko.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.set
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecRandomDefault

@OptIn(ExperimentalForeignApi::class)
actual object PinHasher {
    private const val SALT_BYTE_SIZE = 16

    actual fun hashPin(pin: String): String {
        val salt = generateSalt()
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

    private fun generateSalt(): ByteArray = memScoped {
        val saltPtr = allocArray<UByteVar>(SALT_BYTE_SIZE)
        val result = SecRandomCopyBytes(kSecRandomDefault, SALT_BYTE_SIZE.toULong(), saltPtr)
        if (result != errSecSuccess) {
            throw IllegalStateException("Failed to generate secure random bytes")
        }
        ByteArray(SALT_BYTE_SIZE) { i -> saltPtr[i].toByte() }
    }

    private fun sha256(input: String): String = memScoped {
        val inputBytes = input.encodeToByteArray()
        val inputPtr = allocArray<UByteVar>(inputBytes.size)
        inputBytes.forEachIndexed { index, byte ->
            inputPtr[index] = byte.toUByte()
        }
        val digestLength = CC_SHA256_DIGEST_LENGTH
        val digestPtr = allocArray<UByteVar>(digestLength)
        CC_SHA256(inputPtr, inputBytes.size.toUInt(), digestPtr)
        ByteArray(digestLength) { i -> digestPtr[i].toByte() }.toHexString()
    }

    private fun ByteArray.toHexString(): String =
        joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }
}
