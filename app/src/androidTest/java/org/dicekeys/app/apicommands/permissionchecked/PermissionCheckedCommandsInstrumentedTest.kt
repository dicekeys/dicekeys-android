package org.dicekeys.app.apicommands.permissionchecked

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import org.dicekeys.api.ApiRecipe
import org.dicekeys.api.ClientMayNotRetrieveKeyException
import org.dicekeys.api.ClientPackageNotAuthorizedException
import org.dicekeys.api.UnsealingInstructions
import org.dicekeys.crypto.seeded.PackagedSealedMessage
import org.dicekeys.dicekey.DiceKey
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class PermissionCheckedCommandsInstrumentedTest {

  private val diceKey = DiceKey.example

  private fun mockRequestUsersConsentAlwaysAsync(
    requestForUsersConsent: UnsealingInstructions.RequestForUsersConsent
  ) : Deferred<UnsealingInstructions.RequestForUsersConsent.UsersResponse> = CompletableDeferred(UnsealingInstructions.RequestForUsersConsent.UsersResponse.Allow)

  @Test(expected = ClientPackageNotAuthorizedException::class)
  fun preventsLengthExtensionAttackOnASymmetricSeal() {
    runBlocking {
      PermissionCheckedCommands(PermissionCheckedSeedAccessor(
        ApiPermissionChecksForPackages("com.examplespoof", ::mockRequestUsersConsentAlwaysAsync)
        )
      { CompletableDeferred(diceKey) })
        .sealWithSymmetricKey(
          ApiRecipe().apply {
            allowAndroidPrefixes = listOf("com.example")
          }.toJson(),
          "The secret ingredient is sarcasm.".toByteArray(),
          UnsealingInstructions().toJson()
        )
    }
  }

  @Test(expected = ClientPackageNotAuthorizedException::class)
  fun preventsLengthExtensionAttackOnASymmetricUnseal() {
    runBlocking {
      PermissionCheckedCommands(PermissionCheckedSeedAccessor(
        ApiPermissionChecksForPackages("com.examplespoof", ::mockRequestUsersConsentAlwaysAsync)
      )
      { CompletableDeferred(diceKey) })
        .unsealWithSymmetricKey(PackagedSealedMessage(
          ByteArray(0),
          ApiRecipe().apply {
            allowAndroidPrefixes = listOf("com.example")
          }.toJson(),
          UnsealingInstructions().toJson()
        ))
    }
  }

  @Test(expected = ClientPackageNotAuthorizedException::class)
  fun preventsLengthExtensionAttackOnASymmetricUnsealPostDecryptionOptions() {
    runBlocking {
      val api = PermissionCheckedCommands(PermissionCheckedSeedAccessor(
        ApiPermissionChecksForPackages("com.examplespoof", ::mockRequestUsersConsentAlwaysAsync)
      )
      { CompletableDeferred(diceKey) })
      val publicKey = api.getSealingKey("")
      val packagedSealedMessage = publicKey.seal("The secret ingredient is eternal despair.",
        UnsealingInstructions().apply {
          allowAndroidPrefixes = listOf("com.example")
        }.toJson()
      )
      val plaintextNotFoundBecauseOfException = api.unsealWithUnsealingKey(packagedSealedMessage)
      Assert.fail()
    }
  }

  @Test(expected = ClientMayNotRetrieveKeyException::class)
  fun preventsPrivateKeySinceDerivationOptionsDoesNotAllowIt() {
    runBlocking {
      val api = PermissionCheckedCommands(PermissionCheckedSeedAccessor(
        ApiPermissionChecksForPackages("com.example", ::mockRequestUsersConsentAlwaysAsync)
      )
      { CompletableDeferred(diceKey) })
      val privateKey = api.getUnsealingKey("")
      Assert.fail()
    }
  }

  @Test()
  fun allowsPrivateKeySinceDerivationOptions() {
    runBlocking {
      val api = PermissionCheckedCommands(PermissionCheckedSeedAccessor(
        ApiPermissionChecksForPackages("com.example", ::mockRequestUsersConsentAlwaysAsync)
        )
      { CompletableDeferred(diceKey) })
      val privateKey = api.getUnsealingKey(
        ApiRecipe().apply {
          clientMayRetrieveKey = true
          allowAndroidPrefixes = listOf("com.example")
        }.toJson()
      )
    }
  }

}