package com.example.readkeysqr;

import java.nio.ByteBuffer;

public class ReadKeySqr {
    static {
        System.loadLibrary("jni-read-keysqr");
    }

    public static native String HelloFromOpenCV();
    /**
     * 
     * 
     * byteBufferForGrayscaleChannel must be a DirectByteBuffer (not just any old ByteBuffer)
     */
    public static native String ReadKeySqrJson(int width, int height, int bytesPerRow, ByteBuffer byteBufferForGrayscaleChannel);
}
