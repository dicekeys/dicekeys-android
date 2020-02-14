package com.keysqr

interface Face<T: Face<T>> {
    val letter: Char  // 'A' - 'Z' except 'Q', or '?'
    val digit: Char   // '0' - '6', or '?'
    val clockwise90DegreeRotationsFromUpright: Byte? // 0 - 3

    val orientationAsLowercaseLetterTRBL: Char

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
        ): Char {
            return if (clockwise90DegreeRotationsFromUpright == null)
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
}
