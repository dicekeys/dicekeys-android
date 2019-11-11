package com.keysqr.readkeysqr

import	android.media.Image;
import java.nio.ByteBuffer;


/**
 * Reference to the C++ readKeySqrJson native function
 */
external fun readKeySquareJson(
        width: Int,
        height: Int,
        bytesPerRow: Int,
        grayscalePlaneBuffer: ByteBuffer
): String

/**
 * Reference to the C++ readKeySqrJson native function
 */

fun readKeySqrFromImageJson(image: Image): String {
    if (image == null) {
        return "null";
    }
    val width: Int = image.width;
    val height: Int = image.height;
    val grayscalePlane: Image.Plane = image.getPlanes()[0];
    val bytesPerRow = grayscalePlane.getRowStride();
    val grayscalePlaneBuffer: ByteBuffer = grayscalePlane.getBuffer();
    return readKeySquare(width, height, bytesPerRow, grayscalePlaneBuffer);
}