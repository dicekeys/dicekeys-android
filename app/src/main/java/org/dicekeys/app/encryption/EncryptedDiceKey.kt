package org.dicekeys.app.encryption

import android.util.Base64
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class EncryptedDiceKey(
        @SerialName("id")
        val id: String,
        @SerialName("center_face")
        val centerFace: String,
        @SerialName("encrypted_data")
        val encryptedData: EncryptedData
) {
    override fun toString(): String = Json.encodeToString(this)
}