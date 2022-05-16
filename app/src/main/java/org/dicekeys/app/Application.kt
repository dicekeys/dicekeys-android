package org.dicekeys.app

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.HiltAndroidApp
import org.dicekeys.app.migrations.Migrator
import javax.inject.Inject

@HiltAndroidApp
class Application : Application() {

    @Inject
    lateinit var appLifecycleObserver: AppLifecycleObserver

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var migrator: Migrator

    override fun onCreate() {
        super.onCreate()

        migrator.migrateIfNeeded()

        val appearance = sharedPreferences.getString("appearance", null)

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