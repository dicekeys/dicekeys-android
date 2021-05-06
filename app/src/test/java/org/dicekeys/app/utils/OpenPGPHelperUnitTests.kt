package org.dicekeys.app.utils

import org.dicekeys.app.extensions.hexStringToByteArray
import org.dicekeys.app.extensions.toHex
import org.dicekeys.app.openpgp.PublicPacket
import org.dicekeys.app.openpgp.SecretPacket
import org.dicekeys.app.openpgp.SignaturePacket
import org.dicekeys.app.openpgp.UserIdPacket
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class OpenPGPHelperUnitTests {

    private val privateKey1 = "58CBA8496EABC3D58F84C034448EF1C1F95C9C6582E006C2BB205B70EB58D5CF".hexStringToByteArray()
    private val publicKey1 = "71F0631525A110B2A6046D4C1DCF0A2B8B8CD9DEF1E773DB408165A747A2E3E8".hexStringToByteArray()
    private val timestamp1 = 0x60844560

    @Test
    fun testSecretKeyPacket(){
        val pgpBinary = "9458046084456016092B06010401DA470F0101074071F0631525A110B2A6046D4C1DCF0A2B8B8CD9DEF1E773DB408165A747A2E3E80000FF58CBA8496EABC3D58F84C034448EF1C1F95C9C6582E006C2BB205B70EB58D5CF135C".hexStringToByteArray()

        val secretPacket = SecretPacket(privateKey1, timestamp1)

        Assert.assertEquals(pgpBinary.toHex(), secretPacket.toByteArray().toHex())
    }

    @Test
    fun testPublicKeyPacket(){
        val pgpBinary = "9833046084456016092B06010401DA470F0101074071F0631525A110B2A6046D4C1DCF0A2B8B8CD9DEF1E773DB408165A747A2E3E8".hexStringToByteArray()
        val publicPacket = PublicPacket(publicKey1, timestamp1)

        Assert.assertEquals(pgpBinary.toHex(), publicPacket.toByteArray().toHex())
    }


    @Test
    fun testUserPacket(){
        val pgpBinary = "B420444B5F555345525F31203C646B757365723140646963656B6579732E6F72673E".hexStringToByteArray()

        val userPacket = UserIdPacket("DK_USER_1", "dkuser1@dicekeys.org")

        Assert.assertEquals(pgpBinary.toHex(), userPacket.toByteArray().toHex())
    }

    @Test
    fun testFingerprint(){
        val fingerprint = "FBE62AB5DC8C41B12C06F37E85B7A357B0E9FFD8"

        val userPacket = PublicPacket(publicKey1, timestamp1)

        Assert.assertEquals(fingerprint, userPacket.fingerprint().toHex())
    }

    @Test
    fun testSignaturePacket(){
        val pgpBinary = "8890041316080038162104FBE62AB5DC8C41B12C06F37E85B7A357B0E9FFD8050260844560021B01050B0908070206150A09080B020416020301021E01021780000A091085B7A357B0E9FFD865EB0100C5A77D28D9623C74B493A7A5E72ABF24F34C4E133DA85E314C6105B06A4E26AF0100A6EC13920C8023FC0444705D4F32A55B977EC147BE3B6B68F112601A52B6730A".hexStringToByteArray()

        val userPacket = UserIdPacket("DK_USER_1", "dkuser1@dicekeys.org")

        val signaturePacket = SignaturePacket(privateKey1, timestamp1, userPacket)

        Assert.assertEquals(pgpBinary.toHex(), signaturePacket.toByteArray().toHex())
    }
}
