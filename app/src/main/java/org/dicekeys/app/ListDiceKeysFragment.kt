package org.dicekeys.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.adapters.DiceKeysAdapter
import org.dicekeys.app.databinding.ListDicekeysFragmentBinding
import org.dicekeys.app.encryption.BiometricsHelper
import org.dicekeys.app.encryption.EncryptedDiceKey
import org.dicekeys.app.encryption.EncryptedStorage
import org.dicekeys.app.extensions.showPopupMenu
import org.dicekeys.app.repositories.DiceKeyRepository
import org.dicekeys.dicekey.FaceRead
import org.dicekeys.read.ReadDiceKeyActivity
import javax.inject.Inject

@AndroidEntryPoint
class ListDiceKeysFragment: AppFragment<ListDicekeysFragmentBinding>(R.layout.list_dicekeys_fragment) {

    @Inject
    lateinit var encryptedStorage: EncryptedStorage

    @Inject
    lateinit var diceKeyRepository: DiceKeyRepository

    @Inject
    lateinit var biometricsHelper: BiometricsHelper

    lateinit var adapter: DiceKeysAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonRead.setOnClickListener {
            val intent = Intent(requireContext(), ReadDiceKeyActivity::class.java)
            startActivityForResult(intent, MainActivity.READ_DICE_REQUEST_CODE)
        }

        adapter = DiceKeysAdapter().also {
            it.diceKeyClickListener = object : DiceKeysAdapter.OnDiceKeyClickListener {

                override fun onClick(view: View, diceKey: EncryptedDiceKey) {
                    biometricsHelper.decrypt(diceKey, this@ListDiceKeysFragment) { diceKey ->

                        diceKeyRepository.set(diceKey)
                        navigate(ListDiceKeysFragmentDirections.actionListDiceKeysFragmentToDiceKeyFragment(diceKeyId = diceKey.keyId))
                    }
                }

                override fun onLongClick(view: View, diceKey: EncryptedDiceKey) {
                    showPopupMenu(view, R.menu.dicekey_popup) { menuItem ->
                        when(menuItem.itemId){
                            R.id.delete -> {
                                encryptedStorage.remove(diceKey)
                            }
                        }
                        true
                    }
                }
            }
        }

        encryptedStorage.getDiceKeysLiveData().observe(viewLifecycleOwner){
            it?.let {
                adapter.set(it)
            }
        }

        binding.recycler.also {
            it.layoutManager = LinearLayoutManager(requireContext())
            it.adapter = adapter
        }
    }

    // Needs refactoring
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == MainActivity.READ_DICE_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK && data != null){

                data.getStringExtra(ReadDiceKeyActivity.Companion.Parameters.Response.diceKeyAsJson)?.let { diceKeyAsJson ->
                    FaceRead.diceKeyFromJsonFacesRead(diceKeyAsJson)?.let { diceKey ->
                        diceKeyRepository.set(diceKey)
                        navigate(ListDiceKeysFragmentDirections.actionListDiceKeysFragmentToDiceKeyFragment(diceKeyId = diceKey.keyId))
                    }
                }

            }
        }
    }
}