package org.dicekeys.dicekey

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class Point(
        val x: Float,
        val y: Float
)

@Serializable
data class Line(
        val start: Point,
        val end: Point
)

@Serializable
data class Undoverline(
        val line: Line,
        val code: Int
)

@Serializable
data class FaceReadSerializable(
    val underline: Undoverline?,
    val overline: Undoverline?,
    val orientationAsLowercaseLetterTrbl: Char,
    val ocrLetterCharsFromMostToLeastLikely: String,
    val ocrDigitCharsFromMostToLeastLikely: String,
    val center: Point
){
    fun toFaceRead(): FaceRead =
        FaceRead(
            underline = underline,
            overline = overline,
            orientationAsLowercaseLetterTrbl = orientationAsLowercaseLetterTrbl,
            ocrLetterCharsFromMostToLeastLikely = ocrLetterCharsFromMostToLeastLikely,
            ocrDigitCharsFromMostToLeastLikely = ocrDigitCharsFromMostToLeastLikely,
            center = center
        )
}

// https://youtrack.jetbrains.com/issue/KT-38958
class FaceRead constructor(
        val underline: Undoverline?,
        val overline: Undoverline?,
        override val orientationAsLowercaseLetterTrbl: Char,
        val ocrLetterCharsFromMostToLeastLikely: String,
        val ocrDigitCharsFromMostToLeastLikely: String,
        val center: Point
): Face(
        majorityOfThree(
                underline?.let { decodeUndoverlineByte(false, it.code).letter }
                        ?: '?',
                overline?.let { decodeUndoverlineByte(true, it.code).letter }
                        ?: '?',
                ocrLetterCharsFromMostToLeastLikely[0]),
        majorityOfThree(
                underline?.let { decodeUndoverlineByte(false, it.code).digit }
                        ?: '?',
                overline?.let { decodeUndoverlineByte(true, it.code).digit }
                        ?: '?',
                ocrDigitCharsFromMostToLeastLikely[0]),
        orientationAsLowercaseLetterTrbl
)
 {

    companion object {
        val JsonDeserializer = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

        fun diceKeyFromListOfFacesRead(facesRead: List<FaceRead>): DiceKey<FaceRead> {
            return DiceKey(facesRead)
        }

        fun diceKeyFromJsonFacesRead(json: String): DiceKey<FaceRead>? {
            try {

                if (json == "null" || json[0] != '[')
                    return null
                val faces = JsonDeserializer.decodeFromString<List<FaceReadSerializable>?>(json)?.map {
                    it.toFaceRead()
                }
                return if (faces == null) null else diceKeyFromListOfFacesRead(faces)
            } catch (e: Exception) {
                e.printStackTrace()
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

    override val underlineCode: Short?
        get() {
            return underline?.code?.toShort()
        }
    override val overlineCode: Short?
        get() {
            return overline?.code?.toShort()
        }
 }
