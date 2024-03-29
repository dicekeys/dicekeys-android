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

class BiometricsHelper(private val appKeyStore: AppKeyStore, private val encryptedStorage: EncryptedStorage) {

    fun canUseBiometrics(context: Context): Boolean {
        return BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
    }

    // On Android 12 the user must be authenticated in order to use any crypto-based key
    private fun authenticateUser(fragment: Fragment, callback: AuthenticationCallback, onlyDeviceCredentials: Boolean = false){
        val biometricPrompt = BiometricPrompt(fragment, ContextCompat.getMainExecutor(fragment.requireContext()), object : AuthenticationCallback(fragment) {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                // Seems there is a bug and if we execute immediately before the callback is finished
                // the new prompt won't execute the callback
                ContextCompat.getMainExecutor(fragment.requireContext()).execute {
                    callback.onAuthenticationSucceeded(result)
                }
            }})

        val promptInfo = BiometricPrompt.PromptInfo.Builder().let {
            it.setTitle(fragment.getString(R.string.user_authentication))
                .setSubtitle(fragment.getString(R.string.you_have_to_authenticate_to_unlock_your_device))
                .setConfirmationRequired(true)
                .setAllowedAuthenticators(if (onlyDeviceCredentials) BiometricManager.Authenticators.DEVICE_CREDENTIAL else BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            it.build()

        }
        authenticate(biometricPrompt, promptInfo)
    }

    fun encrypt(diceKey: DiceKey<*>, keyStoreType: AppKeyStore.KeyStoreCredentialsAllowed, fragment: Fragment, onlyDeviceCredentials: Boolean = false) {

        if (keyStoreType == AppKeyStore.KeyStoreCredentialsAllowed.ALLOW_ONLY_BIOMETRIC_AUTHENTICATION && !canUseBiometrics(fragment.requireContext())) {
            fragment.toast(R.string.biometrics_unavailable_message)
            return
        }

        try {

            if (appKeyStore.isAuthenticationRequired(keyStoreType)) {
                authenticateUser(
                    fragment,
                    object : AuthenticationCallback(fragment) {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            // onlyDeviceCredentials = true prevent eternal loops
                            encrypt(diceKey, keyStoreType, fragment, onlyDeviceCredentials = false)
                        }
                    }, onlyDeviceCredentials = onlyDeviceCredentials)
                return
            }

            val promptInfo = createBiometricPrompt(true, keyStoreType, fragment)

            val biometricPrompt = BiometricPrompt(fragment, ContextCompat.getMainExecutor(fragment.requireContext()),
                    object : AuthenticationCallback(fragment) {

                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            try{
                                val cipher: Cipher? = if(keyStoreType == AppKeyStore.KeyStoreCredentialsAllowed.ALLOW_ONLY_BIOMETRIC_AUTHENTICATION){
                                    result.cryptoObject?.cipher
                                }else{
                                    appKeyStore.getEncryptionCipher(keyStoreType)
                                }

                                cipher?.let { cipher ->
                                    // Encrypt DiceKey
                                    val encryptedData = appKeyStore.encryptData(cipher, diceKey.toHumanReadableForm().toByteArray())
                                    // Save data
                                    encryptedStorage.save(diceKey, encryptedData, keyStoreType)
                                }
                            }catch (e: Exception){
                                fragment.errorDialog(e)
                            }
                        }
                    })

