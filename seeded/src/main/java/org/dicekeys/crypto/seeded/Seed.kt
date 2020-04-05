package org.dicekeys.crypto.seeded

class Seed private constructor(internal val nativeObjectPtr: Long) {

    companion object {
        init {
            ensureJniLoaded()
        }

        @JvmStatic external fun constructJNI(
                seedBytes: ByteArray,
                keyDerivationOptionsJson: String
        ) : Long

        @JvmStatic external fun constructJNI(
                seedString: String,
                keyDerivationOptionsJson: String
        ) : Long

        @JvmStatic external fun constructFromJsonJNI(
                seedJson: String
        ) : Long

    }

    private external fun deleteNativeObjectPtrJNI()
    private external fun seedBytesGetterJNI(): ByteArray
    private external fun keyDerivationOptionsJsonGetterJNI(): String
    external fun toJson(): String

    val seedBytes: ByteArray get() = seedBytesGetterJNI()
    val keyDerivationOptionsJson: String get() = keyDerivationOptionsJsonGetterJNI()

    constructor(
            seedBytes: ByteArray,
            keyDerivationOptionsJson: String
    ) : this( constructJNI(seedBytes, keyDerivationOptionsJson) )

    // Create copy constructor to prevent copying of the native pointer, which would lead
    // to a use-after-dereference pointer vulnerability
    constructor(
            other: Seed
    ) : this(other.seedBytes, other.keyDerivationOptionsJson)

    constructor(
            seedString: String,
            keyDerivationOptionsJson: String
    ) : this(constructJNI(seedString, keyDerivationOptionsJson))

    constructor(
            seedJson: String
    ) : this(constructFromJsonJNI(seedJson))

    protected fun finalize() {
        deleteNativeObjectPtrJNI()
    }

}
