package org.dicekeys.app.fragments.dicekey

import android.os.Bundle
import android.view.View
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.AppFragment
import org.dicekeys.app.R
import org.dicekeys.app.databinding.SaveFragmentBinding
import org.dicekeys.app.encryption.BiometricsHelper
import javax.inject.Inject

@AndroidEntryPoint
class SaveFragment: AbstractDiceKeyFragment<SaveFragmentBinding>(R.layout.save_fragment) {

    @Inject
    lateinit var biometricsHelper: BiometricsHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = viewModel

        binding.buttonSave.setOnClickListener{
            biometricsHelper.encrypt(viewModel.diceKey.value!!, this)
        }

        binding.buttonRemove.setOnClickListener{
            viewModel.remove()
        }

    }
}