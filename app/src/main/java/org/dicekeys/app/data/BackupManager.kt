package org.dicekeys.app.data

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.serialization.json.*
import org.dicekeys.api.DerivationRecipe
import org.dicekeys.app.extensions.dialog
import org.dicekeys.app.extensions.errorDialog
import org.dicekeys.app.repositories.RecipeRepository
import java.io.FileInputStream
import java.io.FileOutputStream

class BackupManager constructor(val context: Context, val recipeRepository: RecipeRepository) {

    fun backup(fragment: Fragment, fileUri: Uri) {
        fragment.lifecycleScope.launchWhenStarted {
            try {
                val fileDescriptor: ParcelFileDescriptor? =
                    context.contentResolver.openFileDescriptor(fileUri, "w")
                fileDescriptor?.use {  // auto close file after use
                    FileOutputStream(fileDescriptor.fileDescriptor).use { fileStream ->

                        // get all recipes
                        recipeRepository.getRecipesLiveData().value?.let {
                            Json.encodeToStream(BackupRecipes.create(it), fileStream)
                            fragment.dialog("Backup", "Recipes backup files created")
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                fragment.errorDialog(e)
            }
        }
    }

    fun restore(fragment: Fragment, fileUri: Uri) {
        fragment.lifecycleScope.launchWhenStarted {

            try {

                val fileDescriptor: ParcelFileDescriptor = context.contentResolver.openFileDescriptor(fileUri, "r")!!

                val jsonElement = fileDescriptor.use {  // auto close file after use
                    Json.decodeFromStream<JsonElement>(FileInputStream(fileDescriptor.fileDescriptor))
                }

                val derivationRecipes = try {
                    val backupRecipes = deserializeBackupFileV1(jsonElement)
                    backupRecipes.recipes
                } catch (e: Exception) {
                    e.printStackTrace()

                    // Allow it to throw if v0 is not deserialized
                    deserializeBackupFileV0(jsonElement)
                }

                // Merge recipes, name is replaced if recipe have the same id
                recipeRepository.save(derivationRecipes)
                fragment.dialog("Restore", "Restored recipes: ${derivationRecipes.size}")

            } catch (e: Exception) {
                e.printStackTrace()
                fragment.errorDialog(e)
            }
        }
    }

    private fun deserializeBackupFileV0(jsonElement: JsonElement): List<DerivationRecipe> {
        return Json.decodeFromJsonElement(jsonElement)
    }

    private fun deserializeBackupFileV1(jsonElement: JsonElement): BackupRecipes {
        return Json.decodeFromJsonElement(jsonElement)
    }
}