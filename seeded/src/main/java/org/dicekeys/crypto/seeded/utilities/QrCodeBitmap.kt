package org.dicekeys.crypto.seeded.utilities

import android.graphics.Bitmap
import android.graphics.Color
import  com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.BarcodeFormat.QR_CODE

// Version 40 QR code is (177×177)
internal val qrCodeNativeSizeInQrCodeSquarePixels = 177

internal fun QrCodeBitmap(
    text: String,
    maxEdgeLengthInDevicePixels: Int = qrCodeNativeSizeInQrCodeSquarePixels * 2
): Bitmap {
//        // There should be a positive integer number of device pixels for each QR code pixel
//        // (the little squares that make up a QR code)
//        val devicePixelsPerQrCodePixel = kotlin.math.max(
//            // There must be at least native pixel per QR code square
//            1,
//            // Use as many native pixels as possible, while ensuring a constant number of native
//            // pixels per pixel (square) in the QR code.
//            (maxEdgeLengthInDevicePixels / qrCodeNativeSizeInQrCodeSquarePixels)
//        )
//        val imageSizeInDevicePixels =
//            qrCodeNativeSizeInQrCodeSquarePixels *devicePixelsPerQrCodePixel

    val qrWriter = QRCodeWriter()
    val bitMatrix = qrWriter.encode(
            text,
            QR_CODE,
            maxEdgeLengthInDevicePixels,
            maxEdgeLengthInDevicePixels
    )
    val bmp: Bitmap = Bitmap.createBitmap(
            maxEdgeLengthInDevicePixels, maxEdgeLengthInDevicePixels, Bitmap.Config.RGB_565
    )
    for (x in 0 until maxEdgeLengthInDevicePixels) {
        for (y in 0 until maxEdgeLengthInDevicePixels) {
            bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
        }
    }
    return bmp
}

internal fun QrCodeBitmap(
    urlBase: String,
    json: String,
    maxEdgeLengthInDevicePixels: Int = qrCodeNativeSizeInQrCodeSquarePixels * 2
): Bitmap {
    val urlEncodedJson = java.net.URLEncoder.encode(
            json, java.nio.charset.StandardCharsets.UTF_8.toString())
    val publicKeyAsUri: String = "$urlBase$urlEncodedJson"
    return QrCodeBitmap(publicKeyAsUri, maxEdgeLengthInDevicePixels);
}