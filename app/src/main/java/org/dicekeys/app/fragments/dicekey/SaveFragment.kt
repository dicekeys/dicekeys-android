package org.dicekeys.app.fragments.dicekey

import android.os.Build
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

    override fun onViewCreatedGuarded(view: View, savedInstanceState: Bundle?) {
        binding.vm = viewModel

        binding.buttonSave.setOnClickListener {
            when(binding.keystoreType.checkedRadioButtonId){
                R.id.unlock_screen_lock -> {
                    biometricsHelper.encrypt(viewModel.diceKey.value!!,  AppKeystore.KeystoreType.ALLOW_BIOMETRIC_OR_KNOWLEDGE_BASED_AUTHENTICATION, this)
                }
                R.id.unlock_device_credentials -> {
                    biometricsHelper.encrypt(viewModel.diceKey.value!!,  AppKeystore.KeystoreType.ALLOW_ONLY_KNOWLEDGE_BASED_AUTHENTICATION, this)
                }
                R.id.unlock_biometrics -> {
                    biometricsHelper.encrypt(viewModel.diceKey.value!!,  AppKeystore.KeystoreType.ALLOW_ONLY_BIOMETRIC_AUTHENTICATION, this)
                }
            }
        }

        // DEVICE_CREDENTIAL is unsupported prior to API 30
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            binding.unlockDeviceCredentials.isEnabled = false
        }

        binding.buttonRemove.setOnClickListener{
            viewModel.remove()
        }
    }

    override fun onResume() {
        super.onResume()

        binding.canUseBiometrics = biometricsHelper.canUseBiometrics(requireContext()).also { canUseBiometrics ->
            // Change selection if the checked selection is not longer available
            if(!canUseBiometrics && binding.keystoreType.checkedRadioButtonId == R.id.unlock_biometrics){
                binding.keystoreType.check(R.id.unlock_screen_lock)
            }
        }
    }
}