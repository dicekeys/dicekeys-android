package org.dicekeys.app.fragments.dicekey

import android.os.Bundle
import android.view.View
import androidx.core.view.iterator
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.AppFragment
import org.dicekeys.app.R
import org.dicekeys.app.databinding.MainDicekeyFragmentBinding
import org.dicekeys.app.repositories.DiceKeyRepository
import org.dicekeys.app.viewmodels.DiceKeyViewModel
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face
import javax.inject.Inject

@AndroidEntryPoint
class MainDiceKeyFragment : AppFragment<MainDicekeyFragmentBinding>(R.layout.main_dicekey_fragment) {

    @Inject
    lateinit var repository: DiceKeyRepository

    lateinit var diceKey: DiceKey<Face>

    private val args: MainDiceKeyFragmentArgs by navArgs()

    @Inject
    lateinit var viewModelFactory: DiceKeyViewModel.AssistedFactory

    val viewModel: DiceKeyViewModel by viewModels {
        DiceKeyViewModel.provideFactory(viewModelFactory, diceKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Guard: If DiceKey is not available, return
        repository.get(args.diceKeyId)?.also {
            diceKey = it as DiceKey<Face>
        } ?: run {
            findNavController().popBackStack()
            return
        }

        binding.vm = viewModel

        val navHostFragment = childFragmentManager.findFragmentById(R.id.dicekey_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)

        binding.buttonLock.setOnClickListener {
            lock()
        }

        binding.buttonSave.setOnClickListener {
            navController.navigate(R.id.save)

        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            // remove the highlight when on Save fragment
            binding.bottomNavigation.menu.setGroupCheckable(0, destination.id != R.id.save, true);
        }

        binding.toolbarTitle.text = getString(R.string.dicekey_with_center, diceKey.centerFace().toHumanReadableForm(false))
    }


    private fun lock(){
        viewModel.forget()
        findNavController().popBackStack()
    }
}