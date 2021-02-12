package org.dicekeys.app.repositories

import android.content.SharedPreferences
import com.nhaarman.mockitokotlin2.*
import org.dicekeys.api.DerivationRecipe
import org.dicekeys.crypto.seeded.DerivationOptions
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner


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
}