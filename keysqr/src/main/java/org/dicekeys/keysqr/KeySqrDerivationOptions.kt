package org.dicekeys.keysqr

import org.dicekeys.crypto.seeded.*

/**
 * An extension of the more general [KeyDerivationOptions] class, which supports the
 * constructing and parsing the strings used to derive cryptographic keys from the
 * a KeySqr (the 2D representation of a DiceKey)
 *
 * This extension adds support for the [excludeOrientationOfFaces] option, which can create
 * seeds that remain the same even if the orientation of a die within a DiceKey changes.
 */
open class KeySqrDerivationOptions constructor(
        keyDerivationOptionsJson: String? = null,
        requiredKeyType: KeyType? = null
): KeyDerivationOptions(
        keyDerivationOptionsJson, requiredKeyType
) {
    /**
     * When using a DiceKey as a seed, setting this value to true will exclude the orientation
     * of each face from the key, so that the seed is unchanged even if orientations are misread.
     * This reduces the lieklihood that, if a user copies their DiceKey manually and does not verify
     * it, an error in copying orientation would prevent them from re-generating their key.
     * It also reduces the security of the key.
     *
     * For a key of 25 dice, it reduces the entropy by 50 (2x25) bits, from ~196 bits to ~146 bits.
     */
    var excludeOrientationOfFaces: Boolean
        get () = optBoolean(KeySqrDerivationOptions::excludeOrientationOfFaces.name, false)
        set(value) { put(KeySqrDerivationOptions::excludeOrientationOfFaces.name, value) }


    /**
     * An extension class that must represent a specification for a public/private key pair
     */
    class Public(keyDerivationOptionsJson: String? = null) :
            KeySqrDerivationOptions(keyDerivationOptionsJson,  KeyType.Public)

    /**
     * An extension class that must represent a specification for a derived seed
     */
    class Seed(keyDerivationOptionsJson: String? = null) :
            KeySqrDerivationOptions(keyDerivationOptionsJson,  KeyType.Seed)

    /**
     * An extension class that must represent a specification for a signing/verification key pair
     */
    class Signing(keyDerivationOptionsJson: String? = null) :
            KeySqrDerivationOptions(keyDerivationOptionsJson,  KeyType.Signing)

    /**
     * An extension class that must represent a specification for a symmetric key
     */
    class Symmetric(keyDerivationOptionsJson: String? = null) :
            KeySqrDerivationOptions(keyDerivationOptionsJson,  KeyType.Symmetric)

}
