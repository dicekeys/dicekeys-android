package org.dicekeys.app.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.AppFragment
import org.dicekeys.app.R
import org.dicekeys.app.databinding.DicekeyFragmentBinding
import org.dicekeys.app.databinding.DicekeyRootFragmentBinding
import org.dicekeys.app.encryption.BiometricsHelper
import org.dicekeys.app.repositories.DiceKeyRepository
import org.dicekeys.app.viewmodels.DiceKeyViewModel
import org.dicekeys.dicekey.DiceKey
import javax.inject.Inject

@AndroidEntryPoint
class DiceKeyRootFragment: AppFragment<DicekeyRootFragmentBinding>(R.layout.dicekey_root_fragment) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        NavigationUI.setupWithNavController(binding.bottomNavigationView, findNavController())
    }
}