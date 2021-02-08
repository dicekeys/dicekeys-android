package org.dicekeys.app.fragments

import android.os.Bundle
import android.view.View
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.AppFragment
import org.dicekeys.app.R
import org.dicekeys.app.databinding.DicekeyFragmentBinding
import org.dicekeys.app.openDialogDeleteDiceKey
import org.dicekeys.app.encryption.BiometricsHelper
import org.dicekeys.app.viewmodels.DiceKeyViewModel
import javax.inject.Inject

@AndroidEntryPoint
class DiceKeyFragment: AppFragment<DicekeyFragmentBinding>(R.layout.dicekey_fragment) {

    @Inject
    lateinit var biometricsHelper : BiometricsHelper

    private lateinit var viewModel: DiceKeyViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (parentFragment as MainDiceKeyFragment).viewModel

        binding.vm = viewModel

        binding.title.text = viewModel.diceKey.keyId

        binding.buttonSave.setOnClickListener {
            biometricsHelper.encrypt(viewModel.diceKey, this)
        }

        binding.buttonDelete.setOnClickListener {
            openDialogDeleteDiceKey(requireContext()) {
                viewModel.remove()
            }
        }
    }
}