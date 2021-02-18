package org.dicekeys.dicekey
import android.util.Base64
import org.dicekeys.crypto.seeded.Secret
import java.security.InvalidParameterException

open class DiceKey<F: Face>(val faces: List<F>) {
  companion object {
    const val NumberOfFacesInKey = 25
    val rotationIndexes = listOf<List<Byte>>(
      listOf<Byte>(
        0, 1, 2, 3, 4,
        5, 6, 7, 8, 9,
        10, 11, 12, 13, 14,
        15, 16, 17, 18, 19,
        20, 21, 22, 23, 24
      ),
      listOf<Byte>(
        20, 15, 10, 5, 0,
        21, 16, 11, 6, 1,
        22, 17, 12, 7, 2,
        23, 18, 13, 8, 3,
        24, 19, 14, 9, 4
      ),
      listOf<Byte>(
        24, 23, 22, 21, 20,
        19, 18, 17, 16, 15,
        14, 13, 12, 11, 10,
        9, 8, 7, 6, 5,
        4, 3, 2, 1, 0
      ),
      listOf<Byte>(
        4, 9, 14, 19, 24,
        3, 8, 13, 18, 23,
        2, 7, 12, 17, 22,
        1, 6, 11, 16, 21,
        0, 5, 10, 15, 20
      )
    )

    val example: DiceKey<Face>
      get() = DiceKey(faces = (0 until 25).map { index ->
        Face(FaceLetters[index], FaceDigits[index % 6], orientationAsLowercaseLetterTrbl = FaceRotationLetters[index % 4])
      })

    @JvmStatic
    fun fromHumanReadableForm(hrf: String): DiceKey<Face> {
      return when (hrf.length) {
        // Human readable form with orientations (letter + digit + orientation) x 25
        75 -> DiceKey(
          (0..24).map { k -> Face.fromHumanReadableForm(hrf.substring(k * 3, k * 3 + 3)) }
        )
        // Human readable form without orientations (letter + digit) x 25
        50 -> DiceKey(
              (0..24).map { k -> Face.fromHumanReadableForm(hrf.substring(k * 2, k * 3 + 2) + "t") }
        )
        else -> throw InvalidParameterException("Invalid length")
      }
    }

    fun createFromRandom(): DiceKey<Face> = DiceKey(faces = (0 until 25).map { Face(
            letter = FaceLetters.random(),
            digit = FaceDigits.random(),
            orientationAsLowercaseLetterTrbl = FaceRotationLetters.random()
        )
    })

    fun clone(diceKey: DiceKey<Face>): DiceKey<Face> {
       return fromHumanReadableForm(diceKey.toHumanReadableForm())
    }

    fun toDiceKey(diceKey: DiceKey<FaceRead>) : DiceKey<Face> = DiceKey(faces = diceKey.faces.map {
      Face(letter = it.letter, digit = it.digit, orientationAsLowercaseLetterTrbl = it.orientationAsLowercaseLetterTrbl)
    })
  }

  fun toHumanReadableForm(): String {
    return faces.joinToString(separator = "") {it.toHumanReadableForm(allOrientationsAreDefined)}
  }

  val allOrientationsAreDefined: Boolean get() = faces.all { it.clockwise90DegreeRotationsFromUpright != null }
  val allLettersAreDefined: Boolean get() = faces.all { it.letter != '?' }
  val allDigitsAreDefined: Boolean get() = faces.all { it.digit != '?' }
  val allLettersAndDigitsAreDefined: Boolean get() = allLettersAreDefined && allDigitsAreDefined
  val allLettersDigitsAndOrientationsAreDefined: Boolean get() = allLettersAndDigitsAreDefined && allOrientationsAreDefined

  fun rotate(clockwise90DegreeRotations: Int): DiceKey<Face> =
    (clockwise90DegreeRotations % 4).let { clockwiseTurnsMod4 ->
      DiceKey(
        rotationIndexes[clockwiseTurnsMod4]
          .map<Byte, F>() { faces[it.toInt()] }
          .map<F, Face>() { it.rotate(clockwiseTurnsMod4) }
      )
    }

  fun removeOrientations() : DiceKey<Face> = DiceKey(
    faces.map{ face -> Face(face.letter, face.digit) }
  )

  fun toCanonicalRotation(): DiceKey<Face> {
    var winningRotation: DiceKey<Face> = rotate(0)
    var winningReadableForm = toHumanReadableForm()
    for (clockwiseTurns in 1..3) {
      val rotatedKey = rotate(clockwiseTurns)
      val readableForm = rotatedKey.toHumanReadableForm()
      if (readableForm < winningReadableForm) {
        winningRotation = rotatedKey
        winningReadableForm = readableForm
      }
    }
    return winningRotation
  }

  fun toKeySeed(
    excludeOrientationOfFaces : Boolean
  ) : String =
    (if (excludeOrientationOfFaces) removeOrientations() else this)
      .toCanonicalRotation()
      .toHumanReadableForm()

  fun threeAlternativeRotations() : List<DiceKey<Face>> =
          mutableListOf(
            rotate(1),
            rotate(2),
            rotate(3)
          )

  fun differencesForFixedRotation(other: DiceKey<Face>) : Int {
    var difference = 0
    for (index in 0..24) {
      difference += faces[index].numberOfFieldsDifferent(other.faces[index])
    }
    return difference
  }

  fun mostSimilarRotationWithDifference(other: DiceKey<Face>, maxDifferenceToRotateFor: Int = 12) : Pair<DiceKey<Face>, Int> {
    var rotationWithSmallestDifference = other
    var smallestDifference = differencesForFixedRotation(other)
    if (smallestDifference == 0)
      return Pair(rotationWithSmallestDifference, smallestDifference)
    for (candidate in threeAlternativeRotations()) {
      val difference = differencesForFixedRotation(candidate)
      if (difference < smallestDifference && difference <= maxDifferenceToRotateFor) {
        smallestDifference = difference
        rotationWithSmallestDifference = candidate
      }
      if (smallestDifference == 0) {
        // no need to look further
        return Pair(rotationWithSmallestDifference, smallestDifference)
      }
    }
    return Pair(rotationWithSmallestDifference, smallestDifference)
  }

  fun mostSimilarRotationOf(other: DiceKey<Face>, maxDifferenceToRotateFor: Int = 12) : DiceKey<Face> {
    val (rotationWithSmallestDifference, _) = mostSimilarRotationWithDifference(other, maxDifferenceToRotateFor)
    return rotationWithSmallestDifference
  }

  fun centerFace(): Face {
    return faces[12]
  }

  private val recipeFor16ByteUniqueIdentifier = "{\"purpose\":\"a unique identifier for this DiceKey\",\"lengthInBytes\":16}"

  val seed: String get () = toKeySeed(false)
  val keyIdBytes: ByteArray get () = Secret.deriveFromSeed(seed, recipeFor16ByteUniqueIdentifier).secretBytes
  val keyId: String
      get() = Base64.encodeToString(keyIdBytes, Base64.URL_SAFE or Base64.NO_WRAP)

}

typealias SimpleDiceKey = DiceKey<Face>
