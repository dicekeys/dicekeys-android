package org.dicekeys.app.encryption

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.security.keystore.UserNotAuthenticatedException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.security.KeyStore
import java.security.KeyStoreException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

/*
 * AppKeystore
 *
 * AppKeystore is a wrapper around Android Keystore.
 * You can encrypt/decrypt data using private keys backed by different keystore settings.
 *
 */

class AppKeystore {

    /*
     * Check [initializeKeyStoreKey] for initialization settings for each option.
     * The general idea is to have the most strict/secure options for BIOMETRIC, and simple device authentication
     * with AUTHENTICATION option. Keystore exists as an option but is not used.
     *
     * - BIOMETRIC has the most strict keystore settings, requires any form of supported biometrics to unlock the encryption key (fingerprint, face). [1]
     * - AUTHENTICATION requires user to authenticate himself by the use of PIN/PATTERN/PASSWORD or a Biometric method. Similar to what he is using to unlock his screen lock.
     * - DEVICE_CREDENTIALS exactly the same as AUTHENTICATION (keystore settings/key alias). User is requested to unlock only with PIN/PATTERN/PASSWORD.
     *
     * Note: Encryption key can be invalidated when setInvalidatedByBiometricEnrollment is set to true and new biometrics are enrolled. This option is enabled
     * only on BIOMETRIC type. Check Android documentation for more information. [2]
     *
     * [1] https://developer.android.com/training/sign-in/biometric-auth
     * [2] https://developer.android.com/reference/android/security/keystore/KeyGenParameterSpec.Builder
     */

    @Serializable
    enum class KeystoreType {
        // Use @SerialName to make serializable enum backwards compatible
        @SerialName("BIOMETRIC") ALLOW_ONLY_BIOMETRIC_AUTHENTICATION,
        @SerialName("AUTHENTICATION") ALLOW_BIOMETRIC_OR_KNOWLEDGE_BASED_AUTHENTICATION,
        @SerialName("DEVICE_CREDENTIALS") ALLOW_ONLY_KNOWLEDGE_BASED_AUTHENTICATION,
        @SerialName("KEYSTORE") KEYSTORE,
    }

    private var keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    fun isAuthenticationRequired(keystoreType: KeystoreType): Boolean {
        try{
            when(keystoreType){
                KeystoreType.KEYSTORE -> getEncryptionCipher(BASIC_KEYSTORE_ALIAS, keystoreType)
                KeystoreType.ALLOW_BIOMETRIC_OR_KNOWLEDGE_BASED_AUTHENTICATION, KeystoreType.ALLOW_ONLY_KNOWLEDGE_BASED_AUTHENTICATION -> getEncryptionCipher(AUTHENTICATION_KEYSTORE_ALIAS, keystoreType)
                KeystoreType.ALLOW_ONLY_BIOMETRIC_AUTHENTICATION -> getEncryptionCipher(BIOMETRICS_KEYSTORE_ALIAS, keystoreType)
            }
        }catch (e: UserNotAuthenticatedException){
            e.printStackTrace()
            return true
        }catch (e: Exception){
            e.printStackTrace()
        }

        return false
    }

    private fun keyStoreKeyExists(keystoreAlias: String): Boolean {
        try {
            return keyStore.containsAlias(keystoreAlias)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun deleteFromKeyStore(keystoreAlias: String) {
        try {
            // remove keystore alias
            keyStore.deleteEntry(keystoreAlias)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isKeyStoreValid(keystoreAlias: String): Boolean {
        try {
            getEncryptionCipher(keystoreAlias)
            return true
        } catch (e: KeyPermanentlyInvalidatedException) {
            e.printStackTrace()
            return false
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    @Throws(Exception::class)
    fun initializeKeyStoreKey(keystoreAlias: String, keystoreType: KeystoreType) {
        if (keyStoreKeyExists(keystoreAlias)) {
            throw KeyStoreException("KeyStore is already created for $keystoreAlias")
        }

        val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
        )

        val builder = KeyGenParameterSpec.Builder(
                keystoreAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)

        when(keystoreType){
            KeystoreType.ALLOW_ONLY_BIOMETRIC_AUTHENTICATION  -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    builder.setUnlockedDeviceRequired(true)
                }

                builder.setUserAuthenticationRequired(true)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    builder.setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
                } else {
                    builder.setUserAuthenticationValidityDurationSeconds(-1)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    builder.setInvalidatedByBiometricEnrollment(true)
                }
            }
            KeystoreType.ALLOW_BIOMETRIC_OR_KNOWLEDGE_BASED_AUTHENTICATION, KeystoreType.ALLOW_ONLY_KNOWLEDGE_BASED_AUTHENTICATION -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    builder.setUnlockedDeviceRequired(true)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    builder.setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL)
                } else {
                    builder.setUserAuthenticationValidityDurationSeconds(-1)
                }
            }
            KeystoreType.KEYSTORE -> {
               // No need to init anything
            }
        }

