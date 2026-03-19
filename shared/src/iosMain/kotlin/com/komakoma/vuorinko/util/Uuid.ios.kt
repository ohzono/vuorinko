package com.komakoma.vuorinko.util

import platform.Foundation.NSUUID

internal actual fun generateUuid(): String = NSUUID().UUIDString()
