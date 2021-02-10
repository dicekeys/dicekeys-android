package org.dicekeys.app.fragments

import android.os.Bundle
import android.view.View
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.AppFragment
import org.dicekeys.app.R
import org.dicekeys.app.databinding.AssembleFragmentBinding
import org.dicekeys.app.viewmodels.DiceKeyViewModel

@AndroidEntryPoint
class AssembleFragment: AppFragment<AssembleFragmentBinding>(R.layout.assemble_fragment) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}