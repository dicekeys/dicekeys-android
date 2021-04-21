package org.dicekeys.app.utils

import org.dicekeys.app.extensions.hexStringToByteArray
import org.dicekeys.app.extensions.toHex
import org.dicekeys.app.openpgp.*
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

class TestVector(val privateKey: ByteArray,
                      val publicKey: ByteArray,
                      val timestamp: UInt,

                      val name: String,
                      val email: String,

                      val fingerprint: String,
                      val publicPacketBinary: String,
                      val secretPacketBinary: String,
                      val userIdPacketBinary: String,
                      val signaturePacketBinary: String,
)

@RunWith(MockitoJUnitRunner::class)
class OpenPGPUnitTests {

    private val testData = listOf(
        TestVector(privateKey = "58CBA8496EABC3D58F84C034448EF1C1F95C9C6582E006C2BB205B70EB58D5CF".hexStringToByteArray(),
            publicKey = "71F0631525A110B2A6046D4C1DCF0A2B8B8CD9DEF1E773DB408165A747A2E3E8".hexStringToByteArray(),
            timestamp = 0x60844560u,

            name = "DK_USER_1",
            email = "dkuser1@dicekeys.org",

            fingerprint = "FBE62AB5DC8C41B12C06F37E85B7A357B0E9FFD8",
            publicPacketBinary = "9833046084456016092B06010401DA470F0101074071F0631525A110B2A6046D4C1DCF0A2B8B8CD9DEF1E773DB408165A747A2E3E8",
            secretPacketBinary = "9458046084456016092B06010401DA470F0101074071F0631525A110B2A6046D4C1DCF0A2B8B8CD9DEF1E773DB408165A747A2E3E80000FF58CBA8496EABC3D58F84C034448EF1C1F95C9C6582E006C2BB205B70EB58D5CF135C",
            userIdPacketBinary = "B420444B5F555345525F31203C646B757365723140646963656B6579732E6F72673E",
            signaturePacketBinary = "8890041316080038162104FBE62AB5DC8C41B12C06F37E85B7A357B0E9FFD8050260844560021B01050B0908070206150A09080B020416020301021E01021780000A091085B7A357B0E9FFD865EB0100C5A77D28D9623C74B493A7A5E72ABF24F34C4E133DA85E314C6105B06A4E26AF0100A6EC13920C8023FC0444705D4F32A55B977EC147BE3B6B68F112601A52B6730A"
        ),

        TestVector(privateKey = "F741CC9AC284484A9282152E36CDEE239EBA572F5C258979C9657AA3F7E95EBC".hexStringToByteArray(),
            publicKey = "207E2C90C6F41BCC055CD939DF50575E9BD77F1BFAAD6F85BE1058FFB6AEDBDF".hexStringToByteArray(),
            timestamp = 0x6084459bu,

            name = "DK_USER_2",
            email = "dkuser2____@dicekeys.com",

            fingerprint = "4F98C213FCBBBD97004A4473E99D26BEB59B3C9A",
            publicPacketBinary = "9833046084459B16092B06010401DA470F01010740207E2C90C6F41BCC055CD939DF50575E9BD77F1BFAAD6F85BE1058FFB6AEDBDF",
            secretPacketBinary = "9458046084459B16092B06010401DA470F01010740207E2C90C6F41BCC055CD939DF50575E9BD77F1BFAAD6F85BE1058FFB6AEDBDF000100F741CC9AC284484A9282152E36CDEE239EBA572F5C258979C9657AA3F7E95EBC1088",
            userIdPacketBinary = "B424444B5F555345525F32203C646B75736572325F5F5F5F40646963656B6579732E636F6D3E",
            signaturePacketBinary = "88900413160800381621044F98C213FCBBBD97004A4473E99D26BEB59B3C9A05026084459B021B01050B0908070206150A09080B020416020301021E01021780000A0910E99D26BEB59B3C9A485400FE298973EBB860EF016F581FAD2C80226C05056C3B6B6710B7AD20BE06DE22F7820100EE4FC5C1C204F897FACF9CF4973C052C7E703EE658F97C5CAEF99C09CE27E700"
        ),
    )

