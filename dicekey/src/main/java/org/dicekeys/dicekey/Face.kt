package org.dicekeys.dicekey

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
open class Face(
    open val letter: Char,
    open val digit: Char,
    open val orientationAsLowercaseLetterTrbl: Char = '?'
) {
    val clockwise90DegreeRotationsFromUpright: Byte? get()  =
        FaceInternals.trblToClockwise90DegreeRotationsFromUpright(orientationAsLowercaseLetterTrbl)

    val orientationAsDegrees: Float
        get() = when(orientationAsLowercaseLetterTrbl) {
            't' -> 0f
            'r' -> 90f
            'b' -> 180f
            'l' -> 270f
            else -> 0f
        }

    val orientationAsFacingString: String
        get() = when(orientationAsLowercaseLetterTrbl) {
            't' -> "upright"
            'r' -> "right"
            'b' -> "down"
            'l' -> "left"
            else -> "unknown"
        }

    companion object {
        fun majorityOfThree(a: Char, b: Char, c: Char): Char {
            return when {
                (a == b || a == c) -> a
                (b == c) -> b
                else -> '?'
            }
        }
    }


    fun toHumanReadableForm(includeFaceOrientations: Boolean): String =
        String(
            if (includeFaceOrientations)
                charArrayOf(letter, digit, orientationAsLowercaseLetterTrbl)
            else
                charArrayOf(letter, digit)
        )

    fun rotate(clockwise90DegreeRotations: Int): Face {
        return Face(
                letter,
                digit,
                FaceInternals.clockwise90DegreeRotationsFromUprightToTrbl(
                        clockwise90DegreeRotationsFromUpright,
                        clockwise90DegreeRotations
                )
        )
    }

    val undoverlineCodes: FaceWithUnderlineAndOverlineCode?
        get() {
            val letterIndex = FaceLetters.indexOf(letter)
            if (letterIndex < 0)
                return null
            val digitIndex = FaceDigits.indexOf(digit)
            if (digitIndex < 0)
                return null
            return letterIndexTimesSixPlusDigitIndexFaceWithUndoverlineCodes[letterIndex * 6 + digitIndex]
        }

    open val underlineCode: Short?
        get() {
            return undoverlineCodes?.underlineCode
        }
    open val overlineCode: Short?
        get() {
            return undoverlineCodes?.overlineCode
        }

    open val underlineCode11Bits: UShort?
        get() {
            val value = undoverlineCodes?.underlineCode
            return if (value != null) {
                ((1 shl  10) or
                        // set the next high-order bit on overlines
                        0 or
                        // shift the face code 1 to the left to leave the 0th bit empty
                        (value.toUShort().toInt() shl 1)).toUShort()
            } else null
        }

    open val overlineCode11Bits: UShort?
        get() {
            val value = undoverlineCodes?.overlineCode
            return if (value != null) {
                ((1 shl  10) or
                        // set the next high-order bit on overlines
                        (1 shl 9) or
                        // shift the face code 1 to the left to leave the 0th bit empty
                        (value.toUShort().toInt() shl 1)).toUShort()
            } else null
        }

    fun numberOfFieldsDifferent(other: Face) : Int {
        var numberOfFields: Int = 0
        if (letter != other.letter) {
            numberOfFields += 1
        }
        if (digit != other.digit) {
            numberOfFields += 1
        }
        if (orientationAsLowercaseLetterTrbl != other.orientationAsLowercaseLetterTrbl) {
            numberOfFields += 1
        }
        return numberOfFields
    }

}



internal class FaceInternals {
    companion object {
        fun trblToClockwise90DegreeRotationsFromUpright(trbl: Char): Byte? {
            return when (trbl) {
                't' -> 0
                'r' -> 1
                'b' -> 2
                'l' -> 3
                else -> null
            }
        }

        fun clockwise90DegreeRotationsFromUprightToTrbl(
                clockwise90DegreeRotationsFromUpright: Byte?,
                additionalClockwise90DegreeRotations: Int = 0
        ): Char =
            if (clockwise90DegreeRotationsFromUpright == null)
                '?'
            else
                FaceRotationLetters[
                        (
                                clockwise90DegreeRotationsFromUpright +
                                        additionalClockwise90DegreeRotations
                                ) % 4
                ]

    }
}
