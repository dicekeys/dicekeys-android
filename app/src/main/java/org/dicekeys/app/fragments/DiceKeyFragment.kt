package org.dicekeys.app.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.AppFragment
import org.dicekeys.app.R
import org.dicekeys.app.databinding.DicekeyFragmentBinding
import org.dicekeys.app.encryption.BiometricsHelper
import org.dicekeys.app.repositories.DiceKeyRepository
import org.dicekeys.app.viewmodels.DiceKeyViewModel
import org.dicekeys.dicekey.DiceKey
import javax.inject.Inject

@AndroidEntryPoint
class DiceKeyFragment: AppFragment<DicekeyFragmentBinding>(R.layout.dicekey_fragment) {

    @Inject
    lateinit var biometricsHelper : BiometricsHelper

    @Inject
    lateinit var repository: DiceKeyRepository

    lateinit var diceKey: DiceKey<*>

    private val args: DiceKeyFragmentArgs by navArgs()

    @Inject
    lateinit var viewModelFactory: DiceKeyViewModel.AssistedFactory

    private val viewModel : DiceKeyViewModel by viewModels {
        DiceKeyViewModel.provideFactory(viewModelFactory, diceKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Guard: If DiceKey is not available, return
        repository.get(args.diceKeyId)?.also {
            diceKey = it
        } ?: run {
            findNavController().popBackStack()
            return
        }

        binding.vm = viewModel

        binding.title.text = diceKey.keyId

        binding.buttonSave.setOnClickListener {
            biometricsHelper.encrypt(diceKey, this)
        }

        binding.buttonDelete.setOnClickListener {
            viewModel.remove()
        }

        binding.buttonForget.setOnClickListener {
            viewModel.forget()
            findNavController().popBackStack()
        }
    }
}