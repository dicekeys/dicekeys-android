package org.dicekeys.app.openssh

import com.google.common.io.BaseEncoding
import org.dicekeys.app.extensions.hexStringToByteArray
import org.dicekeys.app.utils.PemHelper
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class OpenSshHelperUnitTests {
    private val privateKey = "05AD7768A6BF76BACF11CD6E958685C2921A2D0A1F7B3313CB66FA71382FCF41".hexStringToByteArray()
    private val publicKey = "C953742F5D7A26111D868FBBAD228C9C180524FD1743891A2796D49F4735FD3D".hexStringToByteArray()

    @Test
    fun test_authorizedKey(){
        Assert.assertEquals("ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIMlTdC9deiYRHYaPu60ijJwYBST9F0OJGieW1J9HNf09 DiceKeys", OpenSslHelper.createAuthorizedPublicKeyEd25519(publicKey))
    }

    @Test
    fun test_privateKey(){
        val checksum = 0x103D60C3
        Assert.assertEquals(
                "b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAMwAAAAtzc2gtZWQyNTUxOQAAACDJU3QvXXomER2Gj7utIoycGAUk/RdDiRonltSfRzX9PQAAAJAQPWDDED1gwwAAAAtzc2gtZWQyNTUxOQAAACDJU3QvXXomER2Gj7utIoycGAUk/RdDiRonltSfRzX9PQAAAEAFrXdopr92us8RzW6VhoXCkhotCh97MxPLZvpxOC/PQclTdC9deiYRHYaPu60ijJwYBST9F0OJGieW1J9HNf09AAAACERpY2VLZXlzAQIDBAU=",
                BaseEncoding.base64().encode(OpenSslHelper.createPrivateKeyEd25519(privateKey, "DiceKeys", checksum)))
    }
}
