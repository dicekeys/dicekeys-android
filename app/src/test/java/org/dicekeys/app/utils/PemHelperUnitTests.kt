package org.dicekeys.app.utils

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class PemHelperUnitTests {
    @Test
    fun test_privateKey_pemBlock(){
        val pem = PemHelper.block("OPENSSH PRIVATE KEY", "b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAMwAAAAtzc2gtZWQyNTUxOQAAACDJU3QvXXomER2Gj7utIoycGAUk/RdDiRonltSfRzX9PQAAAJAQPWDDED1gwwAAAAtzc2gtZWQyNTUxOQAAACDJU3QvXXomER2Gj7utIoycGAUk/RdDiRonltSfRzX9PQAAAEAFrXdopr92us8RzW6VhoXCkhotCh97MxPLZvpxOC/PQclTdC9deiYRHYaPu60ijJwYBST9F0OJGieW1J9HNf09AAAACERpY2VLZXlzAQIDBAU=")

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
