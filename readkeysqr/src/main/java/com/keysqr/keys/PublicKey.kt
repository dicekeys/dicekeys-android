package com.keysqr.keys

import android.graphics.Bitmap
import android.graphics.Color

class PublicKey(
        public val jsonKeyDerivationOptions: String,
        public val asByteArray: ByteArray
) {
    @ExperimentalUnsignedTypes
    public val asHexDigits: String get() =
        asByteArray.asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }
    public val asJson: String get() =
        """{
      |  "jsonKeyDerivationOptions": "${
        jsonKeyDerivationOptions
                // Escape quotes and backslashes
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
        }",
      |  "asHexDigits": ${asHexDigits}
      |}""".trimMargin()

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

    public fun getJsonQrCode(
            width: Int = 640,
            height: Int = width
    ): Bitmap {
        val qrWriter = com.google.zxing.qrcode.QRCodeWriter()
        val bitMatrix = qrWriter.encode(
                "dicekeys-public-key:${asJson}",
                com.google.zxing.BarcodeFormat.QR_CODE,
                width,
                height
        )
        val bmp: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }
}
