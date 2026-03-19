package com.komakoma.vuorinko.util

expect object PinHasher {
    fun hashPin(pin: String): String
    fun verifyPin(pin: String, storedHash: String): Boolean
}
