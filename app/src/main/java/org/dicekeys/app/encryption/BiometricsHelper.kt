package org.dicekeys.app.encryption

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.dicekeys.app.R
import org.dicekeys.app.extensions.toast
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face

/*
 *  BiometricsHelper
 *
 *  Helper class to handle BiometricPrompt specific to app use case
 */

class BiometricsHelper(private val appKeystore: AppKeystore, private val encryptedStorage: EncryptedStorage) {
    fun encrypt(diceKey: DiceKey<*>, fragment: Fragment) {

        if (appKeystore.canUseBiometrics(fragment.requireContext())) {
            try {
                val promptInfo = createBiometricPrompt(true, fragment)

                val biometricPrompt = BiometricPrompt(fragment, ContextCompat.getMainExecutor(fragment.requireContext()),
                        object : AuthenticationCallback(fragment) {

                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                super.onAuthenticationSucceeded(result)

                                result.cryptoObject?.cipher?.let { cipher ->
                                    // Encrypt DiceKey
                                    val encryptedData = appKeystore.encryptData(cipher, diceKey.toHumanReadableForm().toByteArray())
                                    // Save data
                                    encryptedStorage.save(diceKey, encryptedData)
                                }
                            }

                        })

                val cryptoObject = BiometricPrompt.CryptoObject(appKeystore.getBiometricsEncryptionCipher())
                authenticate(biometricPrompt, promptInfo, cryptoObject)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else {
            fragment.toast(R.string.biometrics_unavailable_message)
        }
    }


    fun decrypt(encryptedDiceKey: EncryptedDiceKey, fragment: Fragment, success: (diceKey: DiceKey<Face>) -> Unit) {
        try {
            val promptInfo = createBiometricPrompt(false, fragment)

            val biometricPrompt = BiometricPrompt(fragment, ContextCompat.getMainExecutor(fragment.requireContext()),
                    object : AuthenticationCallback(fragment) {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            result.cryptoObject?.cipher?.let { cipher ->
                                val humanReadable = String(appKeystore.decryptData(cipher, encryptedDiceKey.encryptedData))
                                val diceKey = DiceKey.fromHumanReadableForm(humanReadable)
                                success.invoke(diceKey)
                            }
                        }
                    })

            val cryptoObject = BiometricPrompt.CryptoObject(appKeystore.getBiometricsDecryptionCipher(encryptedDiceKey.encryptedData))
            authenticate(biometricPrompt, promptInfo, cryptoObject)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun authenticate(biometricPrompt: BiometricPrompt, promptInfo: BiometricPrompt.PromptInfo, cryptoObject: BiometricPrompt.CryptoObject) {
        biometricPrompt.authenticate(promptInfo, cryptoObject)
    }

    private fun createBiometricPrompt(promptForEncryption: Boolean, fragment: Fragment): BiometricPrompt.PromptInfo {
        val title: String
        val subtitle: String

        if(promptForEncryption){
            title = fragment.getString(R.string.biometrics_encryption_title)
            subtitle = fragment.getString(R.string.biometrics_encryption_subtitle)
        }else{
            title = fragment.getString(R.string.biometrics_decryption_title)
            subtitle = fragment.getString(R.string.biometrics_decryption_subtitle)
        }

        return BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setNegativeButtonText(fragment.getString(android.R.string.cancel))
                .setConfirmationRequired(true)
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build()
    }
}

open class AuthenticationCallback(val fragment: Fragment) : BiometricPrompt.AuthenticationCallback() {

    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        super.onAuthenticationError(errorCode, errString)
        if (errorCode == BiometricPrompt.ERROR_USER_CANCELED || errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON || errorCode == BiometricPrompt.ERROR_CANCELED) {
            // This is errorCode OK, no need to handle it
        } else {
            fragment.toast("$errString")
        }
    }

    override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()
        fragment.toast("Authentication failed")
    }
}