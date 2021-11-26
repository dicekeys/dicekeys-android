package org.dicekeys.app

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import org.dicekeys.api.DerivationRecipe
import org.dicekeys.app.utils.getDepthOfPublicSuffix
import org.dicekeys.crypto.seeded.DerivationOptions
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class RecipeBuilderUnitTests {

    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    private lateinit var builder: RecipeBuilder

    companion object{
        val GOOGLE = DerivationRecipe(DerivationOptions.Type.Password, "google.com", """{"allow":[{"host":"*.google.com"}],"#":12}""")
        val GOOGLE_SUBDOMAIN = DerivationRecipe(DerivationOptions.Type.Password, "subdomain.google.com", """{"allow":[{"host":"*.subdomain.google.com"}],"#":12}""")
        val GOOGLE_FOOD = DerivationRecipe(DerivationOptions.Type.Password, "food.com, google.com", """{"allow":[{"host":"*.food.com"},{"host":"*.google.com"}]}""")
        val APPLE = DerivationRecipe(DerivationOptions.Type.Password, "apple.com, icloud.com", """{"allow":[{"host":"*.apple.com"},{"host":"*.icloud.com"}],"lengthInChars":64,"#":2}""")
    }

    @Before
    fun setup(){
        builder = RecipeBuilder(DerivationOptions.Type.Password, null)
    }

    @Test
    fun test_predefinedSolutions_listOfDomains(){

        builder.updateDomains("google.com")
        builder.updateSequence(12)
        Assert.assertEquals(GOOGLE, builder.build())

        builder.reset()
        builder.updateDomains("*.google.com")
        builder.updateSequence(12)
        Assert.assertEquals(GOOGLE, builder.build())

        builder.reset()
        builder.updateDomains(".google.com/")
        builder.updateSequence(12)
        Assert.assertEquals(GOOGLE, builder.build())

        builder.reset()
        builder.updateDomains("*.google.com/")
        builder.updateSequence(12)
        Assert.assertEquals(GOOGLE, builder.build())

        builder.reset()
        builder.updateDomains( "  *.google.com/  ")
        builder.updateSequence(12)
        Assert.assertEquals(GOOGLE, builder.build())

        builder.reset()
        builder.updateDomains( "  *.google.com/  ,   *.google.com/  ")
        builder.updateSequence(12)
        Assert.assertEquals(GOOGLE, builder.build())

        builder.reset()
        builder.updateDomains(".subdomain.google.com/")
        builder.updateSequence(12)
        Assert.assertEquals(GOOGLE_SUBDOMAIN, builder.build())

        builder.reset()
        builder.updateDomains("google.com,food.com")
        Assert.assertEquals(GOOGLE_FOOD, builder.build())

        builder.reset()
        builder.updateDomains(".apple.com/,.icloud.com/")
        builder.updateSequence(2)
        builder.updateLengthInChars(64)
        Assert.assertEquals(APPLE, builder.build())
    }

    @Test
    fun test_predefinedSolutions_Url(){

        builder.updateDomains("https://google.com/?q=DiceKeys")
        builder.updateSequence(12)
        Assert.assertEquals(GOOGLE, builder.build())

        builder.reset()
        builder.updateDomains("http://google.com/search?q=DiceKeys")
        builder.updateSequence(12)
        Assert.assertEquals(GOOGLE, builder.build())

        builder.reset()
        builder.updateDomains("http://subdomain.google.com/search?q=DiceKeys")
        builder.updateSequence(10)
        builder.build()
        builder.updateSequence(12)
        Assert.assertEquals(GOOGLE_SUBDOMAIN, builder.build())
    }

    @Test
    fun test_createDerivationRecipeForSequence(){
        builder.updateDomains("google.com")
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
}