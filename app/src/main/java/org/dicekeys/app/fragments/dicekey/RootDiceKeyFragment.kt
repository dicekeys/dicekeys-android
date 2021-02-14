package org.dicekeys.app.fragments.dicekey

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.AppFragment
import org.dicekeys.app.R
import org.dicekeys.app.databinding.RootDicekeyFragmentBinding
import org.dicekeys.app.repositories.DiceKeyRepository
import org.dicekeys.app.viewmodels.DiceKeyViewModel
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face
import javax.inject.Inject

/*
 * This is the Root Fragment handling all Fragment related to a DiceKey
 * It has a navController handling the bottom bar navigation and the inner nav flow.
 * The best way to add a view under the RootDiceKeyFragment is by extending the AbstractDiceKeyFragment
 */

@AndroidEntryPoint
class RootDiceKeyFragment : AppFragment<RootDicekeyFragmentBinding>(R.layout.root_dicekey_fragment) {

    private lateinit var innerNavController: NavController

    @Inject
    lateinit var repository: DiceKeyRepository

    lateinit var diceKey: DiceKey<Face>

    private val args: RootDiceKeyFragmentArgs by navArgs()

    @Inject
    lateinit var viewModelFactory: DiceKeyViewModel.AssistedFactory

    val viewModel: DiceKeyViewModel by viewModels {
        DiceKeyViewModel.provideFactory(viewModelFactory, diceKey)
    }


    private val onBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            innerNavController.popBackStack()
        }
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

        innerNavController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(innerNavController)

        binding.buttonLock.setOnClickListener {
            lock()
        }

        binding.buttonSave.setOnClickListener {
            innerNavController.navigate(R.id.save)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)

        innerNavController.addOnDestinationChangedListener { _, destination, _ ->
            // remove the highlight when on Save fragment
            binding.bottomNavigation.menu.setGroupCheckable(0, destination.id != R.id.save, true);

            // handle Back navigation
            onBackPressedCallback.isEnabled = innerNavController.previousBackStackEntry != null
        }

        binding.toolbarTitle.text = getString(R.string.dicekey_with_center, diceKey.centerFace().toHumanReadableForm(false))
    }


    private fun lock() {
        viewModel.forget()
        findNavController().popBackStack()
    }
}