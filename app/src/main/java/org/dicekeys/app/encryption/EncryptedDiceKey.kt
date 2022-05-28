package org.dicekeys.app.encryption

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNames
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
    @JsonNames("key_id")
    val keyId: String,
    @JsonNames("center_face")
    val centerFace: String,
    @JsonNames("encrypted_data")
    val encryptedData: EncryptedData,
    // Set Biometric for backward compatibility by default
    @JsonNames("keystore_type")
    val keyStoreType: AppKeyStore.KeyStoreCredentialsAllowed = AppKeyStore.KeyStoreCredentialsAllowed.ALLOW_ONLY_BIOMETRIC_AUTHENTICATION,
) {

    val centerFaceAsFace: Face by lazy {
        Face.fromHumanReadableForm(centerFace)
    }

    override fun toString(): String = Json.encodeToString(this)
}