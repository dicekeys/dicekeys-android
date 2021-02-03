package org.dicekeys.app.repositories

import android.content.SharedPreferences
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class RecipeRepositoryUnitTests {

    private lateinit var repo: RecipeRepository

    @Mock
    lateinit var  sharedPreferences: SharedPreferences


    @Before
    fun setup() {
        repo = RecipeRepository(sharedPreferences)

    }

    @Test
    fun fail() {
        assert(true)
        assert(false)
    }
}