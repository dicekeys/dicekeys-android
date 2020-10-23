package org.dicekeys.dicekey
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

    @JvmStatic
    fun fromHumanReadableForm(hrf: String): DiceKey<Face> {
      return when (hrf.length) {
        // Human readable form with orientations (letter + digit + orientation) x 25
        75 -> DiceKey(
          (0..24).map { k -> Face(hrf[k * 3], hrf[k * 3 + 1], hrf[k * 3 + 2]) }
        )
        // Human readable form without orientations (letter + digit) x 25
        50 -> DiceKey(
          (0..24).map { k -> Face(hrf[k * 2], hrf[k * 2 + 1], '?') }
        )
        else -> throw InvalidParameterException("Invalid length")
      }
    }
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

}

typealias SimpleDiceKey = DiceKey<Face>