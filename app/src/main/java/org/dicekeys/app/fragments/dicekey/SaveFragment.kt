package org.dicekeys.app.fragments.dicekey

import android.os.Bundle
import android.view.View
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.R
import org.dicekeys.app.databinding.SaveFragmentBinding
import org.dicekeys.app.encryption.AppKeystore
import org.dicekeys.app.encryption.BiometricsHelper
import javax.inject.Inject

@AndroidEntryPoint
class SaveFragment: AbstractDiceKeyFragment<SaveFragmentBinding>(R.layout.save_fragment) {

    @Inject
    lateinit var biometricsHelper: BiometricsHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(isGuarded) return

        binding.vm = viewModel

        binding.buttonSaveBiometrics.setOnClickListener{
            biometricsHelper.encrypt(viewModel.diceKey.value!!,  AppKeystore.KeystoreType.BIOMETRIC, this)
        }

        binding.buttonSaveScreenLock.setOnClickListener {
            biometricsHelper.encrypt(viewModel.diceKey.value!!,  AppKeystore.KeystoreType.AUTHENTICATION, this)
        }

        binding.buttonRemove.setOnClickListener{
            viewModel.remove()
        }
    }

    override fun onResume() {
        super.onResume()

        binding.canUseBiometrics = biometricsHelper.canUseBiometrics(requireContext())
    }
}