package org.dicekeys.app.repositories

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.dicekeys.api.DerivationRecipe

/*
 * RecipeRepository
 *
 * A SharedPreferences-backed storage for Recipes.
 *
 */

class RecipeRepository constructor(private val sharedPreferences: SharedPreferences) {
    private val recipesLiveData: MutableLiveData<List<DerivationRecipe>> = MutableLiveData(listOf())

    private val sharedPreferencesListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        updateRecipes()
    }

    init {
        updateRecipes()
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferencesListener)
    }

    private fun updateRecipes() {
        GlobalScope.launch {
            val list = mutableListOf<DerivationRecipe>()
            for (key in sharedPreferences.all.keys) {

                getRecipe(key)?.let {
                    list += it
                }
            }

            // sort by name
            list.sortBy { it.name }

            recipesLiveData.postValue(list)
        }
    }

    fun getRecipesLiveData(): LiveData<List<DerivationRecipe>> = recipesLiveData

    private fun getRecipe(id: String): DerivationRecipe? {
        return sharedPreferences.getString(id, null)?.let {
            return Json.decodeFromString(it)
        }
    }

    /*
     * Save a recipe by it's id replacing any previous recipe with the same id.
     */
    fun save(recipe: DerivationRecipe) {
        with(sharedPreferences.edit()) {
            putString(recipe.id, recipe.toString())
            apply()
        }
    }

    /*
     * Iterate over the list and call "save" for each recipe
     */
    fun save(recipes: List<DerivationRecipe>) {
        for(recipe in recipes){
            save(recipe)
        }
    }

    fun remove(recipe: DerivationRecipe) {
        remove(recipe.id)
    }

    fun exists(recipe: DerivationRecipe) = sharedPreferences.contains(recipe.id)

    private fun remove(id: String) {
        sharedPreferences.edit().remove(id).apply()
    }

    fun size() = sharedPreferences.all.size
}