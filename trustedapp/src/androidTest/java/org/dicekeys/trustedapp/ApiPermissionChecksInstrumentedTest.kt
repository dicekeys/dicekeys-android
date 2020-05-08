package org.dicekeys.trustedapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CompletableDeferred
import org.dicekeys.api.ApiDerivationOptions
import org.dicekeys.api.ClientPackageNotAuthorizedException
import org.dicekeys.api.UnsealingInstructions
import org.dicekeys.trustedapp.apicommands.permissionchecked.ApiPermissionChecksForPackages
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class ApiPermissionChecksInstrumentedTest {
  private val deferredAllow =
    CompletableDeferred<UnsealingInstructions.RequestForUsersConsent.UsersResponse>(
      UnsealingInstructions.RequestForUsersConsent.UsersResponse.Allow
    )

  @Test
  fun isClientAuthorizedInFaceOfRestrictionsMostlyHarmless() {

    Assert.assertTrue(ApiPermissionChecksForPackages("com.example"){ deferredAllow }
      .doesClientMeetAuthenticationRequirements(
        ApiDerivationOptions().apply {
          androidPackagePrefixesAllowed = listOf("com.example", "com.other")
        }
      ))

    Assert.assertTrue(ApiPermissionChecksForPackages("com.example"){ deferredAllow }
      .doesClientMeetAuthenticationRequirements(
        ApiDerivationOptions().apply {
          androidPackagePrefixesAllowed = listOf("com.example.", "com.other")
        }
      ))

      Assert.assertTrue(ApiPermissionChecksForPackages("com.example."){ deferredAllow }
      .doesClientMeetAuthenticationRequirements(
        ApiDerivationOptions().apply {
          androidPackagePrefixesAllowed = listOf("com.example", "com.other")
        }
      ))

      Assert.assertFalse(ApiPermissionChecksForPackages("com.examplespoof"){ deferredAllow }
      .doesClientMeetAuthenticationRequirements(
        ApiDerivationOptions().apply {
          androidPackagePrefixesAllowed = listOf("com.example", "com.other")
        }
      ))

  }

  @Test(expected = ClientPackageNotAuthorizedException::class)
  fun preventsLengthExtensionAttack() {
    ApiPermissionChecksForPackages("com.examplespoof"){ deferredAllow }
      .throwIfClientNotAuthorized(
        ApiDerivationOptions().apply {
          androidPackagePrefixesAllowed = listOf("com.example", "com.other")
        }
      )
  }

  @Test(expected = ClientPackageNotAuthorizedException::class)
  fun throwsIfAndroidPackagePrefixesNotSet() {
    ApiPermissionChecksForPackages("com.example"){ deferredAllow }
      .throwIfClientNotAuthorized(
        ApiDerivationOptions().apply{
          urlPrefixesAllowed =listOf("https://someplaceotherthanhere.com/")
        }
      )
  }
}