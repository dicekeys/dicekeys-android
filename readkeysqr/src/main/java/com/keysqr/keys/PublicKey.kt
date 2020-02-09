package com.keysqr.keys

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Base64
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

internal
class Base64Adapter {
    @FromJson
    fun fromJson(s: String): ByteArray {
        return Base64.decode(s, Base64.NO_WRAP)
    }

    @ToJson
    fun toJson(byteArray: ByteArray): String
    {
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}

@JsonClass(generateAdapter = true)
class PublicKey(
    val jsonKeyDerivationOptions: String,
    val asByteArray: ByteArray
) {

    companion object {
        internal val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .add(Base64Adapter())
                .build()
        val jsonAdapter: JsonAdapter<PublicKey> =
            moshi.adapter<PublicKey>(PublicKey::class.java)
            .indent("")
        fun fromJson(json: String): PublicKey? {
            return jsonAdapter.fromJson(json)
        }
    }


    fun toJson(): String { return jsonAdapter.toJson(this) }
    // public val asJson: String get() = jsonAdapter.toJson(this)

    @ExperimentalUnsignedTypes
    public val asHexDigits: String get() =
        asByteArray.asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }

    private external fun sealJNI(
        publicKeyBytes: ByteArray,
        jsonKeyDerivationOptions: String,
        plaintext: ByteArray,
        postDecryptionInstructionJson: String = ""
    ): ByteArray

    public fun seal(
        message: ByteArray,
        postDecryptionInstructionJson: String = ""
    ): ByteArray {
        return sealJNI(asByteArray, jsonKeyDerivationOptions, message, postDecryptionInstructionJson)
    }

    // Version 40 QR code is (177×177)
    val qrCodeNativeSizeInQrCodeSquarePixels = 177

    fun getJsonQrCode(
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

        val qrWriter = com.google.zxing.qrcode.QRCodeWriter()
        val json = toJson()
        val urlEncodedJson = java.net.URLEncoder.encode(
                json, java.nio.charset.StandardCharsets.UTF_8.toString())
        val publicKeyAsUri: String = "https://dicekeys.org/pk/$urlEncodedJson"
        val bitMatrix = qrWriter.encode(
                publicKeyAsUri, // FIXME
                com.google.zxing.BarcodeFormat.QR_CODE,
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


    fun getJsonQrCode(
            maxWidth: Int,
            maxHeight: Int
    ): Bitmap {
        return getJsonQrCode(kotlin.math.min(maxWidth, maxHeight))
    }

}
