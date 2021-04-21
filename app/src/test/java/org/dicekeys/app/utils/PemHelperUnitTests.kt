package org.dicekeys.app.utils

import com.google.common.io.BaseEncoding
import org.dicekeys.app.extensions.hexStringToByteArray
import org.dicekeys.app.extensions.toHex
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class PemHelperUnitTests {



    @Test
    fun createTemplate(){
//        val pubKey = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF".hexStringToByteArray()
//
//        println(OpenSslHelper.marshalPublicKeyEd25519(pubKey).toHex())

        val privateKey = "05AD7768A6BF76BACF11CD6E958685C2921A2D0A1F7B3313CB66FA71382FCF41".hexStringToByteArray()
        val publicKey = "C953742F5D7A26111D868FBBAD228C9C180524FD1743891A2796D49F4735FD3D".hexStringToByteArray()

        println(OpenSslHelper.marshalPrivateKeyEd25519(privateKey, publicKey, "DiceKeys").toHex())

        println(BaseEncoding.base64().encode(OpenSslHelper.marshalPrivateKeyEd25519(privateKey, publicKey, "DiceKeys")))

//        BaseEncoding.base64().encode()

//        Assert.assertEquals(
//                "b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAMwAAAAtzc2gtZWQyNTUxOQAAACDJU3QvXXomER2Gj7utIoycGAUk/RdDiRonltSfRzX9PQAAAJAQPWDDED1gwwAAAAtzc2gtZWQyNTUxOQAAACDJU3QvXXomER2Gj7utIoycGAUk/RdDiRonltSfRzX9PQAAAEAFrXdopr92us8RzW6VhoXCkhotCh97MxPLZvpxOC/PQclTdC9deiYRHYaPu60ijJwYBST9F0OJGieW1J9HNf09AAAACERpY2VLZXlzAQIDBAU=",
//                BaseEncoding.base64().encode( OpenSslHelper.marshalPrivateKeyEd25519(privateKey, publicKey, "DiceKeys")))
    }

    @Test
    fun testAuthorizedKey(){
        val pubKey = "C953742F5D7A26111D868FBBAD228C9C180524FD1743891A2796D49F4735FD3D".hexStringToByteArray()
        println(OpenSslHelper.marshalPublicKeyEd25519(pubKey).toHex())
        Assert.assertEquals("ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIMlTdC9deiYRHYaPu60ijJwYBST9F0OJGieW1J9HNf09 DiceKeys", OpenSslHelper.marshalAuthorizedPublicKeyEd25519(pubKey))
    }

    @Test
    fun testPrivateKeyd(){

        val privateKey = "05AD7768A6BF76BACF11CD6E958685C2921A2D0A1F7B3313CB66FA71382FCF41".hexStringToByteArray()
        val publicKey = "C953742F5D7A26111D868FBBAD228C9C180524FD1743891A2796D49F4735FD3D".hexStringToByteArray()
        Assert.assertEquals(
                "b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAMwAAAAtzc2gtZWQyNTUxOQAAACDJU3QvXXomER2Gj7utIoycGAUk/RdDiRonltSfRzX9PQAAAJAQPWDDED1gwwAAAAtzc2gtZWQyNTUxOQAAACDJU3QvXXomER2Gj7utIoycGAUk/RdDiRonltSfRzX9PQAAAEAFrXdopr92us8RzW6VhoXCkhotCh97MxPLZvpxOC/PQclTdC9deiYRHYaPu60ijJwYBST9F0OJGieW1J9HNf09AAAACERpY2VLZXlzAQIDBAU=",
                BaseEncoding.base64().encode( OpenSslHelper.marshalPrivateKeyEd25519(privateKey, publicKey, "DiceKeys")))
    }

    @Test
    fun testPrivateKey(){
        val pem = PemHelper.block("OPENSSH PRIVATE KEY", "b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAMwAAAAtzc2gtZWQyNTUxOQAAACDJU3QvXXomER2Gj7utIoycGAUk/RdDiRonltSfRzX9PQAAAJAQPWDDED1gwwAAAAtzc2gtZWQyNTUxOQAAACDJU3QvXXomER2Gj7utIoycGAUk/RdDiRonltSfRzX9PQAAAEAFrXdopr92us8RzW6VhoXCkhotCh97MxPLZvpxOC/PQclTdC9deiYRHYaPu60ijJwYBST9F0OJGieW1J9HNf09AAAACERpY2VLZXlzAQIDBAU=")
println(pem)
        Assert.assertEquals("-----BEGIN OPENSSH PRIVATE KEY-----\n" +
                "b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAMwAAAAtz\n" +
                "c2gtZWQyNTUxOQAAACDJU3QvXXomER2Gj7utIoycGAUk/RdDiRonltSfRzX9PQAA\n" +
                "AJAQPWDDED1gwwAAAAtzc2gtZWQyNTUxOQAAACDJU3QvXXomER2Gj7utIoycGAUk\n" +
                "/RdDiRonltSfRzX9PQAAAEAFrXdopr92us8RzW6VhoXCkhotCh97MxPLZvpxOC/P\n" +
                "QclTdC9deiYRHYaPu60ijJwYBST9F0OJGieW1J9HNf09AAAACERpY2VLZXlzAQID\n" +
                "BAU=\n" +
                "-----END OPENSSH PRIVATE KEY-----\n", pem)
    }
}
