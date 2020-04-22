package org.dicekeys.crypto.seeded

private var loaded = false;
internal fun ensureJniLoaded() {
    if (!loaded) {
        loaded = true
        System.loadLibrary("jni-seeded-crypto")
    }
}

