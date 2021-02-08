package org.dicekeys.app.fragments

import android.os.Bundle
import android.view.View
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.AppFragment
import org.dicekeys.app.R
import org.dicekeys.app.databinding.SeedSolokeyFragmentBinding
import org.dicekeys.app.viewmodels.DiceKeyViewModel

@AndroidEntryPoint
class SeedSoloKeyFragment: AppFragment<SeedSolokeyFragmentBinding>(R.layout.seed_solokey_fragment) {

    private lateinit var viewModel: DiceKeyViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (parentFragment as MainDiceKeyFragment).viewModel
    }
}