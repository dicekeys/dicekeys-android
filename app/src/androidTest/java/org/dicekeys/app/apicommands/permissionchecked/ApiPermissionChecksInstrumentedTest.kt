package org.dicekeys.app.apicommands.permissionchecked

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CompletableDeferred
import org.dicekeys.api.ApiRecipe
import org.dicekeys.api.ClientPackageNotAuthorizedException
import org.dicekeys.api.UnsealingInstructions
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
  private val deferredAllow = CompletableDeferred(UnsealingInstructions.RequestForUsersConsent.UsersResponse.Allow)

  @Test
  fun isClientAuthorizedInFaceOfRestrictionsMostlyHarmless() {

    Assert.assertTrue(ApiPermissionChecksForUrls("com.example", null){ deferredAllow }
      .doesClientMeetAuthenticationRequirements(
        ApiRecipe().apply {
          allowAndroidPrefixes = listOf("com.example", "com.other")
        }
      ))

    Assert.assertTrue(ApiPermissionChecksForUrls("com.example", null){ deferredAllow }
      .doesClientMeetAuthenticationRequirements(
        ApiRecipe().apply {
          allowAndroidPrefixes = listOf("com.example.", "com.other")
        }
      ))

      Assert.assertTrue(ApiPermissionChecksForUrls("com.example.", null){ deferredAllow }
      .doesClientMeetAuthenticationRequirements(
        ApiRecipe().apply {
          allowAndroidPrefixes = listOf("com.example", "com.other")
        }
      ))

      Assert.assertFalse(ApiPermissionChecksForUrls("com.examplespoof", null){ deferredAllow }
      .doesClientMeetAuthenticationRequirements(
        ApiRecipe().apply {
          allowAndroidPrefixes = listOf("com.example", "com.other")
        }
      ))

  }

  @Test(expected = ClientPackageNotAuthorizedException::class)
  fun preventsLengthExtensionAttack() {
    ApiPermissionChecksForUrls("com.examplespoof", null){ deferredAllow }
      .throwIfClientNotAuthorized(
        ApiRecipe().apply {
          allowAndroidPrefixes = listOf("com.example", "com.other")
        }
      )
  }

  @Test(expected = ClientPackageNotAuthorizedException::class)
  fun throwsIfAndroidPackagePrefixesNotSet() {
    ApiPermissionChecksForUrls("com.example", null){ deferredAllow }
      .throwIfClientNotAuthorized(
        ApiRecipe().apply{
          allowAndroidPrefixes = listOf("https://someplaceotherthanhere.com/")
        }
      )
  }
}