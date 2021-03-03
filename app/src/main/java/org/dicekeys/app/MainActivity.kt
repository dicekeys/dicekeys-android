package org.dicekeys.app

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.databinding.ActivityMainBinding

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityMainBinding

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        setContentView(binding.root)


        setSupportActionBar(binding.toolbar)
        supportActionBar?.also {
            // Prevent replacing title from NavController
            it.setDisplayShowTitleEnabled(false)
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
                setOf(R.id.listDiceKeysFragment)
        )

        binding.toolbar.also {
            // remove the extra padding on the title
            it.contentInsetStartWithNavigation = 0
            it.setupWithNavController(navController, appBarConfiguration)
        }

        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { navController, destination, _ ->

            val insideDiceKeyNav = navController.backStack.firstOrNull { it.destination.id == R.id.dicekey
                    || it.destination.id == R.id.solokey
                    || it.destination.id == R.id.backupSelect
                    || it.destination.id == R.id.secrets
            } != null

            val isRoot = destination.id == R.id.listDiceKeysFragment
            val isScan = navController.currentBackStackEntry?.destination?.id == R.id.scanFragment

            // Hide the toolbar
            // binding.toolbar.isGone = ( (isRoot || insideDiceKeyNav) && !isScan)
            binding.bottomNavigation.isVisible = insideDiceKeyNav && !isScan

            // Remove the highlight when on Save fragment
            binding.bottomNavigation.menu.setGroupCheckable(0, destination.id != R.id.save, true);
        }

        updateSecureDisplay()
    }

    override fun onResume() {
        super.onResume()

        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()

        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    fun setTitle(title: String){
        binding.toolbar.title = title
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        updateSecureDisplay()
    }

    private fun updateSecureDisplay(){
        sharedPreferences.getBoolean("secure_display", true).also {
            if(it){
                window?.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
            }else{
                window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }
}