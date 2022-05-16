package org.dicekeys.app.repositories

import android.content.SharedPreferences
import org.dicekeys.api.DerivationRecipe
import org.dicekeys.crypto.seeded.DerivationOptions
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever


@RunWith(MockitoJUnitRunner::class)
class RecipeRepositoryUnitTests {

    private lateinit var repo: RecipeRepository

    @Mock
    lateinit var sharedPreferences: SharedPreferences

    @Mock
    lateinit var derivationRecipe: DerivationRecipe


    @Mock
    lateinit var editor : SharedPreferences.Editor

    @Before
    fun setup() {
        whenever(derivationRecipe.id).thenReturn("recipeId")

        // Mock SharedPreferences Editor
        whenever(editor.remove(any())).thenReturn(mock())
        whenever(sharedPreferences.edit()).thenReturn(editor)

        repo = RecipeRepository(sharedPreferences)
    }

    @Test
    fun test_contain_isCalled() {
        repo.exists(derivationRecipe)
        verify(sharedPreferences).contains(derivationRecipe.id)
    }

    @Test
    fun test_remove_isCalled() {
        repo.remove(derivationRecipe)
        verify(editor).remove(derivationRecipe.id)
    }

    // Move to DerivationRecipes unit test
    @Test
    fun test_derivationRecipes_id() {
        val derivationRecipe1 = DerivationRecipe(type = DerivationOptions.Type.Password, name = "name1", recipeJson = """{"purpose":"ssh"}""")
        val derivationRecipe2 = DerivationRecipe(type = DerivationOptions.Type.Password, name = "name2", recipeJson = """{"purpose":"ssh"}""")
        val derivationRecipe3 = DerivationRecipe(type = DerivationOptions.Type.Secret, name = "name3", recipeJson = """{"purpose":"ssh"}""")
        val derivationRecipe4 = DerivationRecipe(type = DerivationOptions.Type.Secret, name = "name4", recipeJson = """{"purpose":"ssh"}""")

        assertEquals(derivationRecipe1.id, derivationRecipe1.id)
        assertEquals(derivationRecipe1.id, derivationRecipe2.id)
        assertEquals(derivationRecipe1.recipeJson, derivationRecipe2.recipeJson)

        assertEquals(derivationRecipe3.id, derivationRecipe4.id)
        assertEquals(derivationRecipe3.id, derivationRecipe4.id)
        assertEquals(derivationRecipe3.recipeJson, derivationRecipe4.recipeJson)


        assertNotEquals(derivationRecipe1, derivationRecipe2)
        assertNotEquals(derivationRecipe1.toString(), derivationRecipe2.recipeJson)

        assertNotEquals(derivationRecipe1.id, derivationRecipe3.id)
    }
}