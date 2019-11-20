package com.keysqr.readkeysqr

import android.util.Log
import java.nio.ByteBuffer

class ReadKeySqr {
    init {
        System.loadLibrary("jni-read-keysqr")
    }

    external fun HelloFromOpenCV(): String
    external fun ReadKeySqrJson(
            width: Int,
            height: Int,
            bytesPerRow: Int,
            byteBufferForGrayscaleChannel: ByteBuffer
    ): String

    private external fun createObject(): Long
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
    private external fun deleteObject(
            reader: Long
    )

    private var obj: Long = 0

    constructor() {
        obj = createObject()
    }

    fun ProcessImage(
            width: Int,
            height: Int,
            bytesPerRow: Int,
            byteBufferForGrayscaleChannel: ByteBuffer
    ): Boolean
    {
        return  processImage(obj, width, height, bytesPerRow, byteBufferForGrayscaleChannel)
    }

    fun RenderAugmentationOverlay(
            width: Int,
            height: Int,
            byteBufferForOverlay: ByteBuffer
    )
    {
        renderAugmentationOverlay(obj, width, height, byteBufferForOverlay)
    }

    fun finalize() {
        deleteObject(obj)
        obj = 0
    }
}
