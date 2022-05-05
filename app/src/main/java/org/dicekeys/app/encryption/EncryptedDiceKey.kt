package org.dicekeys.app.encryption

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
data class EncryptedDiceKey constructor(
    @SerialName("key_id")
    val keyId: String,
    @SerialName("center_face")
    val centerFace: String,
    @SerialName("encrypted_data")
    val encryptedData: EncryptedData,
    // Set Biometric for backward compatibility by default
    @SerialName("keystore_type")
    val keyStoreType: AppKeyStore.KeyStoreCredentialsAllowed = AppKeyStore.KeyStoreCredentialsAllowed.ALLOW_ONLY_BIOMETRIC_AUTHENTICATION,
) {

    val centerFaceAsFace: Face by lazy {
        Face.fromHumanReadableForm(centerFace)
    }

    override fun toString(): String = Json.encodeToString(this)
}