package org.dicekeys.trustedapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.dicekeys.api.ApiDerivationOptions
import org.dicekeys.api.ClientMayNotRetrieveKeyException
import org.dicekeys.api.ClientPackageNotAuthorizedException
import org.dicekeys.api.UnsealingInstructions
import org.dicekeys.crypto.seeded.PackagedSealedMessage
import org.dicekeys.keysqr.DiceKey
import org.dicekeys.trustedapp.apicommands.permissionchecked.ApiPermissionChecksForPackages
import org.dicekeys.trustedapp.apicommands.permissionchecked.PermissionCheckedCommands
import org.dicekeys.trustedapp.apicommands.permissionchecked.PermissionCheckedSeedAccessor
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

  val diceKey = DiceKey.fromHumanReadableForm(
    "A1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1t"
  )

  @Test(expected = ClientPackageNotAuthorizedException::class)
  fun preventsLengthExtensionAttackOnASymmetricSeal() {
    runBlocking {
      PermissionCheckedCommands(PermissionCheckedSeedAccessor(
        ApiPermissionChecksForPackages("com.examplespoof")
        { CompletableDeferred<UnsealingInstructions.RequestForUsersConsent.UsersResponse>(
          UnsealingInstructions.RequestForUsersConsent.UsersResponse.Allow
        ) })
      { CompletableDeferred(diceKey) })
        .sealWithSymmetricKey(
          ApiDerivationOptions().apply {
            restrictions = ApiDerivationOptions.Restrictions().apply {
              androidPackagePrefixesAllowed = listOf("com.example")
            }
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
        ApiPermissionChecksForPackages("com.examplespoof")
        { CompletableDeferred<UnsealingInstructions.RequestForUsersConsent.UsersResponse>(
          UnsealingInstructions.RequestForUsersConsent.UsersResponse.Allow
        ) })
      { CompletableDeferred(diceKey) })
        .unsealWithSymmetricKey(PackagedSealedMessage(
          ByteArray(0),
          ApiDerivationOptions().apply {
            restrictions = ApiDerivationOptions.Restrictions().apply {
              androidPackagePrefixesAllowed = listOf("com.example")
            }
          }.toJson(),
          UnsealingInstructions().toJson()
        ))
    }
  }

  @Test(expected = ClientPackageNotAuthorizedException::class)
  fun preventsLengthExtensionAttackOnASymmetricUnsealPostDecryptionOptions() {
    runBlocking {
      val api = PermissionCheckedCommands(PermissionCheckedSeedAccessor(
        ApiPermissionChecksForPackages("com.examplespoof")
        { CompletableDeferred<UnsealingInstructions.RequestForUsersConsent.UsersResponse>(
          UnsealingInstructions.RequestForUsersConsent.UsersResponse.Allow
        ) })
      { CompletableDeferred(diceKey) })
      val publicKey = api.getSealingKey("")
      val packagedSealedMessage = publicKey.seal("The secret ingredient is eternal despair.",
        UnsealingInstructions().apply {
          restrictions = ApiDerivationOptions.Restrictions().apply {
            androidPackagePrefixesAllowed = listOf("com.example")
          }
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
        ApiPermissionChecksForPackages("com.example")
        { CompletableDeferred<UnsealingInstructions.RequestForUsersConsent.UsersResponse>(
          UnsealingInstructions.RequestForUsersConsent.UsersResponse.Allow
        ) })
      { CompletableDeferred(diceKey) })
      val privateKey = api.getUnsealingKey("")
      Assert.fail()
    }
  }

  @Test()
  fun allowsPrivateKeySinceDerivationOptions() {
    runBlocking {
      val api = PermissionCheckedCommands(PermissionCheckedSeedAccessor(
        ApiPermissionChecksForPackages("com.example")
        { CompletableDeferred<UnsealingInstructions.RequestForUsersConsent.UsersResponse>(
          UnsealingInstructions.RequestForUsersConsent.UsersResponse.Allow
        ) })
      { CompletableDeferred(diceKey) })
      val privateKey = api.getUnsealingKey(
        ApiDerivationOptions().apply {
          clientMayRetrieveKey = true
          restrictions = ApiDerivationOptions.Restrictions().apply {
            androidPackagePrefixesAllowed = listOf("com.example")
          }
        }.toJson()
      )
    }
  }

}