            if(keyStoreType == AppKeyStore.KeyStoreCredentialsAllowed.ALLOW_ONLY_BIOMETRIC_AUTHENTICATION){
                val cryptoObject = BiometricPrompt.CryptoObject(appKeyStore.getEncryptionCipher(keyStoreType))
                authenticate(biometricPrompt, promptInfo, cryptoObject)
            }else{
                authenticate(biometricPrompt, promptInfo)
            }
        } catch (e: Exception) {
            fragment.errorDialog(e)
        }
    }

    fun decrypt(encryptedDiceKey: EncryptedDiceKey, fragment: Fragment, success: (diceKey: DiceKey<Face>) -> Unit, onlyDeviceCredentials: Boolean = false) {
        try {
            if (appKeyStore.isAuthenticationRequired(encryptedDiceKey.keyStoreType)) {
                authenticateUser(
                    fragment = fragment,
                    callback = object : AuthenticationCallback(fragment) {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            // onlyDeviceCredentials = true prevent eternal loops
                            decrypt(encryptedDiceKey, fragment, success, onlyDeviceCredentials = true)
                        }
                    }, onlyDeviceCredentials = onlyDeviceCredentials)
                return
            }

            val promptInfo = createBiometricPrompt(false, encryptedDiceKey.keyStoreType, fragment)

            val biometricPrompt = BiometricPrompt(fragment, ContextCompat.getMainExecutor(fragment.requireContext()),
                    object : AuthenticationCallback(fragment) {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            try{

                                val cipher: Cipher? = if(encryptedDiceKey.keyStoreType == AppKeyStore.KeyStoreCredentialsAllowed.ALLOW_ONLY_BIOMETRIC_AUTHENTICATION){
                                    result.cryptoObject?.cipher
                                }else{
                                    appKeyStore.getDecryptionCipher(encryptedDiceKey.encryptedData, encryptedDiceKey.keyStoreType)
                                }

                                cipher?.let { cipher ->
                                    val humanReadable = String(appKeyStore.decryptData(cipher, encryptedDiceKey.encryptedData))
                                    val diceKey = DiceKey.fromHumanReadableForm(humanReadable).toCenterUprightRotation()
                                    success.invoke(diceKey)
                                }

                            }catch (e: Exception){
                                fragment.errorDialog(e)
                            }
                        }
                    })

            if(encryptedDiceKey.keyStoreType == AppKeyStore.KeyStoreCredentialsAllowed.ALLOW_ONLY_BIOMETRIC_AUTHENTICATION){
                val cryptoObject = BiometricPrompt.CryptoObject(appKeyStore.getDecryptionCipher(encryptedDiceKey.encryptedData, encryptedDiceKey.keyStoreType))
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

    private fun authenticate(biometricPrompt: BiometricPrompt, promptInfo: BiometricPrompt.PromptInfo, cryptoObject: BiometricPrompt.CryptoObject? = null) {
        if(cryptoObject == null){
            biometricPrompt.authenticate(promptInfo)
        }else{
            biometricPrompt.authenticate(promptInfo, cryptoObject)
        }
    }

    private fun createBiometricPrompt(promptForEncryption: Boolean, keyStoreType: AppKeyStore.KeyStoreCredentialsAllowed, fragment: Fragment): BiometricPrompt.PromptInfo {
        val title: String
        val subtitle: String

        if (promptForEncryption) {
            title = fragment.getString(R.string.biometrics_encryption_title)
            subtitle = if (keyStoreType == AppKeyStore.KeyStoreCredentialsAllowed.ALLOW_ONLY_BIOMETRIC_AUTHENTICATION) {
                fragment.getString(R.string.biometrics_encryption_subtitle)
            }else{
                fragment.getString(R.string.authenticate_encryption_subtitle)
            }
        } else {
            title = fragment.getString(R.string.biometrics_decryption_title)
            subtitle = if (keyStoreType == AppKeyStore.KeyStoreCredentialsAllowed.ALLOW_ONLY_BIOMETRIC_AUTHENTICATION) {
                fragment.getString(R.string.biometrics_decryption_subtitle)
            }else{
                fragment.getString(R.string.authenticate_decryption_subtitle)
            }
        }

        return BiometricPrompt.PromptInfo.Builder().let {
            it.setTitle(title)
                    .setSubtitle(subtitle)
                    .setConfirmationRequired(false)

            if (keyStoreType == AppKeyStore.KeyStoreCredentialsAllowed.ALLOW_ONLY_BIOMETRIC_AUTHENTICATION) {
                it.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                it.setNegativeButtonText(fragment.getString(android.R.string.cancel))
            }else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && keyStoreType == AppKeyStore.KeyStoreCredentialsAllowed.ALLOW_ONLY_KNOWLEDGE_BASED_AUTHENTICATION) {
                    it.setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        it.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                    } else {
                        it.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                    }
                }
            }

            it.build()
        }
    }
}

open class AuthenticationCallback(val fragment: Fragment) : BiometricPrompt.AuthenticationCallback() {
    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        if (errorCode == BiometricPrompt.ERROR_USER_CANCELED || errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON || errorCode == BiometricPrompt.ERROR_CANCELED) {
            // This is errorCode OK, no need to handle it
        } else {
            fragment.toast("$errString")
        }
    }

    override fun onAuthenticationFailed() {
        fragment.toast("Authentication failed")
    }
}