        keyGenerator.init(builder.build())

        // Add it to KeyStore
        keyGenerator.generateKey()
    }

    @Throws(Exception::class)
    private fun getEncryptionCipher(keystoreAlias: String): Cipher {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val key = keyStore.getKey(keystoreAlias, null) as SecretKey
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher
    }

    private fun getEncryptionCipher(keystoreAlias: String, keystoreType: KeystoreType): Cipher {
        if (!keyStoreKeyExists(keystoreAlias) || !isKeyStoreValid(keystoreAlias)) {
            deleteFromKeyStore(keystoreAlias)
            initializeKeyStoreKey(keystoreAlias, keystoreType)
        }

        return getEncryptionCipher(keystoreAlias)
    }

    @Throws(Exception::class)
    private fun getDecryptionCipher(keystoreAlias: String, encryptedData: EncryptedData): Cipher {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val key = keyStore.getKey(keystoreAlias, null)
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(encryptedData.getIv()))
        return cipher
    }

    @Throws(Exception::class)
    fun getEncryptionCipher(keystoreType: KeystoreType): Cipher {
        return when(keystoreType){
            KeystoreType.KEYSTORE -> getEncryptionCipher(BASIC_KEYSTORE_ALIAS, keystoreType)
            KeystoreType.ALLOW_BIOMETRIC_OR_KNOWLEDGE_BASED_AUTHENTICATION, KeystoreType.ALLOW_ONLY_KNOWLEDGE_BASED_AUTHENTICATION -> getEncryptionCipher(AUTHENTICATION_KEYSTORE_ALIAS, keystoreType)
            KeystoreType.ALLOW_ONLY_BIOMETRIC_AUTHENTICATION -> getEncryptionCipher(BIOMETRICS_KEYSTORE_ALIAS, keystoreType)
        }
    }

    @Throws(Exception::class)
    fun getDecryptionCipher(encryptedData: EncryptedData, keystoreType: KeystoreType): Cipher {
        return when(keystoreType){
            KeystoreType.KEYSTORE -> getDecryptionCipher(BASIC_KEYSTORE_ALIAS, encryptedData)
            KeystoreType.ALLOW_BIOMETRIC_OR_KNOWLEDGE_BASED_AUTHENTICATION, KeystoreType.ALLOW_ONLY_KNOWLEDGE_BASED_AUTHENTICATION -> getDecryptionCipher(AUTHENTICATION_KEYSTORE_ALIAS, encryptedData)
            KeystoreType.ALLOW_ONLY_BIOMETRIC_AUTHENTICATION -> getDecryptionCipher(BIOMETRICS_KEYSTORE_ALIAS, encryptedData)
        }
    }

    @Throws(Exception::class)
    fun encryptData(dataToEncrypt: ByteArray): EncryptedData {
        if (!keyStoreKeyExists(BASIC_KEYSTORE_ALIAS)) {
            initializeKeyStoreKey(BASIC_KEYSTORE_ALIAS, KeystoreType.KEYSTORE)
        }

        val cipher = getEncryptionCipher(BASIC_KEYSTORE_ALIAS)

        return encryptData(cipher, dataToEncrypt)
    }

    @Throws(Exception::class)
    fun encryptData(cipher: Cipher, dataToEncrypt: ByteArray): EncryptedData {
        return EncryptedData.fromByteArray(cipher.doFinal(dataToEncrypt), cipher.iv)
    }

    @Throws(Exception::class)
    fun decryptData(encryptedData: EncryptedData): ByteArray {
        if (!keyStoreKeyExists(BASIC_KEYSTORE_ALIAS)) {
            throw KeyStoreException("KeyStore Keys are not created. You have to init them first.")
        }

        val cipher = getDecryptionCipher(BASIC_KEYSTORE_ALIAS, encryptedData)

        return decryptData(cipher, encryptedData)
    }

    @Throws(Exception::class)
    fun decryptData(cipher: Cipher, encryptedData: EncryptedData): ByteArray {
        return cipher.doFinal(encryptedData.getEncryptedData())
    }

    companion object {
        private const val TRANSFORMATION =
                "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"

        const val BASIC_KEYSTORE_ALIAS = "v1-basic"
        const val AUTHENTICATION_KEYSTORE_ALIAS = "v1-authentication"
        const val BIOMETRICS_KEYSTORE_ALIAS = "v1-biometrics"
    }
}
