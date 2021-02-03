package org.dicekeys.app.repositories

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.dicekeys.app.recipes.Recipe

/*
 * RecipeRepository
 *
 * A SharedPreferences-backed storage for Derivation Recipes.
 *
 */

class RecipeRepository(private val sharedPreferences: SharedPreferences) {
    private val recipesLiveData: MutableLiveData<List<Recipe>> = MutableLiveData(listOf())

    private val sharedPreferencesListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        updateRecipes()
    }

    init {
        updateRecipes()
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferencesListener)
    }

    private fun updateRecipes() {
        GlobalScope.launch {
            val list = mutableListOf<Recipe>()
            for (key in sharedPreferences.all.keys) {

                getRecipe(key)?.let {
                    list += it
                }
            }
            recipesLiveData.postValue(list)
        }
    }

    fun getRecipesLiveData(): LiveData<List<Recipe>> = recipesLiveData

    fun getRecipe(id: String): Recipe? {
        return sharedPreferences.getString(id, null)?.let {
            return Json.decodeFromString(it)
        }
    }

    fun save(recipe: Recipe) {
        sharedPreferences
                .edit()
                .putString(recipe.id, recipe.toString())
                .apply()
    }

    fun remove(recipe: Recipe) {
        remove(recipe.id)
    }

    fun exists(recipe: Recipe) = sharedPreferences.contains(recipe.id)


    private fun remove(id: String) {
        sharedPreferences.edit().remove(id).apply()
    }
}