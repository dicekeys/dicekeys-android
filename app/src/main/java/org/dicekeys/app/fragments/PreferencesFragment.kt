package org.dicekeys.app.fragments

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.BuildConfig
import org.dicekeys.app.R

@AndroidEntryPoint
class PreferencesFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        findPreference<Preference>("version")?.summary = getString(R.string.version_s, BuildConfig.VERSION_NAME)

    }
}