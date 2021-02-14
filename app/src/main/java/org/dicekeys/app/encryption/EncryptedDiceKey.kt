package org.dicekeys.app.encryption

import android.graphics.drawable.GradientDrawable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.dicekeys.dicekey.Face

/*
 * EncryptedDiceKey
 *
 * An Encrypted representation of a DiceKey.
 * Available data are the KeyId and the center Face of the DiceKey.
 *
 */

@Serializable
data class EncryptedDiceKey(
        @SerialName("key_id")
        val keyId: String,
        @SerialName("center_face")
        val centerFace: String,
        @SerialName("encrypted_data")
        val encryptedData: EncryptedData,
) {

    val centerFaceAsFace: Face by lazy {
        Face.fromHumanReadableForm(centerFace)
    }

    override fun toString(): String = Json.encodeToString(this)
}