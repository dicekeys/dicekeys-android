package com.example.readkeysqr

import java.nio.ByteBuffer

public object ReadKeySqr {
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
}
