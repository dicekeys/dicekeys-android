package org.dicekeys.app.fragments.dicekey

import android.os.Bundle
import android.view.View
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.AppFragment
import org.dicekeys.app.R
import org.dicekeys.app.databinding.SolokeyFragmentBinding
import org.dicekeys.app.viewmodels.DiceKeyViewModel

@AndroidEntryPoint
class SoloKeyFragment: AbstractDiceKeyFragment<SolokeyFragmentBinding>(R.layout.solokey_fragment) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}