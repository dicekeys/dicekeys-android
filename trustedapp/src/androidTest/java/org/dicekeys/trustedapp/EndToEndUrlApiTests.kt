package org.dicekeys.trustedapp

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import org.dicekeys.api.*
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face
import org.dicekeys.trustedapp.apicommands.permissionchecked.PermissionCheckedUrlCommands
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class EndToEndUrlApiTests {
  private fun mockLoadDiceKeyAsync(): Deferred<DiceKey<Face>> = CompletableDeferred<DiceKey<Face>>().apply {
    complete(DiceKey.fromHumanReadableForm(
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

  private val apiUrlString: String = "https://dicekeys.app/"
  private val respondToUrlString: String = "https://my.app/--derived-secret-api--/"
  private val api : Api get() {
    var mockedWebApi : DiceKeysWebApiClient? = null
    mockedWebApi = DiceKeysWebApiClient(apiUrlString, respondToUrlString) { requestUri ->
      runBlocking {
        mockApiServerCall(requestUri) { responseUri -> mockedWebApi?.handleResult(responseUri) }
          .executeCommand()
      }
    }
    return mockedWebApi!!
  }

  private val derivationOptionsJson = "{ \"allow\": [{\"host\": \"my.app\"}] }"
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
    val publicKey = api.getSealingKey("")
    val packagedSealedPkMessage = publicKey.seal(testMessageByteArray, "{ \"allow\": [{\"host\": \"my.app\"}] }")
    val plaintext = api.unsealWithUnsealingKey(packagedSealedPkMessage)
    Assert.assertArrayEquals(plaintext, testMessageByteArray)
  }}

  @Test
  fun getSecretWithHandshake() { runBlocking {
    val derivationOptions = ApiDerivationOptions().apply {
      requireAuthenticationHandshake = true
      allow = listOf(WebBasedApplicationIdentity("my.app", null))
      lengthInBytes = 13
    }
    val secret = api.getSecret(derivationOptions.toJson())
    Assert.assertEquals(13, secret.secretBytes.size)
  }}

}