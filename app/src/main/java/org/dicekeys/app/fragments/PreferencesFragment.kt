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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.dicekeys.api.DerivationRecipe
import org.dicekeys.app.BuildConfig
import org.dicekeys.app.R
import org.dicekeys.app.data.BackupManager
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

    @Inject
    lateinit var backupManager: BackupManager

    /*
     * Register an ActivityResultContract to prompt the user to select a path for creating a new file.
     * On returning with a valid uri/file, serialize all recipes in the app.
     */
    private val createDocument =
        registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
            uri?.let { uri ->
                backupManager.backup(this, uri)
            }
        }

    /*
     * Register an ActivityResultContract to prompt the user to open a file.
     * On returning with a valid uri/file, deserialize and import all recipes in the app.
     */
    private val openDocument =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { uri ->
                backupManager.restore(this, uri)
            }
        }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        findPreference<Preference>("version")?.summary = BuildConfig.VERSION_NAME
        findPreference<Preference>("build")?.summary = BuildConfig.VERSION_CODE.toString(10)

        findPreference<Preference>("backup")?.setOnPreferenceClickListener {
            val date = DateFormat.format("yyyy-MM-dd_HH:mm", Date())
            val proposedFilename = "DiceKeys_Recipes_$date.json"
            createDocument.launch(proposedFilename)
            true
        }

        findPreference<Preference>("restore")?.setOnPreferenceClickListener {
            openDocument.launch(arrayOf(JsonMimeType))
            true
        }
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