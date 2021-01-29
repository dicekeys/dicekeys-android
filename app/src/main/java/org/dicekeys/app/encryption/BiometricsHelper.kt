package org.dicekeys.app.encryption

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.dicekeys.app.extensions.toast
import org.dicekeys.dicekey.DiceKey

class BiometricsHelper(private val appKeystore: AppKeystore, private val encryptedStorage: EncryptedStorage) {

    fun encrypt(diceKey: DiceKey<*>, fragment: Fragment) {
        if (appKeystore.canUseBiometrics(fragment.requireContext())) {

            // TODO change texts

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Biometric Encryption")
                    .setSubtitle("Log in using your biometric credential")
                    .setDescription("This is a description for the action")
                    .setNegativeButtonText(fragment.getString(android.R.string.cancel))
                    .setConfirmationRequired(true)
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                    .build()

            val biometricPrompt = BiometricPrompt(fragment, ContextCompat.getMainExecutor(fragment.requireContext()),
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationError(
                                errorCode: Int,
                                errString: CharSequence
                        ) {
                            super.onAuthenticationError(errorCode, errString)

                            if (errorCode == BiometricPrompt.ERROR_USER_CANCELED || errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON || errorCode == BiometricPrompt.ERROR_CANCELED) {
                                // This is OK
                            } else {
                                fragment.toast("Authentication error:$errorCode $errString")
                            }
                        }

                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)

                            result.cryptoObject?.cipher?.let { cipher ->
                                // Encrypt DiceKey
                                val encryptedData = appKeystore.encryptData(cipher, diceKey.toHumanReadableForm().toByteArray())
                                // Save data
                                encryptedStorage.save(diceKey, encryptedData)
                            }
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            fragment.toast("Authentication failed")
                        }
                    })

            try {
                biometricPrompt.authenticate(
                        promptInfo,
                        BiometricPrompt.CryptoObject(appKeystore.getBiometricsEncryptionCipher())
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else {
            fragment.toast("You have to enable Biometrics to use this feature.")
        }
    }

    fun decrypt(encryptedDiceKey: EncryptedDiceKey, fragment: Fragment, success: (diceKey: DiceKey<*>)->Unit) {
        // TODO change texts
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Decryption")
                .setSubtitle("Log in using your biometric credential")
                .setDescription("This is a description for the action")
                .setNegativeButtonText(fragment.getString(android.R.string.cancel))
                .setConfirmationRequired(true)
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build()

        val biometricPrompt = BiometricPrompt(fragment, ContextCompat.getMainExecutor(fragment.requireContext()),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(
                            errorCode: Int,
                            errString: CharSequence
                    ) {
                        super.onAuthenticationError(errorCode, errString)
                        fragment.toast("$errString")

                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)

                        result.cryptoObject?.cipher?.let { cipher ->

                            val humanReadable = String(appKeystore.decryptData(cipher, encryptedDiceKey.encryptedData))
                            val diceKey = DiceKey.fromHumanReadableForm(humanReadable)

                            success.invoke(diceKey)
                        }
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        fragment.toast("Authentication failed")
                    }
                })

        try {
            biometricPrompt.authenticate(
                    promptInfo,
                    BiometricPrompt.CryptoObject(appKeystore.getBiometricsDecryptionCipher(encryptedDiceKey.encryptedData))
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}