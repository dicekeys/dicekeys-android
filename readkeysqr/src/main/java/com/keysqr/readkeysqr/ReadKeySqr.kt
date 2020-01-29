package com.keysqr.readkeysqr

import android.util.Log
import java.nio.ByteBuffer

//external fun jsonGlobalPublicKey(4
//        keySqrInHumanReadableForm: String,
//        keyDerivationOptionsJson: String
//): String

class ReadKeySqr {
    init {
        System.loadLibrary("jni-read-keysqr")
    }

    external fun HelloFromOpenCV(): String
//    external fun ReadKeySqrJson(
//            width: Int,
//            height: Int,
//            bytesPerRow: Int,
//            byteBufferForGrayscaleChannel: ByteBuffer
//    ): String

    private external fun newKeySqrImageReader(): Long
    private external fun processImage(
            reader: Long,
            width: Int,
            height: Int,
            bytesPerRow: Int,
            byteBufferForGrayscaleChannel: ByteBuffer
    ): Boolean
    private external fun renderAugmentationOverlay(
            reader: Long,
            width: Int,
            height: Int,
            byteBufferForOverlay: ByteBuffer
    )
    private external fun deleteKeySqrImageReader(
            reader: Long
    )
    private external fun jsonKeySqrRead(
            reader: Long
    ): String

    private var ptrToKeySqrImageReader: Long = 0

    constructor() {
        ptrToKeySqrImageReader = newKeySqrImageReader()
    }

    fun ProcessImage(
            width: Int,
            height: Int,
            bytesPerRow: Int,
            byteBufferForGrayscaleChannel: ByteBuffer
    ): Boolean
    {
        return  processImage(ptrToKeySqrImageReader, width, height, bytesPerRow, byteBufferForGrayscaleChannel)
    }

    fun JsonKeySqrRead(): String
    {
        return jsonKeySqrRead(ptrToKeySqrImageReader)
    }

    fun RenderAugmentationOverlay(
            width: Int,
            height: Int,
            byteBufferForOverlay: ByteBuffer
    )
    {
        renderAugmentationOverlay(ptrToKeySqrImageReader, width, height, byteBufferForOverlay)
    }

    fun finalize() {
        deleteKeySqrImageReader(ptrToKeySqrImageReader)
        ptrToKeySqrImageReader = 0
    }
}
