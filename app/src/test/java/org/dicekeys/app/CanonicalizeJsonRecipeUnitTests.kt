package org.dicekeys.app

import org.dicekeys.api.canonicalizeRecipeJson
import org.dicekeys.api.derivationRecipeTemplates
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner

@RunWith(BlockJUnit4ClassRunner::class)
class CanonicalizeJsonRecipeUnitTests {
    companion object{
        val doNotModifyBuiltInRecipeTestCases = derivationRecipeTemplates.map {
            it.recipeJson to it.recipeJson
        }

        val customTestCases = listOf(
            // Order fields correctly
            "{\"#\":3,\"allow\":[{\"host\":\"*.example.com\"}]}" to "{\"allow\":[{\"host\":\"*.example.com\"}],\"#\":3}",

            // Order fields correctly
            "{\"#\":3,\"allow\":[{\"host\":\"*.example.com\"}],\"purpose\":\"Life? Don't talk to me about life!\" }" to "{\"purpose\":\"Life? Don't talk to me about life!\",\"allow\":[{\"host\":\"*.example.com\"}],\"#\":3}",

            // Order fields in sub-object (allow) correctly
            "{\"allow\":[{\"paths\":[\"lo\", \"yo\"],\"host\":\"*.example.com\"}]}" to "{\"allow\":[{\"host\":\"*.example.com\",\"paths\":[\"lo\",\"yo\"]}]}",

            // Remove white space correctly
            " {  \"allow\" : [  {\"host\"\n:\"*.example.com\"}\t ]    }\n\n" to "{\"allow\":[{\"host\":\"*.example.com\"}]}",
        )
    }

    @Test
    fun test_canonicalizeRecipeJson(){
        for (test in doNotModifyBuiltInRecipeTestCases){
            Assert.assertEquals(test.second, canonicalizeRecipeJson(test.first))
        }

        for (test in customTestCases){
            Assert.assertEquals(test.second, canonicalizeRecipeJson(test.first))
        }
    }
}