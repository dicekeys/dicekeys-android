package org.dicekeys

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import androidx.appcompat.app.AppCompatActivity
import org.dicekeys.activities.ExecuteApiCommandActivity

class Api {
    companion object {
        fun ensureKeyLoaded(callingActivity: AppCompatActivity) {
            val intent = Intent(callingActivity, ExecuteApiCommandActivity::class.java).apply {
                action = OperationNames.UI.ensureKeyLoaded
            }
            callingActivity.startActivityForResult(intent, 0)
        }
    }

    object ParameterNames {
        const val ciphertext = "ciphertext"
        const val keyDerivationOptionsJson = "keyDerivationOptionsJson"
        const val plaintext = "plaintext"
        const val postDecryptionInstructionsJson = "postDecryptionInstructionsJson"
        const val publicKeyJson = "publicKeyJson"
        const val seed = "seed"
        const val exception = "exception"
    }

    object OperationNames {
        object UI {
            const val ensureKeyLoaded = "org.dicekeys.api.actions.UI.ensureKeyLoaded"
        }

        object Seed {
            const val get = "org.dicekeys.api.actions.Seed.get"
        }

        object SymmetricKey {
            const val seal = "org.dicekeys.api.actions.SymmetricKey.seal"
            const val unseal = "org.dicekeys.api.actions.SymmetricKey.unseal"
        }

        object PublicPrivateKeyPair {
            const val getPublic = "org.dicekeys.api.actions.PublicPrivateKeyPair.getPublic"
            const val unseal = "org.dicekeys.api.actions.PublicPrivateKeyPair.unseal"
        }

        val All = setOf(
                UI.ensureKeyLoaded,
                Seed.get,
                SymmetricKey.seal,
                SymmetricKey.unseal,
                PublicPrivateKeyPair.getPublic,
                PublicPrivateKeyPair.unseal
        )
    }

}