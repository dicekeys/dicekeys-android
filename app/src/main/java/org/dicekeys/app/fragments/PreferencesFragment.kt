package org.dicekeys.app.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.text.format.DateFormat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dicekeys.app.BuildConfig
import org.dicekeys.app.R
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.dicekeys.app.data.BackupRecipes
import org.dicekeys.app.extensions.dialog
import org.dicekeys.app.extensions.errorDialog
import org.dicekeys.app.repositories.RecipeRepository
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class PreferencesFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    lateinit var recipeRepository: RecipeRepository

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    /*
     * Register an ActivityResultContract to prompt the user to select a path for creating a new file.
     * On returning with a valid uri/file, serialize all recipes in the app.
     */
    private val createDocument =
        registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
            uri?.let { uri ->
                lifecycleScope.launchWhenStarted {
                    try {
                        val fileDescriptor: ParcelFileDescriptor? =
                            requireContext().contentResolver.openFileDescriptor(uri, "w")
                        fileDescriptor?.use {  // auto close file after use
                            FileOutputStream(fileDescriptor.fileDescriptor).use { fileStream ->

                                // get all recipes
                                recipeRepository.getRecipesLiveData().value?.let {
                                    // create a BackupRecipes data object
                                    val backupRecipes = BackupRecipes(version = 1, recipes = it)
                                    // serialize it and write it to the file
                                    Json.encodeToStream(backupRecipes, fileStream)
                                    dialog("Backup", "Recipes backup files created")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        errorDialog(e)
                    }
                }
            }
        }

    /*
     * Register an ActivityResultContract to prompt the user to open a file.
     * On returning with a valid uri/file, deserialize and import all recipes in the app.
     */
    private val openDocument =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { uri ->
                lifecycleScope.launchWhenStarted {
                    val fileDescriptor: ParcelFileDescriptor? =
                        requireContext().contentResolver.openFileDescriptor(uri, "r")
                    fileDescriptor?.use {  // auto close file after use
                        try {
                            val fd: FileDescriptor = fileDescriptor.fileDescriptor
                            val fileStream = FileInputStream(fd)
                            val recipes: BackupRecipes = Json.decodeFromStream(fileStream) // Decode file contents into BackupRecipes
                            recipeRepository.save(recipes.recipes)

                            dialog("Restore", "Restored recipes: ${recipes.recipes.size}")
                        } catch (e: Exception) {
                            e.printStackTrace()
                            errorDialog(e)
                        }
                    }
                }
            }
        }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        findPreference<Preference>("version")?.summary = BuildConfig.VERSION_NAME
        findPreference<Preference>("build")?.summary = BuildConfig.VERSION_CODE.toString(10)

        findPreference<Preference>("backup")?.setOnPreferenceClickListener {
            val date = DateFormat.format("yyyy-MM-dd", Date())
            val proposedFilename = "DiceKeys_Recipes_$date.json"
            createDocument.launch(proposedFilename)
            true
        }
        findPreference<Preference>("backup")?.isVisible = false // Disable until we finalize the backup file structure

        findPreference<Preference>("restore")?.setOnPreferenceClickListener {
            openDocument.launch(arrayOf(JsonMimeType))
            true
        }
        findPreference<Preference>("restore")?.isVisible = false // Disable until we finalize the backup file structure
    }

    override fun onResume() {
        super.onResume()
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sp: SharedPreferences?, key: String?) {
        if(key == "appearance"){
            val appearance = sharedPreferences.getString(key, null)
            AppCompatDelegate.setDefaultNightMode(
                when (appearance) {
                    "dark" -> {
                        AppCompatDelegate.MODE_NIGHT_YES
                    }
                    "light" -> {
                        AppCompatDelegate.MODE_NIGHT_NO
                    }
                    else -> {
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    }
                }
            )
        }
    }

    companion object {
        const val JsonMimeType = "application/json"
    }
}