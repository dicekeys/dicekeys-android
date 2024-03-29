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
            "{\"#\":3,\"allow\":[{\"host\":\"*.example.com\"}]}" to "{\"allow\":[{\"host\":\"*.example.com\"}],\"#\":3}",
            // Order fields correctly
            "{\"#\":3,\"allow\":[{\"host\":\"*.example.com\"}],\"purpose\":\"Life? Don't talk to me about life!\" }" to "{\"purpose\":\"Life? Don't talk to me about life!\",\"allow\":[{\"host\":\"*.example.com\"}],\"#\":3}",
            // Order fields in sub-object (allow) correctly
            "{\"allow\":[{\"paths\":[\"lo\", \"yo\"],\"host\":\"*.example.com\"}]}" to "{\"allow\":[{\"host\":\"*.example.com\",\"paths\":[\"lo\",\"yo\"]}]}",
            // Remove white space correctly
            " {  \"allow\" : [  {\"host\"${"\n"}:\"*.example.com\"}${"\t"} ]    }${"\n\n"}" to "{\"allow\":[{\"host\":\"*.example.com\"}]}",
            // Lots of fields to order correctly, including an empty object with all-caps field name
            "{\"allow\":[{\"paths\":[\"lo\", \"yo\"],\"host\":\"*.example.com\"}],\"#\":3, \"purpose\":\"Don't know\", \"lengthInChars\":3, \"lengthInBytes\": 15, \"UNANTICIPATED_CAPITALIZED_FIELD\":{}}" to "{\"purpose\":\"Don't know\",\"UNANTICIPATED_CAPITALIZED_FIELD\":{},\"allow\":[{\"host\":\"*.example.com\",\"paths\":[\"lo\",\"yo\"]}],\"lengthInBytes\":15,\"lengthInChars\":3,\"#\":3}",
            // Lots of fields to order correctly, including an empty array with all-caps field name
            "{\"allow\":[{\"paths\":[\"lo\", \"yo\"],\"host\":\"*.example.com\"}],\"#\":3, \"purpose\":\"Don't know\", \"lengthInChars\":3, \"lengthInBytes\": 15, \"UNANTICIPATED_CAPITALIZED_FIELD\":[ ] }" to "{\"purpose\":\"Don't know\",\"UNANTICIPATED_CAPITALIZED_FIELD\":[],\"allow\":[{\"host\":\"*.example.com\",\"paths\":[\"lo\",\"yo\"]}],\"lengthInBytes\":15,\"lengthInChars\":3,\"#\":3}",
            // objects and array parsing
            "{ \"silly\":[{\"pointless\":[ \"spacing in\", \"out\"]}]}" to "{\"silly\":[{\"pointless\":[\"spacing in\",\"out\"]}]}",
            // objects and array parsing
            "{ \"silly\":[{\"pointless\":[ \"spacing in\", \"out\"]}],   \"crazy\":3}" to "{\"crazy\":3,\"silly\":[{\"pointless\":[\"spacing in\",\"out\"]}]}"
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