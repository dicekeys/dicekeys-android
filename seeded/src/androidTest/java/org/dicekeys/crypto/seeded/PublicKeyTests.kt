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
        val pk = PublicKey.fromJson(publicKeyJson)
        assertEquals(0x0f, pk.keyBytes[31].toInt())
        assertEquals(keyDerivationOptionsJson, pk.keyDerivationOptionsJson)
        val copyOfPk = PublicKey(pk.keyBytes, pk.keyDerivationOptionsJson)
        assertEquals(pk, copyOfPk)
    }

    val seed = "some seedy value to test with"

    @Test
    fun testPrivateKeys() {
        val keyDerivationOptionsJson = """{"keyType": "Public"}"""
        val sk = PrivateKey.deriveFromSeed(seed, keyDerivationOptionsJson)
        val pk = sk.getPublicKey()
        val testMessage = "some message to test"
        val packagedSealedMessage = pk.seal( testMessage );
        val decryptedBytes = PrivateKey.unseal(seed, packagedSealedMessage )
        val decryptedMessage = String(decryptedBytes)
        assertEquals(testMessage, decryptedMessage)

        val binaryCopy = pk.toSerializedBinaryForm()
        val copy = PublicKey.fromSerializedBinaryForm(binaryCopy)
        assertEquals(copy, pk)

        val binaryCopySk = sk.toSerializedBinaryForm()
        val copySk = PrivateKey.fromSerializedBinaryForm(binaryCopySk)
        assertEquals(copySk, sk)


    }

    @Test
    fun shouldThrowInvalidKeyDerivationOptionValueException() {
        val keyDerivationOptionsJson = """{}"""
        val publicKeyJson = """{
            "ErrorInsteadOfKeyBytes": "000102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e0f",
            "keyDerivationOptionsJson": "$keyDerivationOptionsJson"
        }"""
        try {
            PublicKey.fromJson(publicKeyJson)
            fail()
        } catch (e: InvalidKeyDerivationOptionValueException) {
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
            PublicKey.fromJson(publicKeyJson)
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
        val svk = SignatureVerificationKey.fromJson(signatureVerificationKeyJson)
        assertEquals(0x0f, svk.keyBytes[31].toInt())
        assertEquals(keyDerivationOptionsJson, svk.keyDerivationOptionsJson)
        val copyOfSvk = SignatureVerificationKey(svk.keyBytes, svk.keyDerivationOptionsJson)
        assertEquals(svk, copyOfSvk)

        val bCopy = svk.toSerializedBinaryForm()
        val copy = SignatureVerificationKey.fromSerializedBinaryForm(bCopy)
        assertEquals(copy, svk)
    }

    @Test
    fun testSigningAndVerificationPairs() {
        val keyDerivationOptionsJson = """{"keyType": "Signing"}"""
        val sk = SigningKey.deriveFromSeed(seed, keyDerivationOptionsJson)
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
        val keyDerivationOptionsJson = """{"keyType": "Symmetric"}"""
        val sk = SymmetricKey.deriveFromSeed(seed, keyDerivationOptionsJson)
        val testMessage = "some message to test"
        val packagedSealedMessage = sk.seal(testMessage)
        val decryptedBytes = SymmetricKey.unseal(packagedSealedMessage, seed)
        val decryptedMessage = String(decryptedBytes)
        assertEquals(testMessage, decryptedMessage)

        val binaryCopy = sk.toSerializedBinaryForm()
        val copy = SymmetricKey.fromSerializedBinaryForm(binaryCopy)
        assertArrayEquals(copy.keyBytes, sk.keyBytes)
        assertEquals(copy.keyDerivationOptionsJson, sk.keyDerivationOptionsJson)

    }

    @Test
    fun testSeed() {
        val keyDerivationOptionsJson = """{"keyType": "Seed", "keyLengthInBytes": 48}"""
        val s = Seed.deriveFromSeed(seed, keyDerivationOptionsJson)
        assertEquals(s.seedBytes.size, 48)

        val binaryCopy = s.toSerializedBinaryForm()
        val copy = Seed.fromSerializedBinaryForm(binaryCopy)
        assertArrayEquals(copy.seedBytes, s.seedBytes)
        assertEquals(copy.keyDerivationOptionsJson, s.keyDerivationOptionsJson)
    }


}
