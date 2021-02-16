package org.dicekeys.app.fragments.dicekey

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.AppFragment
import org.dicekeys.app.R
import org.dicekeys.app.databinding.DicekeyFragmentBinding
import org.dicekeys.app.openDialogDeleteDiceKey
import org.dicekeys.app.encryption.BiometricsHelper
import org.dicekeys.app.viewmodels.DiceKeyViewModel
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face
import javax.inject.Inject

@AndroidEntryPoint
class DiceKeyFragment: AbstractDiceKeyFragment<DicekeyFragmentBinding>(R.layout.dicekey_fragment) {

    @Inject
    lateinit var biometricsHelper : BiometricsHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = viewModel

        binding.buttonSave.setOnClickListener {
            biometricsHelper.encrypt(viewModel.diceKey, this)
        }
    }
}