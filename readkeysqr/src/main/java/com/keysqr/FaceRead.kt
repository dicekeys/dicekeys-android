package com.keysqr

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@JsonClass(generateAdapter = true)
class Point(
        val x: Float,
        val y: Float
)

@JsonClass(generateAdapter = true)
class Line(
        val start: Point,
        val end: Point
)

@JsonClass(generateAdapter = true)
class Undoverline(
        val line: Line,
        val code: Int
)

@JsonClass(generateAdapter = true)
class FaceRead(
        val underline: Undoverline?,
        val overline: Undoverline?,
        val orientationAsLowercaseLetterTRBL: String,
        val ocrLetterCharsFromMostToLeastLikely: String,
        val ocrDigitCharsFromMostToLeastLikely: String,
        val center: Point
): Face<FaceRead> {

    companion object {
        fun keySqrFromListOfFacesRead(facesRead: List<FaceRead>): KeySqr<FaceRead> {
            return KeySqr(facesRead)
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
                return if (faces == null) null else keySqrFromListOfFacesRead(faces)
            } catch (e: Exception) {
                return null
            }
        }


        fun majorityOfThree(a: Char, b: Char, c: Char): Char {
            return when {
                (a == b || a == c) -> a
                (b == c) -> b
                else -> '?'
            }
        }
    }

    override val clockwise90DegreeRotationsFromUpright: Byte? get() =
        FaceInternals.trblToClockwise90DegreeRotationsFromUpright(orientationAsLowercaseLetterTRBL)

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
                FaceInternals.clockwise90DegreeRotationsFromUprightToTrbl(
                        clockwise90DegreeRotationsFromUpright,
                        clockwise90DegreeRotations
                ),
                ocrLetterCharsFromMostToLeastLikely,
                ocrDigitCharsFromMostToLeastLikely,
                center
        )
    }
}
