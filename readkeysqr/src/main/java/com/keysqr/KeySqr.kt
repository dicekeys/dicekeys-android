package com.keysqr
import android.graphics.Bitmap
import android.graphics.Color
import com.keysqr.readkeysqr.*
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.moshi.Types
import com.squareup.moshi.JsonClass


const val NumberOfFacesInKey = 25


val rotationIndexes = listOf<List<Byte>>(
  listOf<Byte>(
     0,  1,  2,  3,  4,
     5,  6,  7,  8,  9,
    10, 11, 12, 13, 14,
    15, 16, 17, 18, 19,
    20, 21, 22, 23, 24
   ),
   listOf<Byte>(
     20, 15, 10,  5,  0,
     21, 16, 11,  6,  1,
     22, 17, 12,  7,  2,
     23, 18, 13,  8,  3,
     24, 19, 14,  9,  4
   ),
   listOf<Byte>(
     24, 23, 22, 21, 20,
     19, 18, 17, 16, 15,
     14, 13, 12, 11, 10,
      9,  8,  7,  6,  5,
      4,  3,  2,  1,  0
   ),
   listOf<Byte>(
     4,  9, 14, 19, 24,
     3,  8, 13, 18, 23,
     2,  7, 12, 17, 22,
     1,  6, 11, 16, 21,
     0,  5, 10, 15, 20
   )
);

/**
 * Reduce the set of possible digits to 0..24 for precise index of 25 dice.
 */


class InvalidKeySqrException(message: String) : Exception(message) {};

class ClientNotAuthorizedException(message: String) : java.lang.Exception(message) {};
class InvalidJsonKeyDerivationOptionsException(message: String) : java.lang.Exception(message) {};
class InvalidKeyDerivationOptionValueException(message: String) : java.lang.Exception(message) {};
class UnknownKeySqrApiException(message: String) : java.lang.Exception(message) {};


interface Face<T: Face<T>> {
  val letter: Char  // 'A' - 'Z' except 'Q', or '?'
  val digit: Char   // '0' - '6', or '?'
  val clockwise90DegreeRotationsFromUpright: Byte? // 0 - 3

  fun rotate(clockwise90DegreeRotations: Int): T
  fun toHumanReadableForm(includeFaceOrientations: Boolean): String

  private val undoverlineCodes: FaceWithUnderlineAndOverlineCode?
    get() {
      val letterIndex = FaceLetters.indexOf(letter)
      if (letterIndex < 0)
        return null
      val digitIndex = FaceDigits.indexOf(digit)
      if (digitIndex < 0)
        return null
      return letterIndexTimesSixPlusDigitIndexFaceWithUndoverlineCodes[letterIndex * 6 + digitIndex]
    }

  val underlineCode: Short?
    get() {
      return undoverlineCodes?.underlineCode
    }
  val overlineCode: Short?
    get() {
      return undoverlineCodes?.overlineCode
    }
}

external fun keySqrGetSeed(
  keySqrInHumanReadableFormWithOrientations: String,
  jsonKeyDerivationOptions: String,
  clientsApplicationId: String
): ByteArray

external fun keySqrGetPublicKey(
        keySqrInHumanReadableFormWithOrientations: String,
        jsonKeyDerivationOptions: String,
        clientsApplicationId: String
): ByteArray

external fun keySqrGetPublicPrivateKeyPairPtr(
    keySqrInHumanReadableFormWithOrientations: String,
    jsonKeyDerivationOptions: String,
    clientsApplicationId: String
): Long

external fun keySqrDisposePublicPrivateKeyPairPtr(
    publicPrivateKeyPairPtr: Long
): Long

external fun keySqrPublicPrivateKeyPairGetPublicKey(
        publicPrivateKeyPairPtr: Long
): ByteArray


