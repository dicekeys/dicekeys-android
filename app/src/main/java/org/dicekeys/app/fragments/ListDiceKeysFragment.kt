package org.dicekeys.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.*
import org.dicekeys.app.databinding.ListDicekeysFragmentBinding
import org.dicekeys.app.databinding.ListItemDicekeyBinding
import org.dicekeys.app.encryption.BiometricsHelper
import org.dicekeys.app.encryption.EncryptedDiceKey
import org.dicekeys.app.encryption.EncryptedStorage
import org.dicekeys.app.extensions.clearNavigationResult
import org.dicekeys.app.extensions.getNavigationResult
import org.dicekeys.app.extensions.showPopupMenu
import org.dicekeys.app.repositories.DiceKeyRepository
import org.dicekeys.app.viewmodels.ListDiceKeysViewModel
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.FaceRead
import javax.inject.Inject

@AndroidEntryPoint
class ListDiceKeysFragment : AppFragment<ListDicekeysFragmentBinding>(R.layout.list_dicekeys_fragment) {

    @Inject
    lateinit var encryptedStorage: EncryptedStorage

    @Inject
    lateinit var diceKeyRepository: DiceKeyRepository

    @Inject
    lateinit var biometricsHelper: BiometricsHelper

    val viewModel: ListDiceKeysViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getNavigationResult<String>(ScanFragment.READ_DICEKEY)?.observe(viewLifecycleOwner) { facesReadJsonOrNull ->
            facesReadJsonOrNull?.let { facesReadJson ->
                clearNavigationResult(ScanFragment.READ_DICEKEY)
                FaceRead.diceKeyFromJsonFacesRead(facesReadJson)?.let { diceKey ->
                    diceKeyRepository.set(DiceKey.toDiceKey(diceKey))
                    navigate(ListDiceKeysFragmentDirections.actionListDiceKeysFragmentToDiceKeyRootFragment(diceKeyId = diceKey.keyId))
                }
            }
        }

        binding.load.setOnClickListener {
            navigate(ListDiceKeysFragmentDirections.actionGlobalScanFragment())
        }

        binding.assemble.setOnClickListener {
            navigate(ListDiceKeysFragmentDirections.actionListDiceKeysFragmentToAssembleFragment())
        }

        encryptedStorage.getDiceKeysLiveData().observe(viewLifecycleOwner) {
            it?.let {
                updateDiceKeys(it)
            }
        }
    }

    private fun updateDiceKeys(list: List<EncryptedDiceKey>) {

        // 2 elements are hardcoded in the xml, the rest are dynamically generated
        while (binding.root.childCount > 2) {
            binding.root.removeViewAt(0)
        }

        for ((index, encryptedDiceKey) in list.withIndex()) {
            val diceKeyView = ListItemDicekeyBinding.inflate(LayoutInflater.from(requireContext()))
            diceKeyView.diceKey = encryptedDiceKey

            diceKeyView.centerView.centerFace = encryptedDiceKey.centerFaceAsFace

            diceKeyView.root.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1.0f
            )

            diceKeyView.root.setOnClickListener {
                if (diceKeyRepository.exists(encryptedDiceKey)) {
                    navigate(ListDiceKeysFragmentDirections.actionListDiceKeysFragmentToDiceKeyRootFragment(diceKeyId = encryptedDiceKey.keyId))
                } else {
                    biometricsHelper.decrypt(encryptedDiceKey, this@ListDiceKeysFragment) { diceKey ->
                        diceKeyRepository.set(diceKey)
                        navigate(ListDiceKeysFragmentDirections.actionListDiceKeysFragmentToDiceKeyRootFragment(diceKeyId = diceKey.keyId))
                    }
                }
            }

            diceKeyView.root.setOnLongClickListener {
                showPopupMenu(diceKeyView.root, R.menu.dicekey_popup) { menuItem ->
                    when (menuItem.itemId) {
                        R.id.delete -> {
                            openDialogDeleteDiceKey(requireContext()) {
                                viewModel.remove(encryptedDiceKey)
                            }
                        }
                    }
                    true
                }
                true
            }

            binding.root.addView(diceKeyView.root, index)
        }
    }
}