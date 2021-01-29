package org.dicekeys.app.fragments

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.AppFragment
import org.dicekeys.app.R
import org.dicekeys.app.databinding.DicekeyFragmentBinding
import org.dicekeys.app.encryption.BiometricsHelper
import org.dicekeys.app.encryption.EncryptedStorage
import org.dicekeys.app.repositories.DiceKeyRepository
import org.dicekeys.app.viewmodels.DiceKeyViewModel
import javax.inject.Inject

@AndroidEntryPoint
class DiceKeyFragment: AppFragment<DicekeyFragmentBinding>(R.layout.dicekey_fragment) {

    @Inject
    lateinit var biometricsHelper : BiometricsHelper

    @Inject
    lateinit var encryptedStorage: EncryptedStorage

    @Inject
    lateinit var repository: DiceKeyRepository

    val args: DiceKeyFragmentArgs by navArgs()

    @Inject
    lateinit var viewModelFactory: DiceKeyViewModel.AssistedFactory

    val viewModel : DiceKeyViewModel by viewModels {
        DiceKeyViewModel.provideFactory(viewModelFactory, args.diceKeyId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val diceKey = repository.get(args.diceKeyId)

        if(diceKey == null){
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