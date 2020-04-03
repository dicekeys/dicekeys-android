package org.dicekeys.crypto.seeded

import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import java.lang.Exception

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class PublicKeyTests {
//    @Test
//    fun useAppContext() {
//        // Context of the app under test.
//        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
//        assertEquals("org.dicekeys.crypto.seeded.test", appContext.packageName)
//    }

//    @Test
//    fun exampleFailingTest() {
//        assertEquals(0, 1)
//    }

    @Test
    fun createPublicKeyFromJson() {
        val keyDerivationOptionsJson = """{}"""
        val publicKeyJson = """{
            "keyBytes": "000102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e0f",
            "keyDerivationOptionsJson": "$keyDerivationOptionsJson"
        }"""
        val pk = PublicKey(publicKeyJson)
        assertEquals(0x0f, pk.keyBytes[31].toInt())
        assertEquals(keyDerivationOptionsJson, pk.keyDerivationOptionsJson)
        val copyOfPk = PublicKey(pk.keyBytes, pk.keyDerivationOptionsJson)
        assertEquals(pk, copyOfPk)
    }

    @Test
    fun shouldThrowJsonParsingException() {
        val keyDerivationOptionsJson = """{}"""
        val publicKeyJson = """{
            "ErrorInsteadOfKeyBytes": "000102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e0f",
            "keyDerivationOptionsJson": "$keyDerivationOptionsJson"
        }"""
        try {
            val pk = PublicKey(publicKeyJson)
            fail()
        } catch (e: JsonParsingException) {
        } catch (other: Exception) {
            fail()
        }

    }

    @Test
    fun createSignatureVerificationKeyFromJson() {
        val keyDerivationOptionsJson = """{}"""
        val signatureVerificationKeyJson = """{
            "keyBytes": "000102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e0f",
            "keyDerivationOptionsJson": "$keyDerivationOptionsJson"
        }"""
        val svk = SignatureVerificationKey(signatureVerificationKeyJson)
        assertEquals(0x0f, svk.keyBytes[31].toInt())
        assertEquals(keyDerivationOptionsJson, svk.keyDerivationOptionsJson)
        val copyOfSvk = SignatureVerificationKey(svk.keyBytes, svk.keyDerivationOptionsJson)
        assertEquals(svk, copyOfSvk)
    }
}
