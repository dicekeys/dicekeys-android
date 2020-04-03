package org.dicekeys
import org.dicekeys.api.SignatureVerificationKey
import org.dicekeys.faces.Face
import org.dicekeys.keys.*


class KeySqr<F: Face>(val faces: List<F>) {
  companion object {
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
    )
  }

  external fun getSeedJNI(
    keySqrInHumanReadableFormWithOrientations: String,
    keyDerivationOptionsJson: String,
    clientsApplicationId: String
  ): ByteArray

  fun toHumanReadableForm(includeFaceOrientations: Boolean): String {
    return faces.joinToString(separator = "") {it.toHumanReadableForm((includeFaceOrientations))}
  }

  val allOrientationsAreDefined: Boolean get() = faces.all { it.clockwise90DegreeRotationsFromUpright != null }
  val allLettersAreDefined: Boolean get() = faces.all { it.letter != '?' }
  val allDigitsAreDefined: Boolean get() = faces.all { it.digit != '?' }
  val allLettersAndDigitsAreDefined: Boolean get() = allLettersAreDefined && allDigitsAreDefined
  val allLettersDigitsAndOrientationsAreDefined: Boolean get() = allLettersAndDigitsAreDefined && allOrientationsAreDefined

  fun rotate(clockwise90DegreeRotations: Int): KeySqr<Face> {
    val clockwiseTurnsMod4 = clockwise90DegreeRotations % 4
    return KeySqr(
            rotationIndexes[clockwiseTurnsMod4]
                    .map<Byte, F>() { faces[it.toInt()] }
                    .map<F, Face>() { it.rotate(clockwiseTurnsMod4) }
    )
  }

  fun toCanonicalRotation(
    includeFaceOrientations: Boolean = allOrientationsAreDefined
  ): KeySqr<Face> {
    var winningRotation: KeySqr<Face> = rotate(0)
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
    keyDerivationOptionsJson: String? = "",
    clientsApplicationId: String = ""
  ): ByteArray {
    return getSeedJNI(
      toCanonicalRotation().toHumanReadableForm(true),
      keyDerivationOptionsJson ?: "",
      clientsApplicationId
    )
  }

  fun getSymmetricKey(
          keyDerivationOptionsJson: String? = "",
          clientsApplicationId: String = ""
  ): SymmetricKey {
    return SymmetricKey(
            toCanonicalRotation().toHumanReadableForm(true),
            keyDerivationOptionsJson ?: "",
            clientsApplicationId
    )
  }

  fun getPublicKey(
    keyDerivationOptionsJson: String? = "",
    clientsApplicationId: String = ""
  ): org.dicekeys.api.PublicKey {
    return PublicPrivateKeyPair(
        toCanonicalRotation().toHumanReadableForm(true),
        keyDerivationOptionsJson ?: "",
        clientsApplicationId
      ).getPublicKey()
  }

  fun getPublicPrivateKeyPair(
          keyDerivationOptionsJson: String?= "",
          clientsApplicationId: String = ""
  ): PublicPrivateKeyPair {
    return PublicPrivateKeyPair(
        toCanonicalRotation().toHumanReadableForm(true),
        keyDerivationOptionsJson ?: "",
        clientsApplicationId
    )
  }

  fun getSigningKey(
          keyDerivationOptionsJson: String? = "",
          clientsApplicationId: String = ""
  ): SigningKey {
    return SigningKey(
            toCanonicalRotation().toHumanReadableForm(true),
            keyDerivationOptionsJson ?: "",
            clientsApplicationId
    )
  }

  fun getSignatureVerificationKey(
          keyDerivationOptionsJson: String? = "",
          clientsApplicationId: String = ""
  ): SignatureVerificationKey {
    return SigningKey(
            toCanonicalRotation().toHumanReadableForm(true),
            keyDerivationOptionsJson ?: "",
            clientsApplicationId
    ).getSignatureVerificationKey()
  }


}
