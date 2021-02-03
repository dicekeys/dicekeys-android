package org.dicekeys.app.fragments

import android.os.Bundle
import android.view.View
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.AppFragment
import org.dicekeys.app.R
import org.dicekeys.app.databinding.BackupFragmentBinding
import org.dicekeys.app.databinding.DeriveSecretFragmentBinding
import org.dicekeys.app.databinding.DicekeyFragmentBinding
import org.dicekeys.app.databinding.SeedSolokeyFragmentBinding

@AndroidEntryPoint
class DeriveSecretFragment: AppFragment<DeriveSecretFragmentBinding>(R.layout.derive_secret_fragment) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }
}