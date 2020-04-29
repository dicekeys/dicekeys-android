package org.dicekeys.dicekeysapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.dicekeys.api.ApiDerivationOptions
import org.dicekeys.api.ClientPackageNotAuthorizedException
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
  @Test
  fun isClientAuthorizedInFaceOfRestrictionsMostlyHarmless() {
    Assert.assertTrue(ApiPermissionChecksForPackages("com.example"){ true }
      .isClientAuthorizedInFaceOfRestrictions(
        ApiDerivationOptions.Restrictions().apply {
          androidPackagePrefixesAllowed = listOf("com.example", "com.other")
        }
      ))

    Assert.assertTrue(ApiPermissionChecksForPackages("com.example"){ true }
      .isClientAuthorizedInFaceOfRestrictions(
        ApiDerivationOptions.Restrictions().apply {
          androidPackagePrefixesAllowed = listOf("com.example.", "com.other")
        }
      ))

      Assert.assertTrue(ApiPermissionChecksForPackages("com.example."){ true }
      .isClientAuthorizedInFaceOfRestrictions(
        ApiDerivationOptions.Restrictions().apply {
          androidPackagePrefixesAllowed = listOf("com.example", "com.other")
        }
      ))

      Assert.assertFalse(ApiPermissionChecksForPackages("com.examplespoof"){ true }
      .isClientAuthorizedInFaceOfRestrictions(
        ApiDerivationOptions.Restrictions().apply {
          androidPackagePrefixesAllowed = listOf("com.example", "com.other")
        }
      ))

  }

  @Test(expected = ClientPackageNotAuthorizedException::class)
  fun preventsLengthExtensionAttack() {
    ApiPermissionChecksForPackages("com.examplespoof"){ true }
      .throwIfClientNotAuthorized(
        ApiDerivationOptions.Restrictions().apply {
          androidPackagePrefixesAllowed = listOf("com.example", "com.other")
        }
      )
  }

  @Test(expected = ClientPackageNotAuthorizedException::class)
  fun throwsIfAndroidPackagePrefixesNotSet() {
    ApiPermissionChecksForPackages("com.example"){ true }
      .throwIfClientNotAuthorized(
        ApiDerivationOptions.Restrictions()
      )
  }
}