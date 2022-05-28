package org.dicekeys.app.encryption

import android.util.Base64
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNames

/*
 * EncryptedData
 *
 * A Serializable class representing encrypted payload and the IV used to encrypt it.
 * For space reasons data are Base64 encoded
 */

@Serializable
data class EncryptedData(
    @JsonNames("encrypted_data")
    private val encryptedData: String,
    private val iv: String
) {

    fun getEncryptedData(): ByteArray = Base64.decode(encryptedData, Base64.NO_WRAP)

    fun getIv(): ByteArray = Base64.decode(iv, Base64.NO_WRAP)

    override fun toString(): String = Json.encodeToString(this)

    companion object {
        fun fromJson(json: String): EncryptedData = Json.decodeFromString(json)

        fun fromByteArray(encryptedData: ByteArray, iv: ByteArray) = EncryptedData(
            Base64.encodeToString(encryptedData, Base64.NO_WRAP),
            Base64.encodeToString(iv, Base64.NO_WRAP)
        )
    }
}