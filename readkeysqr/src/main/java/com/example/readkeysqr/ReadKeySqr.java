package com.example.readkeysqr;

import java.nio.ByteBuffer;

public class ReadKeySqr {
    static {
        System.loadLibrary("lib-read-keysqr");
    }

    public static native String HelloFromOpenCV();
    public static native String ReadKeySqrJson(int width, int height, int bytesPerRow, ByteBuffer byteBufferForGrayscaleChannel);
}
