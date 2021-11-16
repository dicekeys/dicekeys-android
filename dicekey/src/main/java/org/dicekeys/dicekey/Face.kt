package org.dicekeys.dicekey

import kotlinx.serialization.Serializable


/**
 * Thrown when human readable form is invalid
 */
class InvalidHumanReadableFormException(message: String) : java.lang.Exception(message)

@Serializable
open class Face constructor(
    open val letter: Char,
    open val digit: Char,
    open val orientationAsLowercaseLetterTrbl: Char = '?'
) {
    val isBlank by lazy { letter.isWhitespace() && digit.isWhitespace() }
    val isNotBlank by lazy { !letter.isWhitespace() && !digit.isWhitespace() }

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

        fun isValidFaceLetter(candidateFaceLetter: Char): Boolean {
            return "ABCDEFGHIJKLMNOPRSTUVWXYZ".indexOf(candidateFaceLetter) != -1
        }
        fun isValidFaceDigit(candidateFaceDigit: Char): Boolean {
            return "123456".indexOf(candidateFaceDigit) != -1
        }
        fun isValidOrientationAsLowercaseLetterTrbl(candidateOrientation: Char): Boolean {
            return "trbl".indexOf(candidateOrientation) != -1
        }
        @JvmStatic
        fun fromHumanReadableForm(humanReadableForm: String): Face {
            if (humanReadableForm.length != 3) {
                throw InvalidHumanReadableFormException("Invalid length: a face stored in human readable form must be 3 characters long")
            }
            val letter: Char = humanReadableForm[0]
            if (!isValidFaceLetter(letter)) {
                throw InvalidHumanReadableFormException("Invalid letter: $letter")
            }
            val digit = humanReadableForm[1]
            if (!isValidFaceDigit(digit)) {
                throw InvalidHumanReadableFormException("Invalid digit: $digit")
            }
            val orientationAsLowercaseLetterTrbl = humanReadableForm[2]
            if (!isValidOrientationAsLowercaseLetterTrbl(orientationAsLowercaseLetterTrbl)) {
                throw InvalidHumanReadableFormException("Invalid orientation must be 't', 'r', 'b', or 'l': $orientationAsLowercaseLetterTrbl")
            }
            return Face(letter, digit, orientationAsLowercaseLetterTrbl)
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
