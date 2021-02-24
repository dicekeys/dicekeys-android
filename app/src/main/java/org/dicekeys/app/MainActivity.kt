package org.dicekeys.app

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.databinding.ActivityMainBinding
import org.dicekeys.app.fragments.AssembleFragmentDirections
import org.dicekeys.app.repositories.DiceKeyRepository
import org.dicekeys.app.viewmodels.DiceKeyViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var diceKeyRepository: DiceKeyRepository

    val viewModel: DiceKeyViewModel by viewModels()

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
            binding.toolbar.isGone = ( (isRoot || insideDiceKeyNav) && !isScan)

            binding.dicekeyToolbar.isVisible = insideDiceKeyNav && !isScan
            binding.bottomNavigation.isVisible = insideDiceKeyNav && !isScan

            // Remove the highlight when on Save fragment
            binding.bottomNavigation.menu.setGroupCheckable(0, destination.id != R.id.save, true);

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

        binding.buttonLock.setOnClickListener {
            viewModel.forget()
            navController.popBackStack(R.id.listDiceKeysFragment, false)
        }

        binding.buttonSave.setOnClickListener {
            navController.navigate(R.id.save)
        }


        viewModel.diceKey.observe(this){
            binding.toolbarTitle.text = getString(R.string.dicekey_with_center, it?.centerFace()?.toHumanReadableForm(false))
        }
    }
}