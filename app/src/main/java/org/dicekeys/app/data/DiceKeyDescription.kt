package org.dicekeys.app.data

import org.dicekeys.app.encryption.EncryptedDiceKey
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face

/*
 * DiceKeyDescription
 *
 * A class to keep basic data of a DiceKey for use when rendering in obsucred from with only
 * the center face exposed.
 * The class may describe an EncryptedDiceKey or a DiceKey in the DiceKeyRepository.
 *
 * @param centerFace is a three letter string of the DiceKey's center face in human-readble form.
 * It must contain three characters even though the orientation will always be rendered as upright
 * and is thus ignored.
 */
data class DiceKeyDescription constructor(val keyId: String, val centerFace: String) {
    constructor(diceKey: DiceKey<*>) : this(
        diceKey.keyId,
        diceKey.centerFace().toHumanReadableForm(true)
    )

    constructor(encryptedDiceKey: EncryptedDiceKey) : this(encryptedDiceKey.keyId, encryptedDiceKey.centerFace)

    val centerFaceAsFace: Face by lazy {
        Face.fromHumanReadableForm(centerFace)
    }
}
