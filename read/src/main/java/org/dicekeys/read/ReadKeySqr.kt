package org.dicekeys.read

import java.nio.ByteBuffer


class ReadKeySqr() {
    companion object {
        init {
            System.loadLibrary("jni-read-keysqr")
        }
    }
    private var nativeObjectPtr: Long = constructJNI()

    private external fun constructJNI(): Long
    private external fun destructJNI()

    fun finalize() = destructJNI()

    external fun processImage(
            width: Int,
            height: Int,
            bytesPerRow: Int,
            byteBufferForGrayscaleChannel: ByteBuffer
    ): Boolean

    external fun renderAugmentationOverlay(
            width: Int,
            height: Int,
            byteBufferForOverlay: ByteBuffer
    )

    external fun jsonKeySqrRead(): String

}
