package org.dicekeys.api

import android.app.Activity
import android.app.IntentService
import android.content.Intent
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.parcel.Parcelize
import org.dicekeys.KeyDerivationOptions
import java.lang.Exception

object Actions {
    object Seed {
        const val get = "org.keysqr.api.actions.Seed.get"
    }
    object SymmetricKey {
        const val seal = "org.keysqr.api.actions.SymmetricKey.seal"
        const val unseal = "org.keysqr.api.actions.SymmetricKey.unseal"
    }
    object PublicPrivateKeyPair {
        const val getPublic = "org.keysqr.api.actions.PublicPrivateKeyPair.getPublic"
        const val unseal = "org.keysqr.api.actions.PublicPrivateKeyPair.unseal"
    }

    val All = setOf(
        Seed.get,
        SymmetricKey.seal,
        SymmetricKey.unseal,
        PublicPrivateKeyPair.getPublic,
        PublicPrivateKeyPair.unseal
    )
}


@Parcelize
class ActionSeedGet(
    val jsonKeyDerivationOptions: String
): Parcelable

@Parcelize
class ActionSymmetricKeySeal(
    val jsonKeyDerivationOptions: String,
    val plaintext: ByteArray
): Parcelable

@Parcelize
class ActionSymmetricKeyUnseal(
    val jsonKeyDerivationOptions: String,
    val ciphertext: ByteArray
): Parcelable

@Parcelize
class ActionGetPublicKey(
        val jsonKeyDerivationOptions: String,
        val plaintext: ByteArray
): Parcelable

@Parcelize
class ActionPubliczPrivateKeyUnseal(
        val jsonKeyDerivationOptions: String,
        val ciphertext: ByteArray
): Parcelable


class ClientNotAuthorizeException(
        clientApplicationId: String?,
        authorizedPrefixes: List<String>
): Exception("Client $clientApplicationId is not authorized to generate key as it does not start with one of the following prefixes: ${
    authorizedPrefixes.joinToString(",", "'", "'" )
}")

class ApiActivity : AppCompatActivity() {

    private val intentReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context, intent: Intent) {
            val action = intent.action

            if (!org.dicekeys.api.Actions.All.contains((action))) {
                // This is not an action handled by this API
                return
            }

            // All calls to the API require key derivation options
            val keyDerivationOptionsJson = intent.getStringExtra("keyDerivationOptionsJson")
            val keyDerivationOptions = KeyDerivationOptions.fromJson(keyDerivationOptionsJson ?: "{}")

            // Enforce client restrictions
            val restrictToClientApplicationsIdPrefixes = keyDerivationOptions?.restrictToClientApplicationsIdPrefixes
            val clientsApplicationId: String = callingActivity?.packageName ?: ""
            if (restrictToClientApplicationsIdPrefixes != null && restrictToClientApplicationsIdPrefixes?.size > 0) {
                // The key derivation options require us to ensure that the client's application/package
                // starts with one of the included prefixes.

                val numberOfValidPrefixes = restrictToClientApplicationsIdPrefixes.count{
                    p -> clientsApplicationId.startsWith(p)
                }
                if (numberOfValidPrefixes == 0) {
                    // The client application id does not start with any of the specified prefixes
                    throw ClientNotAuthorizeException(clientsApplicationId, restrictToClientApplicationsIdPrefixes)
                }
            }

            if (KeySqrState.keySqr == null) {
                // We need to first trigger an action to load the key square, then come back to this
                // intent.
                // FIXME
            }

            val postDecryptionInstructionsJson = intent.getStringExtra("postDecryptionInstructionsJson")
            try {
                when (action) {
                    org.dicekeys.api.Actions.Seed.get -> {
                        // FIXME -- return number of errors in read or if key was manually entered.
                        val seed =
                                KeySqrState.keySqr?.getSeed(keyDerivationOptionsJson, clientsApplicationId)
                        var newIntent = Intent()
                        newIntent.putExtra("seed", seed)
                        setResult(RESULT_OK, newIntent)
                        finish()
                    }
                    org.dicekeys.api.Actions.SymmetricKey.seal -> {
                        // FIXME -- validate key read without errors
                        val plaintext = intent.getByteArrayExtra("plaintext")
                        val ciphertext = KeySqrState.keySqr
                                ?.getSymmetricKey(keyDerivationOptionsJson, clientsApplicationId)
                                ?.seal(plaintext, postDecryptionInstructionsJson)
                        var newIntent = Intent()
                        newIntent.putExtra("ciphertext", ciphertext)
                        setResult(RESULT_OK, newIntent)
                        finish()
                    }
                    org.dicekeys.api.Actions.SymmetricKey.unseal -> {
                        val ciphertext = intent.getByteArrayExtra("ciphertext")
                        val plaintext = KeySqrState.keySqr
                                ?.getSymmetricKey(keyDerivationOptionsJson, clientsApplicationId)
                                ?.unseal(ciphertext, postDecryptionInstructionsJson)
                        var newIntent = Intent()
                        newIntent.putExtra("plaintext", plaintext)
                        setResult(RESULT_OK, newIntent)
                        finish()
                    }
                    org.dicekeys.api.Actions.PublicPrivateKeyPair.getPublic -> {
                        // FIXME -- validate key read without errors
                        val publicKeyJson = KeySqrState.keySqr
                                ?.getPublicKey(keyDerivationOptionsJson, clientsApplicationId)
                                ?.toJson()
                        var newIntent = Intent()
                        newIntent.putExtra("publicKeyJson", publicKeyJson)
                        setResult(RESULT_OK, newIntent)
                        finish()
                    }
                    org.dicekeys.api.Actions.PublicPrivateKeyPair.unseal -> {
                        val ciphertext = intent.getByteArrayExtra("ciphertext")
                        val plaintext = KeySqrState.keySqr
                                ?.getPublicPrivateKeyPair(keyDerivationOptionsJson, clientsApplicationId)
                                ?.unseal(ciphertext, postDecryptionInstructionsJson)
                        var newIntent = Intent()
                        newIntent.putExtra("plaintext", plaintext)
                        setResult(RESULT_OK, newIntent)
                        finish()
                    }
                }
            } catch (e: Exception){
                var newIntent = Intent()
                newIntent.putExtra("exception", e)
                setResult(Activity.RESULT_CANCELED, newIntent)
                finish()

            }
        }
    }

}