    @Test
    fun testMpi(){
        var mpi : Mpi = Mpi.fromHex("01")
        Assert.assertEquals("000101", mpi.toByteArray().toHex())
        Assert.assertEquals(0x01, mpi.size.toInt())

        mpi = Mpi.fromHex("01FF")
        Assert.assertEquals("000901FF", mpi.toByteArray().toHex())
        Assert.assertEquals(0x09, mpi.size.toInt())

        mpi = Mpi.fromHex("0000000000")
        Assert.assertEquals("0000", mpi.toByteArray().toHex())
        Assert.assertEquals(0x00, mpi.size.toInt())

        mpi = Mpi.fromHex("58CBA8496EABC3D58F84C034448EF1C1F95C9C6582E006C2BB205B70EB58D5CF")
        Assert.assertEquals("00FF58CBA8496EABC3D58F84C034448EF1C1F95C9C6582E006C2BB205B70EB58D5CF", mpi.toByteArray().toHex())
        Assert.assertEquals(0xff, mpi.size.toInt())

        mpi = Mpi.fromHex("F741CC9AC284484A9282152E36CDEE239EBA572F5C258979C9657AA3F7E95EBC")
        Assert.assertEquals("0100F741CC9AC284484A9282152E36CDEE239EBA572F5C258979C9657AA3F7E95EBC", mpi.toByteArray().toHex())
        Assert.assertEquals(0x0100, mpi.size.toInt())
    }

    @Test
    fun testSecretKeyPacket(){
        for(test in testData){
            val secretPacket = SecretPacket(test.privateKey, test.timestamp)
            Assert.assertEquals(test.secretPacketBinary, secretPacket.toByteArray().toHex())
        }
    }

    @Test
    fun testPublicKeyPacket(){
        for(test in testData) {
            val publicPacket = PublicPacket(test.publicKey, test.timestamp)
            Assert.assertEquals(test.publicPacketBinary, publicPacket.toByteArray().toHex())
        }
    }

    @Test
    fun testUserPacket(){
        for(test in testData) {
            val userPacket = UserIdPacket(test.name, test.email)
            Assert.assertEquals(test.userIdPacketBinary, userPacket.toByteArray().toHex())
        }
    }

    @Test
    fun testFingerprint(){
        for(test in testData) {
            val publicPacket = PublicPacket(test.publicKey, test.timestamp)
            Assert.assertEquals(test.fingerprint, publicPacket.fingerprint().toHex())
        }
    }

    @Test
    fun testSignaturePacket(){
        for(test in testData) {
            val userPacket = UserIdPacket(test.name, test.email)
            val signaturePacket = SignaturePacket(test.privateKey, test.timestamp, userPacket)
            Assert.assertEquals(test.signaturePacketBinary, signaturePacket.toByteArray().toHex())
        }
    }

    @Test
    fun testPemExport() {
        val pem = OpenPgpHelper.generateOpenPgpKey(testData[0].privateKey, testData[0].name, testData[0].email, testData[0].timestamp)

        Assert.assertEquals("-----BEGIN PGP PRIVATE KEY BLOCK-----\n" +
                "lFgEYIRFYBYJKwYBBAHaRw8BAQdAcfBjFSWhELKmBG1MHc8KK4uM2d7x53PbQIFl\n" +
                "p0ei4+gAAP9Yy6hJbqvD1Y+EwDREjvHB+VycZYLgBsK7IFtw61jVzxNctCBES19V\n" +
                "U0VSXzEgPGRrdXNlcjFAZGljZWtleXMub3JnPoiQBBMWCAA4FiEE++YqtdyMQbEs\n" +
                "BvN+hbejV7Dp/9gFAmCERWACGwEFCwkIBwIGFQoJCAsCBBYCAwECHgECF4AACgkQ\n" +
                "hbejV7Dp/9hl6wEAxad9KNliPHS0k6el5yq/JPNMThM9qF4xTGEFsGpOJq8BAKbs\n" +
                "E5IMgCP8BERwXU8ypVuXfsFHvjtraPESYBpStnMK\n" +
                "-----END PGP PRIVATE KEY BLOCK-----\n", pem)
    }
}
