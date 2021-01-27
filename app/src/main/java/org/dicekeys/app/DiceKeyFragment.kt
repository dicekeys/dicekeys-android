package org.dicekeys.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.databinding.AddFragmentBinding
import org.dicekeys.app.databinding.DicekeyFragmentBinding
import org.dicekeys.app.encryption.BiometricsHelper
import org.dicekeys.app.encryption.EncryptedStorage
import org.dicekeys.app.repositories.DiceKeyRepository
import org.dicekeys.dicekey.FaceRead
import org.dicekeys.read.ReadDiceKeyActivity
import javax.inject.Inject

@AndroidEntryPoint
class DiceKeyFragment: AppFragment<DicekeyFragmentBinding>(R.layout.dicekey_fragment) {

    @Inject
    lateinit var biometricsHelper : BiometricsHelper

    @Inject
    lateinit var encryptedStorage: EncryptedStorage

    @Inject
    lateinit var repository: DiceKeyRepository

    val args: DiceKeyFragmentArgs by navArgs()
    val viewModel : DiceKeyViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dice = repository.get(args.diceKeyId)

        if(dice == null){
            findNavController().popBackStack()
            return
        }

        binding.title.text = dice.keyId
    }
}