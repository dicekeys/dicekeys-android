package org.dicekeys.app.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.AppFragment
import org.dicekeys.app.MainActivity
import org.dicekeys.app.R
import org.dicekeys.app.databinding.AddDicekeyFragmentBinding
import org.dicekeys.app.encryption.BiometricsHelper
import org.dicekeys.app.encryption.EncryptedStorage
import org.dicekeys.dicekey.FaceRead
import org.dicekeys.read.ReadDiceKeyActivity
import javax.inject.Inject

@AndroidEntryPoint
class AddDiceKeyFragment: AppFragment<AddDicekeyFragmentBinding>(R.layout.add_dicekey_fragment) {

    @Inject
    lateinit var biometricsHelper : BiometricsHelper

    @Inject
    lateinit var encryptedStorage: EncryptedStorage

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.readDice.setOnClickListener {
            val intent = Intent(requireContext(), ReadDiceKeyActivity::class.java)
            startActivityForResult(intent, MainActivity.READ_DICE_REQUEST_CODE)
        }
    }

    // Needs refactoring
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == MainActivity.READ_DICE_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK && data != null){

                data.getStringExtra(ReadDiceKeyActivity.Companion.Parameters.Response.diceKeyAsJson)?.let { diceKeyAsJson ->
                    FaceRead.diceKeyFromJsonFacesRead(diceKeyAsJson)?.let { diceKey ->

                        biometricsHelper.encrypt(diceKey, this)

                        // findNavController().popBackStack()
                    }
                }

            }
        }
    }
}