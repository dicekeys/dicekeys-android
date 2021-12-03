package org.dicekeys.app

import org.dicekeys.api.getWildcardOfRegisteredDomainFromCandidateWebUrl
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner

@RunWith(BlockJUnit4ClassRunner::class)
class DomainUnitTests {
    companion object{
        val testCases = listOf(
            "https://account.google.com" to "*.google.com",
            "https://google.com" to "*.google.com",
            "https://dicekeys.app" to "*.dicekeys.app",
            "https://api.dicekeys.com" to "*.dicekeys.com",
            "https://vault.bitwarden.com" to "*.bitwarden.com",
            "http://account.google.com" to "*.google.com",
            "http://google.com" to "*.google.com",
            "http://dicekeys.app" to "*.dicekeys.app",
            "http://api.dicekeys.com" to "*.dicekeys.com",
            "http://vault.bitwarden.com" to "*.bitwarden.com",
            "http://www.amazon.co.uk" to "*.amazon.co.uk",
            "http://sub1.amazon.co.uk" to "*.amazon.co.uk",
            "http://sub1.sub2.amazon.co.uk" to "*.amazon.co.uk"
        )
    }

    @Test
    fun test_getWildcardOfRegisteredDomainFromCandidateWebUrl(){
        for (test in testCases){
            println(getWildcardOfRegisteredDomainFromCandidateWebUrl(test.first))
            Assert.assertEquals(test.second, getWildcardOfRegisteredDomainFromCandidateWebUrl(test.first))
        }
    }
}