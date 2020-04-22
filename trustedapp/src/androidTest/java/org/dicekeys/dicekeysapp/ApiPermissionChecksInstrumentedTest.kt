package org.dicekeys.dicekeysapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.dicekeys.api.ApiKeyDerivationOptions
import org.dicekeys.api.ClientPackageNotAuthorizedException
import org.dicekeys.trustedapp.apicommands.permissionchecked.ApiPermissionChecks
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
  @Test
  fun isClientAuthorizedInFaceOfRestrictionsMostlyHarmless() {
    Assert.assertTrue(ApiPermissionChecks("com.example"){ true }
      .isClientAuthorizedInFaceOfRestrictions(
        ApiKeyDerivationOptions.Restrictions().apply {
          androidPackagePrefixesAllowed = listOf("com.example", "com.other")
        }
      ))

    Assert.assertTrue(ApiPermissionChecks("com.example"){ true }
      .isClientAuthorizedInFaceOfRestrictions(
        ApiKeyDerivationOptions.Restrictions().apply {
          androidPackagePrefixesAllowed = listOf("com.example.", "com.other")
        }
      ))

      Assert.assertTrue(ApiPermissionChecks("com.example."){ true }
      .isClientAuthorizedInFaceOfRestrictions(
        ApiKeyDerivationOptions.Restrictions().apply {
          androidPackagePrefixesAllowed = listOf("com.example", "com.other")
        }
      ))

      Assert.assertFalse(ApiPermissionChecks("com.examplespoof"){ true }
      .isClientAuthorizedInFaceOfRestrictions(
        ApiKeyDerivationOptions.Restrictions().apply {
          androidPackagePrefixesAllowed = listOf("com.example", "com.other")
        }
      ))

  }

  @Test(expected = ClientPackageNotAuthorizedException::class)
  fun preventsLengthExtensionAttack() {
    ApiPermissionChecks("com.examplespoof"){ true }
      .throwIfClientNotAuthorized(
        ApiKeyDerivationOptions.Restrictions().apply {
          androidPackagePrefixesAllowed = listOf("com.example", "com.other")
        }
      )
  }

  @Test(expected = ClientPackageNotAuthorizedException::class)
  fun throwsIfAndroidPackagePrefixesNotSet() {
    ApiPermissionChecks("com.example"){ true }
      .throwIfClientNotAuthorized(
        ApiKeyDerivationOptions.Restrictions()
      )
  }
}