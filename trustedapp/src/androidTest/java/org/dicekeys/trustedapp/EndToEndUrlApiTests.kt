package org.dicekeys.trustedapp

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import org.dicekeys.api.Api
import org.dicekeys.api.UnsealingInstructions
import org.dicekeys.api.DiceKeysWebApiClient
import org.dicekeys.keysqr.DiceKey
import org.dicekeys.keysqr.KeySqr
import org.dicekeys.trustedapp.apicommands.permissionchecked.PermissionCheckedUrlCommands
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class EndToEndUrlApiTests {
  private fun mockLoadDiceKeyAsync(): Deferred<DiceKey> = CompletableDeferred<DiceKey>().apply {
    complete(KeySqr.fromHumanReadableForm(
      "A1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1t"))
  }

  private fun mockRequestUsersConsentAlwaysAsync(
    requestForUsersConsent: UnsealingInstructions.RequestForUsersConsent
  ) : Deferred<UnsealingInstructions.RequestForUsersConsent.UsersResponse> =
    CompletableDeferred<UnsealingInstructions.RequestForUsersConsent.UsersResponse>().apply {
      complete(UnsealingInstructions.RequestForUsersConsent.UsersResponse.Allow)
    }

  private fun mockApiServerCall(
    requestUri: Uri,
    sendResponse: (Uri) -> Unit
  ) = PermissionCheckedUrlCommands(
    requestUri, ::mockLoadDiceKeyAsync, ::mockRequestUsersConsentAlwaysAsync, sendResponse
  )

  private val api : Api get() {
    var mockedWebApi : DiceKeysWebApiClient? = null
    mockedWebApi = DiceKeysWebApiClient(
      "https://myapp/apiresponse/"
    ) { requestUri ->
      runBlocking {
        mockApiServerCall(requestUri) { responseUri -> mockedWebApi?.handleResult(responseUri) }
          .executeCommand()
      }
    }
    return mockedWebApi!! as Api
  }

  private val derivationOptionsJson = "{}"
  private val testMessage = "The secret ingredient is dihydrogen monoxide"
  private val testMessageByteArray = testMessage.toByteArray(Charsets.UTF_8)

  @Test
  fun symmetricKeySealAndUnseal()
  {

    runBlocking {
      val packagedSealedMessage = api.sealWithSymmetricKey(
        derivationOptionsJson,
        testMessageByteArray
      )
      val plaintext = api.unsealWithSymmetricKey(packagedSealedMessage)
      Assert.assertArrayEquals(plaintext, testMessageByteArray)
    }
  }

  @Test
  fun signAndVerify() { runBlocking {
    val sig = api.generateSignature(derivationOptionsJson, testMessageByteArray)
    val signatureVerificationKey = api.getSignatureVerificationKey(derivationOptionsJson)
    Assert.assertArrayEquals(signatureVerificationKey.keyBytes, sig.signatureVerificationKey.keyBytes)
    Assert.assertTrue(signatureVerificationKey.verifySignature(testMessageByteArray, sig.signature))
    Assert.assertFalse(signatureVerificationKey.verifySignature(ByteArray(0), sig.signature))
  }}

  @Test
  fun asymmetricSealAndUnseal() { runBlocking {
    val publicKey = api.getSealingKey(derivationOptionsJson)
    val packagedSealedPkMessage = publicKey.seal(testMessageByteArray, """{
           |  "requireUsersConsent": {
           |     "question": "Do you want use \"8fsd8pweDmqed\" as your SpoonerMail account password and remove your current password?",
           |     "actionButtonLabels": {
           |         "allow": "Make my password \"8fsd8pweDmqed\"",
           |         "deny": "No"
           |     }
           |  }
           |}""".trimMargin())
    val plaintext = api.unsealWithUnsealingKey(packagedSealedPkMessage)
    Assert.assertArrayEquals(plaintext, testMessageByteArray)
  }}

}