class PublicPrivateKeyPair(
    public val keySqrInHumanReadableFormWithOrientations: String,
    public val jsonKeyDerivationOptions: String,
    public val clientsApplicationId: String
) {
  var disposed: Boolean = false
  private val publicPrivateKeyPairPtr: Long = keySqrGetPublicPrivateKeyPairPtr(
    keySqrInHumanReadableFormWithOrientations,
    jsonKeyDerivationOptions,
    clientsApplicationId
  )

  fun getPublicKey(): PublicKey {
    return PublicKey(
      jsonKeyDerivationOptions,
      keySqrPublicPrivateKeyPairGetPublicKey(publicPrivateKeyPairPtr)
    )
  }

  fun decrypt() {
    //
  }

  fun erase() {
    if (publicPrivateKeyPairPtr != 0L && !disposed) {
      keySqrDisposePublicPrivateKeyPairPtr(publicPrivateKeyPairPtr)
      disposed = true
    }
  }


}

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

class KeySqr<F: Face<F>>(val faces: List<F>) {

  fun toHumanReadableForm(includeFaceOrientations: Boolean): String {
    return faces.joinToString(separator = "") {it.toHumanReadableForm((includeFaceOrientations))}
  }

  val allOrientationsAreDefined: Boolean get() = faces.all { it.clockwise90DegreeRotationsFromUpright != null }
  val allLettersAreDefined: Boolean get() = faces.all { it.letter != '?' }
  val allDigitsAreDefined: Boolean get() = faces.all { it.digit != '?' }
  val allLettersAndDigitsAreDefined: Boolean get() = allLettersAreDefined && allDigitsAreDefined
  val allLettersDigitsAndOrientationsAreDefined: Boolean get() = allLettersAndDigitsAreDefined && allOrientationsAreDefined

  fun rotate(clockwise90DegreeRotations: Int): KeySqr<F> {
    val clockwiseTurnsMod4 = clockwise90DegreeRotations % 4
    return if (clockwiseTurnsMod4 == 0)
        this@KeySqr
      else KeySqr(
            rotationIndexes[clockwiseTurnsMod4]
                    .map<Byte, F>() { faces[it.toInt()] }
                    .map<F, F>() { it.rotate(clockwiseTurnsMod4) }
    )
  }

  fun toCanonicalRotation(
    includeFaceOrientations: Boolean = allOrientationsAreDefined
  ): KeySqr<F> {
    var winningRotation: KeySqr<F> = this@KeySqr
    var winningReadableForm = toHumanReadableForm(includeFaceOrientations)
    for (clockwiseTurns in 1..3) {
      val rotatedKey = rotate(clockwiseTurns)
      val readableForm = rotatedKey.toHumanReadableForm(includeFaceOrientations)
      if (readableForm < winningReadableForm) {
        winningRotation = rotatedKey
        winningReadableForm = readableForm
      }
    }
    return winningRotation
  }

  fun getSeed(
    jsonKeyDerivationOptions: String,
    clientsApplicationId: String
  ): ByteArray {
    return keySqrGetSeed(
      toCanonicalRotation().toHumanReadableForm(true),
      jsonKeyDerivationOptions,
      clientsApplicationId
    )
  }

  fun getPublicKey(
    jsonKeyDerivationOptions: String,
    clientsApplicationId: String
  ): PublicKey {
    return PublicKey(
      jsonKeyDerivationOptions,
      keySqrGetPublicKey(
        toCanonicalRotation().toHumanReadableForm(true),
        jsonKeyDerivationOptions,
        clientsApplicationId
      )
    )
  }

  fun getPublicPrivateKeyPair(
          jsonKeyDerivationOptions: String,
          clientsApplicationId: String
  ): PublicPrivateKeyPair {
    return PublicPrivateKeyPair(
        toCanonicalRotation().toHumanReadableForm(true),
        jsonKeyDerivationOptions,
        clientsApplicationId
    )
  }
}

@JsonClass(generateAdapter = true)
class Point(
  val x: Float,
  val y: Float
);

@JsonClass(generateAdapter = true)
class Line(
        val start: Point,
        val end: Point
);

