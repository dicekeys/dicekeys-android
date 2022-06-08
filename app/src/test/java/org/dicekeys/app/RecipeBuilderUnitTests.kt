package org.dicekeys.app

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.GlobalScope
import org.dicekeys.api.DerivationRecipe
import org.dicekeys.api.recipeWithSequence
import org.dicekeys.crypto.seeded.DerivationOptions
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class RecipeBuilderUnitTests {

    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    private lateinit var builder: RecipeBuilder

    companion object{
        val GOOGLE = DerivationRecipe(DerivationOptions.Type.Password, "google.com Password (12)", """{"allow":[{"host":"*.google.com"}],"#":12}""")
        val GOOGLE_WITHOUT_WILDCARD = DerivationRecipe(DerivationOptions.Type.Password, "google.com Password (12)", """{"allow":[{"host":"google.com"}],"#":12}""")
        val GOOGLE_SUBDOMAIN = DerivationRecipe(DerivationOptions.Type.Password, "subdomain.google.com Password (12)", """{"allow":[{"host":"*.subdomain.google.com"}],"#":12}""")
        val GOOGLE_FOOD = DerivationRecipe(DerivationOptions.Type.Password, "food.com, google.com Password", """{"allow":[{"host":"*.food.com"},{"host":"*.google.com"}]}""")
        val APPLE = DerivationRecipe(DerivationOptions.Type.Password, "apple.com, icloud.com Password (2)", """{"allow":[{"host":"*.apple.com"},{"host":"*.icloud.com"}],"lengthInChars":64,"#":2}""")
    }

    @Before
    fun setup(){
        builder = RecipeBuilder(DerivationOptions.Type.Password, GlobalScope, null)
    }

    @Test
    fun test_predefinedSolutions_listOfDomains(){

        builder.domains.value = "google.com"
        builder.sequence.value = "12"
        Assert.assertEquals(GOOGLE_WITHOUT_WILDCARD, builder.build())

        builder.reset()
        builder.domains.value = "*.google.com"
        builder.sequence.value = "12"
        Assert.assertEquals(GOOGLE, builder.build())

        builder.reset()
        builder.domains.value = ".google.com/"
        builder.sequence.value = "12"
        Assert.assertEquals(GOOGLE_WITHOUT_WILDCARD, builder.build())

        builder.reset()
        builder.domains.value = "*.google.com/"
        builder.sequence.value = "12"
        Assert.assertEquals(GOOGLE, builder.build())

        builder.reset()
        builder.domains.value = "  *.google.com/  "
        builder.sequence.value = "12"
        Assert.assertEquals(GOOGLE, builder.build())

        builder.reset()
        builder.domains.value = "*.goo gle.com/  "
        builder.sequence.value = "12"
        Assert.assertEquals(GOOGLE, builder.build())

        builder.reset()
        builder.domains.value = "  *.google.com/  ,   *.google.com/  "
        builder.sequence.value = "12"
        Assert.assertEquals(GOOGLE, builder.build())

        builder.reset()
        builder.domains.value = "*.subdomain.google.com/"
        builder.sequence.value = "12"
        Assert.assertEquals(GOOGLE_SUBDOMAIN, builder.build())

        builder.reset()
        builder.domains.value = "*.google.com,*.food.com"
        Assert.assertEquals(GOOGLE_FOOD, builder.build())

        builder.reset()
        builder.domains.value = "*.apple.com/,*.icloud.com/"
        builder.sequence.value = "2"
        builder.lengthInChars.value = "64"
        Assert.assertEquals(APPLE, builder.build())
    }

    @Test
    fun test_predefinedSolutions_Url(){
        builder.domains.value = "https://google.com/?q=DiceKeys"
        builder.sequence.value = "12"
        Assert.assertEquals(GOOGLE, builder.build())

        builder.domains.value = "https://www.google.com/?q=DiceKeys"
        builder.sequence.value = "12"
        Assert.assertEquals(GOOGLE, builder.build())

        builder.reset()
        builder.domains.value = "https://food.com/?q=DiceKeys, https://google.com/?q=DiceKeys"
        Assert.assertEquals(GOOGLE_FOOD.recipeJson, builder.build()!!.recipeJson)

        builder.reset()
        builder.domains.value = "http://google.com/search?q=DiceKeys"
        builder.sequence.value = "12"
        Assert.assertEquals(GOOGLE.recipeJson, builder.build()!!.recipeJson)

        builder.reset()
        builder.domains.value = "http://subdomain.google.com/search?q=DiceKeys"
        builder.sequence.value = "10"
        builder.build()
        builder.sequence.value = "12"
        Assert.assertEquals(GOOGLE.recipeJson, builder.build()!!.recipeJson)
    }

    @Test
    fun test_createDerivationRecipeForSequence(){
        builder.domains.value = "google.com"
        val original = builder.build()!!

        var changing = original.createDerivationRecipeForSequence(12)
        Assert.assertNotEquals(original.recipeJson, changing.recipeJson)
        changing = changing.createDerivationRecipeForSequence(2)
        Assert.assertNotEquals(original.recipeJson, changing.recipeJson)


        changing = changing.createDerivationRecipeForSequence(1)
        Assert.assertEquals(original.recipeJson, changing.recipeJson)
        changing = changing.createDerivationRecipeForSequence(0)
        Assert.assertEquals(original.recipeJson, changing.recipeJson)
        changing = changing.createDerivationRecipeForSequence(-1)
        Assert.assertEquals(original.recipeJson, changing.recipeJson)

        val checkLength = DerivationRecipe.createRecipeFromTemplate(original, original.sequence, lengthInChars = 44)
        Assert.assertNotEquals(checkLength.recipeJson, DerivationRecipe.createRecipeFromTemplate(checkLength, checkLength.sequence))
    }

    @Test
    fun test_recipeWithSequence() {
        Assert.assertEquals(GOOGLE_FOOD.recipeJson, GOOGLE_FOOD.recipeJson.recipeWithSequence(null))
        Assert.assertEquals(GOOGLE_FOOD.recipeJson, GOOGLE_FOOD.recipeJson.recipeWithSequence(-1))
        Assert.assertEquals(GOOGLE_FOOD.recipeJson, GOOGLE_FOOD.recipeJson.recipeWithSequence(0))
        Assert.assertEquals(GOOGLE_FOOD.recipeJson, GOOGLE_FOOD.recipeJson.recipeWithSequence(1))

        Assert.assertEquals(GOOGLE.recipeJson, GOOGLE.recipeJson.recipeWithSequence(12))

        Assert.assertNotEquals(GOOGLE.recipeJson, GOOGLE.recipeJson.recipeWithSequence(10))
        Assert.assertNotEquals(GOOGLE.recipeJson, GOOGLE.recipeJson.recipeWithSequence(null))
        Assert.assertNotEquals(GOOGLE.recipeJson, GOOGLE.recipeJson.recipeWithSequence(0))


        // Check properties order
        Assert.assertEquals("""{"allow":[{"host":"*.google.com"}],"#":12}""", """{"allow":[{"host":"*.google.com"}],"#":12}""".recipeWithSequence(12))
        Assert.assertEquals("""{"allow":[{"host":"*.google.com"}],"#":12}""", """{"#":12,"allow":[{"host":"*.google.com"}]}""".recipeWithSequence(12))
    }
}