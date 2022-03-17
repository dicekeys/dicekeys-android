package org.dicekeys.app.apicommands.permissionchecked

import android.net.Uri
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import org.dicekeys.api.*
import org.dicekeys.app.MainActivity
import org.dicekeys.dicekey.DiceKey
import org.junit.*
import org.junit.runner.RunWith
import java.net.URLEncoder

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class PermissionCheckedUrlCommandsUnitTests {
    private fun mockLoadDiceKeyAsync() = CompletableDeferred(DiceKey.example)

    private fun mockRequestUsersConsentAlwaysAsync(requestForUsersConsent: UnsealingInstructions.RequestForUsersConsent) = CompletableDeferred(UnsealingInstructions.RequestForUsersConsent.UsersResponse.Allow)

    private val allowTestCases = listOf(
        "google.com" to "google.com",
        "google.com" to "*.google.com",
    )

    private val evilTestCases = listOf(
        "evil.com" to "google.com",
        "evil.com" to "*.google.com",
    )

    @Test
    fun allowTestCases() {
        runBlocking {
            allowTestCases.forEach {
                PermissionCheckedCommands(
                    PermissionCheckedSeedAccessor(
                        ApiPermissionChecksForUrls(
                            replyToUrlString = "https://${it.first}/--derived-secret-api--/*",
                            handshakeAuthenticatedUrlString = null,
                            ::mockRequestUsersConsentAlwaysAsync
                        ),
                        ::mockLoadDiceKeyAsync
                    )
                ).getPassword(
                    ApiRecipe().apply {
                        allow = listOf(WebBasedApplicationIdentity(it.second, null))
                    }.toJson()
                )
            }
        }
    }

    @Test(expected = ClientUriNotAuthorizedException::class)
    fun evilTestCases() {
        runBlocking {
            evilTestCases.forEach {
                PermissionCheckedCommands(
                    PermissionCheckedSeedAccessor(
                        ApiPermissionChecksForUrls(
                            replyToUrlString = "https://${it.first}/--derived-secret-api--/*",
                            handshakeAuthenticatedUrlString = null,
                            ::mockRequestUsersConsentAlwaysAsync
                        ),
                        ::mockLoadDiceKeyAsync
                    )
                ).getPassword(
                    ApiRecipe().apply {
                        allow = listOf(WebBasedApplicationIdentity(it.second, null))
                    }.toJson()
                )
                Assert.fail("No exception thrown")
            }
        }
    }
}