@JsonClass(generateAdapter = true)
class Undoverline(
        val line: Line,
        val code: Int
);

fun majorityOfThree(a: Char, b: Char, c: Char): Char {
  return when {
    (a == b || a == c) -> a
    (b == c) -> b
    else -> '?'
  }
}

fun trblToClockwise90DegreeRotationsFromUpright(trbl: String): Byte? {
  return when (trbl) {
    "t" -> 0
    "r" -> 1
    "b" -> 2
    "l" -> 3
    else -> null
  }
}

fun clockwise90DegreeRotationsFromUprightToTrbl(
        clockwise90DegreeRotationsFromUpright: Byte?,
        additionalClockwise90DegreeRotations: Int = 0
): String {
  return if (clockwise90DegreeRotationsFromUpright == null)
    "?"
  else
    "" + FaceRotationLetters[
      (
        clockwise90DegreeRotationsFromUpright +
        additionalClockwise90DegreeRotations
      ) % 4
    ]
}

@JsonClass(generateAdapter = true)
class FaceRead(
        val underline: Undoverline?,
        val overline: Undoverline?,
        val orientationAsLowercaseLetterTRBL: String,
        val ocrLetterCharsFromMostToLeastLikely: String,
        val ocrDigitCharsFromMostToLeastLikely: String,
        val center: Point
): Face<FaceRead> {

  override val clockwise90DegreeRotationsFromUpright: Byte? get() =
    trblToClockwise90DegreeRotationsFromUpright(orientationAsLowercaseLetterTRBL)

  val underlineLetter: Char
    get() {
      return if (underline == null)
        '?'
      else
        decodeUndoverlineByte(false, underline.code).letter
    }
  val underlineDigit: Char
    get() {
      return if (underline == null)
        '?'
      else
        decodeUndoverlineByte(false, underline.code).digit
    }
  val overlineLetter: Char
    get() {
      return if (overline == null)
        '?'
      else
        decodeUndoverlineByte(true, overline.code).letter
    }
  val overlineDigit: Char
    get() {
      return if (overline == null)
        '?'
      else
        decodeUndoverlineByte(true, overline.code).digit
    }

  override val letter: Char
    get() = majorityOfThree(
            underlineLetter, overlineLetter, ocrLetterCharsFromMostToLeastLikely[0])
  override val digit: Char
    get() = majorityOfThree(
            underlineDigit, overlineDigit, ocrDigitCharsFromMostToLeastLikely[0])
  override val underlineCode: Short?
    get() {
      return underline?.code?.toShort()
    }
  override val overlineCode: Short?
    get() {
      return overline?.code?.toShort()
    }

  override fun toHumanReadableForm(includeFaceOrientations: Boolean): String {
    return String(
            if (includeFaceOrientations)
              charArrayOf(letter, digit, orientationAsLowercaseLetterTRBL[0])
            else
              charArrayOf(letter, digit)
    )
  }

  override fun rotate(clockwise90DegreeRotations: Int): FaceRead {
   return FaceRead(
           underline,
           overline,
           clockwise90DegreeRotationsFromUprightToTrbl(
                   clockwise90DegreeRotationsFromUpright,
                   clockwise90DegreeRotations
           ),
           ocrLetterCharsFromMostToLeastLikely,
           ocrDigitCharsFromMostToLeastLikely,
           center
   )
  }
}


fun keySqrFromJsonFacesRead(json: String): KeySqr<FaceRead>? {
  try {
    val moshi = Moshi.Builder()
      .add(KotlinJsonAdapterFactory())
      .build()

    val faceReadJsonAdapter: JsonAdapter<List<FaceRead>> =
            moshi.adapter(Types.newParameterizedType(List::class.java, FaceRead::class.java))

    if (json == "null" || json[0] != '[')
      return null
    val faces = faceReadJsonAdapter.fromJson(json)
    return if (faces == null) null else KeySqr(faces)
  } catch (e: Exception) {
    return null
  }
}