package org.dicekeys.read

import java.nio.ByteBuffer


class ReadKeySqr {
    init {
        System.loadLibrary("jni-read-keysqr")
    }

    //
    // JNI support functions
    //
    private external fun newKeySqrImageReaderJNI(): Long
    private external fun processImageJNI(
            reader: Long,
            width: Int,
            height: Int,
            bytesPerRow: Int,
            byteBufferForGrayscaleChannel: ByteBuffer
    ): Boolean
    private external fun renderAugmentationOverlayJNI(
            reader: Long,
            width: Int,
            height: Int,
            byteBufferForOverlay: ByteBuffer
    )
    private external fun deleteKeySqrImageReaderJNI(
            reader: Long
    )
    private external fun jsonKeySqrReadJNI(
            reader: Long
    ): String

    private var ptrToKeySqrImageReader: Long = newKeySqrImageReaderJNI()

    fun processImage(
            width: Int,
            height: Int,
            bytesPerRow: Int,
            byteBufferForGrayscaleChannel: ByteBuffer
    ): Boolean  {
        return  processImageJNI(ptrToKeySqrImageReader, width, height, bytesPerRow, byteBufferForGrayscaleChannel)
    }

    fun jsonKeySqrRead(): String
    {
        return jsonKeySqrReadJNI(ptrToKeySqrImageReader)
    }

    fun renderAugmentationOverlay(
            width: Int,
            height: Int,
            byteBufferForOverlay: ByteBuffer
    )
    {
        renderAugmentationOverlayJNI(ptrToKeySqrImageReader, width, height, byteBufferForOverlay)
    }

    fun finalize() {
       deleteKeySqrImageReaderJNI(ptrToKeySqrImageReader)
    }
}
