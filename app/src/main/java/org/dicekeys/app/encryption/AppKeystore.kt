package org.dicekeys.app.encryption

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
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
     */
    enum class KeystoreType {
        BIOMETRIC,
        AUTHENTICATION,
        KEYSTORE,
    }

    private var keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
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
            KeystoreType.BIOMETRIC  -> {
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
            KeystoreType.AUTHENTICATION -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    builder.setUnlockedDeviceRequired(true)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    builder.setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
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
            KeystoreType.KEYSTORE -> getEncryptionCipher(BASIC_KEYSTORE_ALIAS, KeystoreType.KEYSTORE)
            KeystoreType.AUTHENTICATION -> getEncryptionCipher(AUTHENTICATION_KEYSTORE_ALIAS, KeystoreType.AUTHENTICATION)
            KeystoreType.BIOMETRIC -> getEncryptionCipher(BIOMETRICS_KEYSTORE_ALIAS, KeystoreType.BIOMETRIC)
        }
    }

    @Throws(Exception::class)
    fun getDecryptionCipher(encryptedData: EncryptedData, keystoreType: KeystoreType): Cipher {
        return when(keystoreType){
            KeystoreType.KEYSTORE -> getDecryptionCipher(BASIC_KEYSTORE_ALIAS, encryptedData)
            KeystoreType.AUTHENTICATION -> getDecryptionCipher(AUTHENTICATION_KEYSTORE_ALIAS, encryptedData)
            KeystoreType.BIOMETRIC -> getDecryptionCipher(BIOMETRICS_KEYSTORE_ALIAS, encryptedData)
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
