package org.dicekeys.app

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.databinding.ActivityMainBinding
import org.dicekeys.app.repositories.DiceKeyRepository
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var diceKeyRepository: DiceKeyRepository

    companion object{
        const val READ_DICE_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make the window Secure
        // Postpone after V2
        // window?.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
                setOf(R.id.listDiceKeysFragment)
        )

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)


        navController.addOnDestinationChangedListener { _, destination, _ ->

            /*
             * ListDiceKeysFragment is considered the top level navigation,
             * so when navigating to it, its good idea to clear all DiceKeys from memory.
             * Using this method we are capturing navigation from both keyboard back press and toolbar back button.
             */
            if(destination.id == R.id.listDiceKeysFragment){
                // Clear the repository
                diceKeyRepository.clear()
            }
        }
    }
}