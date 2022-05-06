package org.dicekeys.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.dicekeys.app.encryption.AppKeyStore
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Exception

@RunWith(AndroidJUnit4::class)
class AppKeyStoreUnitTests {
    private val ALIAS = "test-alias"
    private val MESSAGE = "A nice message to encrypt"

    private lateinit var appKeyStore: AppKeyStore

    @Before
    fun setup() {
        appKeyStore = AppKeyStore()
        appKeyStore.deleteFromKeyStore(ALIAS)
    }

    @Test
    fun test_keystore_encryption_decryption() {
        val encryptedData = appKeyStore.encryptData(MESSAGE.toByteArray())
        val decryptedBytes = appKeyStore.decryptData(encryptedData)
        Assert.assertEquals(String(decryptedBytes), MESSAGE)
    }

    @Test
    fun test_encryption_same_message_different_ciphertext() {
        val encryptedData1 = appKeyStore.encryptData(MESSAGE.toByteArray())
        val encryptedData2 = appKeyStore.encryptData(MESSAGE.toByteArray())

        Assert.assertNotEquals(encryptedData1, encryptedData2)

        val decryptedBytes1 = appKeyStore.decryptData(encryptedData1)
        val decryptedBytes2 = appKeyStore.decryptData(encryptedData2)

        Assert.assertEquals(String(decryptedBytes1), String(decryptedBytes2))
    }

    @Test(expected = Exception::class)
    fun on_duplicate_initialization_throw_exception() {
        appKeyStore.initializeKeyStoreKey(ALIAS, AppKeyStore.KeyStoreCredentialsAllowed.ALLOW_ACCESS_WITHOUT_REAUTHENTICATION)
        appKeyStore.initializeKeyStoreKey(ALIAS, AppKeyStore.KeyStoreCredentialsAllowed.ALLOW_ACCESS_WITHOUT_REAUTHENTICATION)
    }

    @Test
    fun test_key_alias_removal() {
        appKeyStore.initializeKeyStoreKey(ALIAS, AppKeyStore.KeyStoreCredentialsAllowed.ALLOW_ACCESS_WITHOUT_REAUTHENTICATION)
        appKeyStore.deleteFromKeyStore(ALIAS)
        appKeyStore.initializeKeyStoreKey(ALIAS, AppKeyStore.KeyStoreCredentialsAllowed.ALLOW_ACCESS_WITHOUT_REAUTHENTICATION)
    }
}