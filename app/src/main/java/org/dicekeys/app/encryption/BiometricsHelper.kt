package org.dicekeys.app.encryption

import android.content.Context
import android.os.Build
import android.security.keystore.KeyPermanentlyInvalidatedException
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.dicekeys.app.R
import org.dicekeys.app.extensions.errorDialog
import org.dicekeys.app.extensions.toast
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face
import javax.crypto.Cipher

/*
 *  BiometricsHelper
 *
 *  Helper class to handle BiometricPrompt specific to app use case
 */

class BiometricsHelper(private val appKeystore: AppKeystore, private val encryptedStorage: EncryptedStorage) {

    fun canUseBiometrics(context: Context): Boolean {
        return BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun encrypt(diceKey: DiceKey<*>, keystoreType: AppKeystore.KeystoreType, fragment: Fragment) {

        if (keystoreType == AppKeystore.KeystoreType.BIOMETRIC && !canUseBiometrics(fragment.requireContext())) {
            fragment.toast(R.string.biometrics_unavailable_message)
            return
        }

        try {
            val promptInfo = createBiometricPrompt(true, keystoreType, fragment)

            val biometricPrompt = BiometricPrompt(fragment, ContextCompat.getMainExecutor(fragment.requireContext()),
                    object : AuthenticationCallback(fragment) {

                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)

                            try{

                                val cipher: Cipher? = if(keystoreType == AppKeystore.KeystoreType.BIOMETRIC){
                                    result.cryptoObject?.cipher
                                }else{
                                    appKeystore.getEncryptionCipher(keystoreType)
                                }

                                cipher?.let { cipher ->
                                    // Encrypt DiceKey
                                    val encryptedData = appKeystore.encryptData(cipher, diceKey.toHumanReadableForm().toByteArray())
                                    // Save data
                                    encryptedStorage.save(diceKey, encryptedData, keystoreType)
                                }
                            }catch (e: Exception){
                                fragment.errorDialog(e)
                            }
                        }
                    })

            if(keystoreType == AppKeystore.KeystoreType.BIOMETRIC){
                val cryptoObject = BiometricPrompt.CryptoObject(appKeystore.getEncryptionCipher(keystoreType))
                authenticate(biometricPrompt, promptInfo, cryptoObject)
            }else{
                authenticate(biometricPrompt, promptInfo, null)
            }

        } catch (e: Exception) {
            fragment.errorDialog(e)
        }
    }

    fun decrypt(encryptedDiceKey: EncryptedDiceKey, fragment: Fragment, success: (diceKey: DiceKey<Face>) -> Unit) {
        try {
            val promptInfo = createBiometricPrompt(false, encryptedDiceKey.keystoreType, fragment)

            val biometricPrompt = BiometricPrompt(fragment, ContextCompat.getMainExecutor(fragment.requireContext()),
                    object : AuthenticationCallback(fragment) {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            try{

                                val cipher: Cipher? = if(encryptedDiceKey.keystoreType == AppKeystore.KeystoreType.BIOMETRIC){
                                    result.cryptoObject?.cipher
                                }else{
                                    appKeystore.getDecryptionCipher(encryptedDiceKey.encryptedData, encryptedDiceKey.keystoreType)
                                }

                                cipher?.let { cipher ->
                                    val humanReadable = String(appKeystore.decryptData(cipher, encryptedDiceKey.encryptedData))
                                    val diceKey = DiceKey.fromHumanReadableForm(humanReadable)
                                    success.invoke(diceKey)
                                }

                            }catch (e: Exception){
                                fragment.errorDialog(e)
                            }
                        }
                    })

            if(encryptedDiceKey.keystoreType == AppKeystore.KeystoreType.BIOMETRIC){
                val cryptoObject = BiometricPrompt.CryptoObject(appKeystore.getDecryptionCipher(encryptedDiceKey.encryptedData, encryptedDiceKey.keystoreType))
                authenticate(biometricPrompt, promptInfo, cryptoObject)
            }else{
                authenticate(biometricPrompt, promptInfo, null)
            }

        } catch (e: KeyPermanentlyInvalidatedException) {
            fragment.errorDialog(e)
        } catch (e: Exception) {
            fragment.errorDialog(e)
        }
    }

    private fun authenticate(biometricPrompt: BiometricPrompt, promptInfo: BiometricPrompt.PromptInfo, cryptoObject: BiometricPrompt.CryptoObject?) {
        if(cryptoObject == null){
            biometricPrompt.authenticate(promptInfo)
        }else{
            biometricPrompt.authenticate(promptInfo, cryptoObject)
        }
    }

    private fun createBiometricPrompt(promptForEncryption: Boolean, keystoreType: AppKeystore.KeystoreType, fragment: Fragment): BiometricPrompt.PromptInfo {
        val title: String
        val subtitle: String

        if (promptForEncryption) {
            title = fragment.getString(R.string.biometrics_encryption_title)
            subtitle = if (keystoreType == AppKeystore.KeystoreType.BIOMETRIC) {
                fragment.getString(R.string.biometrics_encryption_subtitle)
            }else{
                fragment.getString(R.string.authenticate_encryption_subtitle)
            }
        } else {
            title = fragment.getString(R.string.biometrics_decryption_title)
            subtitle = if (keystoreType == AppKeystore.KeystoreType.BIOMETRIC) {
                fragment.getString(R.string.biometrics_decryption_subtitle)
            }else{
                fragment.getString(R.string.authenticate_decryption_subtitle)
            }
        }

        return BiometricPrompt.PromptInfo.Builder().let {
            it.setTitle(title)
                    .setSubtitle(subtitle)
                    .setConfirmationRequired(true)

            if (keystoreType == AppKeystore.KeystoreType.BIOMETRIC) {
                it.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                it.setNegativeButtonText(fragment.getString(android.R.string.cancel))
            }else{
                if(Build.VERSION.SDK_INT == Build.VERSION_CODES.R){
                    // SDK 30
                    it.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                    it.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                }
            }

            it.build()
        }
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