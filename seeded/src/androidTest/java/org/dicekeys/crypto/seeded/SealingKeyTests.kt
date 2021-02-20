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
class SealingKeyTests {
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
        val recipeJson = """{}"""
        val publicKeyJson = """{
            "keyBytes": "000102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e0f",
            "recipe": "$recipeJson"
        }"""
        val pk = SealingKey.fromJson(publicKeyJson)
        assertEquals(0x0f, pk.keyBytes[31].toInt())
        assertEquals(recipeJson, pk.recipeJson)
        val copyOfPk = SealingKey(pk.keyBytes, pk.recipeJson)
        assertEquals(pk, copyOfPk)
    }

    val seed = "some seedy value to test with"

    @Test
    fun testPrivateKeys() {
        val recipeJson = """{"type": "UnsealingKey"}"""
        val sk = UnsealingKey.deriveFromSeed(seed, recipeJson)
        val pk = sk.getSealingkey()
        val testMessage = "some message to test"
        val packagedSealedMessage = pk.seal( testMessage );
        val decryptedBytes = UnsealingKey.unseal(seed, packagedSealedMessage )
        val decryptedMessage = String(decryptedBytes)
        assertEquals(testMessage, decryptedMessage)

        val binaryCopy = pk.toSerializedBinaryForm()
        val copy = SealingKey.fromSerializedBinaryForm(binaryCopy)
        assertEquals(copy, pk)

        val binaryCopySk = sk.toSerializedBinaryForm()
        val copySk = UnsealingKey.fromSerializedBinaryForm(binaryCopySk)
        assertEquals(copySk, sk)


    }

    @Test
    fun shouldThrowInvalidDerivationOptionValueException() {
        val recipeJson = """{}"""
        val publicKeyJson = """{
            "ErrorInsteadOfKeyBytes": "000102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e0f",
            "recipe": "$recipeJson"
        }"""
        try {
            SealingKey.fromJson(publicKeyJson)
            fail()
        } catch (e: InvalidDerivationOptionValueException) {
        } catch (e: JsonParsingException) {
        } catch (other: Exception) {
            fail()
        }
    }

    @Test
    fun shouldThrowJsonParsingException() {
        val publicKeyJson = """{
            withoutQuotesBeforeAColon: This$ - is not ! valid JSON
        }"""
        try {
            SealingKey.fromJson(publicKeyJson)
            fail()
        } catch (e: JsonParsingException) {
        } catch (other: Exception) {
            fail()
        }
    }

    @Test
    fun createSignatureVerificationKeyFromJson() {
        val recipeJson = """{}"""
        val signatureVerificationKeyJson = """{
            "keyBytes": "000102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e0f",
            "recipe": "$recipeJson"
        }"""
        val svk = SignatureVerificationKey.fromJson(signatureVerificationKeyJson)
        assertEquals(0x0f, svk.keyBytes[31].toInt())
        assertEquals(recipeJson, svk.recipeJson)
        val copyOfSvk = SignatureVerificationKey(svk.keyBytes, svk.recipeJson)
        assertEquals(svk, copyOfSvk)

        val bCopy = svk.toSerializedBinaryForm()
        val copy = SignatureVerificationKey.fromSerializedBinaryForm(bCopy)
        assertEquals(copy, svk)
    }

    @Test
    fun testSigningAndVerificationPairs() {
        val recipeJson = """{"type": "SigningKey"}"""
        val sk = SigningKey.deriveFromSeed(seed, recipeJson)
        val vk = sk.getSignatureVerificationKey()
        val testMessage = "some message to test"
        val signature = sk.generateSignature( testMessage )
        val isValid = vk.verifySignature(testMessage, signature)
        assertTrue(isValid)
        // Create an invalid signature by copying a valid signature and then
        // incrementing the first byte.
        var invalidSignature: ByteArray = signature
        invalidSignature[0]++
        val invalidSignatureResult = vk.verifySignature(testMessage, invalidSignature)
        assertFalse(invalidSignatureResult)
        val invalidMessageResult = vk.verifySignature(testMessage + "invalid modification", signature)
        assertFalse(invalidMessageResult)

        val skBinaryCopy = sk.toSerializedBinaryForm()
        val copy = SigningKey.fromSerializedBinaryForm(skBinaryCopy)
        assertEquals(copy, sk)

    }


    @Test
    fun testSymmetricKey() {
        val recipeJson = """{"type": "SymmetricKey"}"""
        val sk = SymmetricKey.deriveFromSeed(seed, recipeJson)
        val testMessage = "some message to test"
        val packagedSealedMessage = sk.seal(testMessage)
        val decryptedBytes = SymmetricKey.unseal(packagedSealedMessage, seed)
        val decryptedMessage = String(decryptedBytes)
        assertEquals(testMessage, decryptedMessage)

        val binaryCopy = sk.toSerializedBinaryForm()
        val copy = SymmetricKey.fromSerializedBinaryForm(binaryCopy)
        assertArrayEquals(copy.keyBytes, sk.keyBytes)
        assertEquals(copy.recipeJson, sk.recipeJson)

    }

    @Test
    fun testSeed() {
        val recipeJson = """{"type": "Secret", "lengthInBytes": 48}"""
        val s = Secret.deriveFromSeed(seed, recipeJson)
        assertEquals(s.secretBytes.size, 48)

        val binaryCopy = s.toSerializedBinaryForm()
        val copy = Secret.fromSerializedBinaryForm(binaryCopy)
        assertArrayEquals(copy.secretBytes, s.secretBytes)
        assertEquals(copy.recipeJson, s.recipeJson)
    }


}
