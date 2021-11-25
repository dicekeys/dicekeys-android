package org.dicekeys.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.activityViewModels
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
import org.dicekeys.app.viewmodels.DiceKeyViewModel
import org.dicekeys.app.viewmodels.ListDiceKeysViewModel
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face
import org.dicekeys.dicekey.FaceRead
import javax.inject.Inject

@AndroidEntryPoint
class ListDiceKeysFragment : AbstractListDiceKeysFragment<ListDicekeysFragmentBinding>(R.layout.list_dicekeys_fragment, R.menu.preferences) {

    private val listViewModel: ListDiceKeysViewModel by viewModels()

    override val staticViewsCount: Int = 2

    override val linearLayoutContainer: LinearLayout
        get() = binding.container

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getNavigationResult<String>(ScanFragment.READ_DICEKEY)?.observe(viewLifecycleOwner) { humanReadableOrNull ->
            humanReadableOrNull?.let { humanReadable ->
                clearNavigationResult(ScanFragment.READ_DICEKEY)
                navigateToDiceKey(DiceKey.fromHumanReadableForm(humanReadable))
            }
        }

        binding.load.setOnClickListener {
            navigate(ListDiceKeysFragmentDirections.actionGlobalScanFragment(showEnterByHand = true))
        }

        binding.assemble.setOnClickListener {
            navigate(ListDiceKeysFragmentDirections.actionListDiceKeysFragmentToAssembleFragment())
        }

        navigateToDiceKey(DiceKey.example)
    }

    override fun clickOnDiceKey(view: View, encryptedDiceKey: EncryptedDiceKey) {
        val diceKey = listViewModel.getDiceKey(encryptedDiceKey)

        // Needs Decryption
        if(diceKey == null){
            biometricsHelper.decrypt(encryptedDiceKey, this@ListDiceKeysFragment) { diceKey ->
                navigateToDiceKey(diceKey)
            }
        }else{
            navigateToDiceKey(diceKey)
        }
    }

    override fun longClickOnDiceKey(view: View, encryptedDiceKey: EncryptedDiceKey) {
        showPopupMenu(view, R.menu.dicekey_popup) { menuItem ->
            when (menuItem.itemId) {
                R.id.delete -> {
                    openDialogDeleteDiceKey(requireContext()) {
                        listViewModel.remove(encryptedDiceKey)
                    }
                }
            }
            true
        }
        true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when(item.itemId){
            R.id.preferences -> {
                navigate(R.id.preferencesFragment)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun navigateToDiceKey(diceKey: DiceKey<Face>){
        diceKeyRepository.set(diceKey)
        navigate(ListDiceKeysFragmentDirections.actionGlobalDicekey())
    }
}