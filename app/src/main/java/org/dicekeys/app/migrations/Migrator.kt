package org.dicekeys.app.migrations

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.dicekeys.api.DerivationRecipe
import org.dicekeys.app.BuildConfig

/*
 * Migrator
 *
 * Migrate data between versions and guarantee backwards compatibility
 *
 */
class Migrator constructor(val context: Context, val sharedPreferences: SharedPreferences) {

    fun migrateIfNeeded() {
        val versionLastUsedToWritePreferences = sharedPreferences.getInt(VERSION_CODE_LAST_USED_TO_WRITE_PREFERENCES, 0)

        if (BuildConfig.VERSION_CODE > versionLastUsedToWritePreferences) {
            migrateRecipeRepositoryToV9(versionLastUsedToWritePreferences)

            updateVersionCode()
        }
    }

    private fun updateVersionCode() {
        sharedPreferences.edit().also {
            it.putInt(VERSION_CODE_LAST_USED_TO_WRITE_PREFERENCES, BuildConfig.VERSION_CODE)
        }.apply()
    }

    /*
     * Migrate stored recipes re-calculating the unique ID (type.hashCode() + recipeJson.hashCode())
     */
    private fun migrateRecipeRepositoryToV9(versionLastUsedToWritePreferences: Int) {
        if (versionLastUsedToWritePreferences < 9) {
            val recipesSharedPreferences =
                context.getSharedPreferences("recipes", Context.MODE_PRIVATE)

            // Migrating stored recipes to a JSON format in which the deprecated field name `derivation_options_format` is renamed `recipeJson`.
            // (this will be done automatically by the deserializing the old format and re-serializing it)
            for (key in recipesSharedPreferences.all.keys) {
                try {
                    recipesSharedPreferences.getString(key, null)?.let {
                        val derivationRecipe = Json.decodeFromString<DerivationRecipe>(it)

                        recipesSharedPreferences.edit().apply {
                            remove(key)
                            putString(derivationRecipe.id, derivationRecipe.toString())
                        }.apply()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    companion object {
        const val VERSION_CODE_LAST_USED_TO_WRITE_PREFERENCES = "version_code_last_used"
    }
}