package org.dicekeys.app.fragments.dicekey

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.R
import org.dicekeys.app.databinding.DicekeyFragmentBinding
import org.dicekeys.app.encryption.AppKeystore
import org.dicekeys.app.encryption.BiometricsHelper
import javax.inject.Inject

@AndroidEntryPoint
class DiceKeyFragment: AbstractDiceKeyFragment<DicekeyFragmentBinding>(R.layout.dicekey_fragment) {

    @Inject
    lateinit var biometricsHelper : BiometricsHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(isGuarded) return

        binding.vm = viewModel

        binding.buttonSave.setOnClickListener{
            if(biometricsHelper.canUseBiometrics(requireContext())){
                biometricsHelper.encrypt(viewModel.diceKey.value!!,  AppKeystore.KeystoreType.BIOMETRIC, this)
            }else{
                biometricsHelper.encrypt(viewModel.diceKey.value!!,  AppKeystore.KeystoreType.AUTHENTICATION, this)
            }
        }

        binding.buttonLock.setOnClickListener {
            viewModel.forget()
            findNavController().popBackStack()
        }
        
        binding.dicekey.setOnClickListener {
            viewModel.toggleHideFaces()
        }
    }
}