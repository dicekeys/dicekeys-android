package org.dicekeys.faces
import org.dicekeys.KeySqr

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
open class Face(
    open val letter: Char,
    open val digit: Char,
    open val orientationAsLowercaseLetterTRBL: Char
) {
    val clockwise90DegreeRotationsFromUpright: Byte? get()  =
        FaceInternals.trblToClockwise90DegreeRotationsFromUpright(orientationAsLowercaseLetterTRBL)

    companion object {
        fun keySqrFromHumanReadableForm(hrf: String): KeySqr<Face>? {
            try {
                if (hrf.length == 75) {
                    // Human readable form with orientations (letter + digit + orientation) x 25
                    return KeySqr(
                            (0..24).map { k -> Face(hrf[k * 3], hrf[k * 3 + 1], hrf[k * 3 + 2]) }
                    )
                } else if (hrf.length == 50) {
                    // Human readable form without orientations (letter + digit) x 25
                    return KeySqr(
                            (0..24).map { k -> Face(hrf[k * 2], hrf[k * 2 + 1], '?') }
                    )
                }
                return null
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


    fun toHumanReadableForm(includeFaceOrientations: Boolean): String {
        return String(
            if (includeFaceOrientations)
                charArrayOf(letter, digit, orientationAsLowercaseLetterTRBL)
            else
                charArrayOf(letter, digit)
        )
    }

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
