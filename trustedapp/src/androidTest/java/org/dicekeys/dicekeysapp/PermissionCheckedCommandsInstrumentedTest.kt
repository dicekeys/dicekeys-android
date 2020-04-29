package org.dicekeys.dicekeysapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.dicekeys.api.ApiDerivationOptions
import org.dicekeys.api.ClientMayNotRetrieveKeyException
import org.dicekeys.api.ClientPackageNotAuthorizedException
import org.dicekeys.api.PostDecryptionInstructions
import org.dicekeys.crypto.seeded.PackagedSealedMessage
import org.dicekeys.keysqr.Face
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

  val keySqr = Face.keySqrFromHumanReadableForm(
    "A1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1t"
  )

  @Test(expected = ClientPackageNotAuthorizedException::class)
  fun preventsLengthExtensionAttackOnASymmetricSeal() {
    PermissionCheckedCommands(PermissionCheckedSeedAccessor( keySqr, "com.examplespoof"){ true })
      .sealWithSymmetricKey(
        ApiDerivationOptions().apply {
          restrictions = ApiDerivationOptions.Restrictions().apply{
            androidPackagePrefixesAllowed = listOf("com.example")
          }
        }.toJson(),
        "The secret ingredient is sarcasm.".toByteArray(),
        PostDecryptionInstructions().toJson()
      )
  }

  @Test(expected = ClientPackageNotAuthorizedException::class)
  fun preventsLengthExtensionAttackOnASymmetricUnseal() {
    PermissionCheckedCommands(PermissionCheckedSeedAccessor( keySqr, "com.examplespoof"){ true })
      .unsealWithSymmetricKey(PackagedSealedMessage(
        ByteArray(0),
        ApiDerivationOptions().apply {
          restrictions = ApiDerivationOptions.Restrictions().apply{
            androidPackagePrefixesAllowed = listOf("com.example")
          }
        }.toJson(),
        PostDecryptionInstructions().toJson()
      ))
  }

  @Test(expected = ClientPackageNotAuthorizedException::class)
  fun preventsLengthExtensionAttackOnASymmetricUnsealPostDecryptionOptions() {
    val api = PermissionCheckedCommands(PermissionCheckedSeedAccessor( keySqr, "com.examplespoof"){ true })
    val publicKey = api.getSealingKey("")
    val packagedSealedMessage = publicKey.seal("The secret ingredient is eternal despair.",
        PostDecryptionInstructions().apply {
          restrictions = ApiDerivationOptions.Restrictions().apply{
            androidPackagePrefixesAllowed = listOf("com.example")
        }}.toJson()
      )
    val plaintextNotFoundBecauseOfException = api.unsealWithUnsealingKey(packagedSealedMessage)
    Assert.fail()
  }

  @Test(expected = ClientMayNotRetrieveKeyException::class)
  fun preventsPrivateKeySinceDerivationOptionsDoesNotAllowIt() {
    val api = PermissionCheckedCommands(PermissionCheckedSeedAccessor( keySqr, "com.example"){ true })
    val privateKey = api.getUnsealingKey("")
    Assert.fail()
  }

  @Test()
  fun allowsPrivateKeySinceDerivationOptions() {
    val api = PermissionCheckedCommands(PermissionCheckedSeedAccessor( keySqr, "com.example"){ true })
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