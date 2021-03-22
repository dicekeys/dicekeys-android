package org.dicekeys.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import androidx.annotation.MenuRes
import androidx.databinding.ViewDataBinding
import org.dicekeys.app.AppFragment
import org.dicekeys.app.databinding.ListItemDicekeyBinding
import org.dicekeys.app.encryption.BiometricsHelper
import org.dicekeys.app.encryption.EncryptedDiceKey
import org.dicekeys.app.encryption.EncryptedStorage
import org.dicekeys.app.repositories.DiceKeyRepository
import javax.inject.Inject

abstract class AbstractListDiceKeysFragment<T : ViewDataBinding>(
        @LayoutRes layout: Int,
        @MenuRes menuRes: Int
) : AppFragment<T>(layout, menuRes) {

    @Inject
    lateinit var encryptedStorage: EncryptedStorage

    @Inject
    lateinit var diceKeyRepository: DiceKeyRepository

    @Inject
    lateinit var biometricsHelper: BiometricsHelper

    abstract val staticViewsCount : Int
    abstract val linearLayoutContainer: LinearLayout

    abstract fun clickOnDiceKey(encryptedDiceKey : EncryptedDiceKey)
    abstract fun longClickOnDiceKey(encryptedDiceKey : EncryptedDiceKey)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        encryptedStorage.getDiceKeysLiveData().observe(viewLifecycleOwner) {
            it?.let {
                updateDiceKeys(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // Update DiceKeys list as could have been kicked from memory
        encryptedStorage.getDiceKeysLiveData().value?.let {
            updateDiceKeys(it)
        }
    }

    private fun updateDiceKeys(list: List<EncryptedDiceKey>) {
        // 2 elements are hardcoded in the xml, the rest are dynamically generated
        while (staticViewsCount > 2) {
            linearLayoutContainer.removeViewAt(0)
        }

        for ((index, encryptedDiceKey) in list.withIndex()) {
            val diceKeyView = ListItemDicekeyBinding.inflate(LayoutInflater.from(requireContext()))
            diceKeyView.diceKey = encryptedDiceKey
            diceKeyView.isInMemory = diceKeyRepository.exists(encryptedDiceKey)

            diceKeyView.centerView.centerFace = encryptedDiceKey.centerFaceAsFace

            diceKeyView.root.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1.0f
            )

            diceKeyView.root.setOnClickListener {
                clickOnDiceKey(encryptedDiceKey)
            }

            diceKeyView.root.setOnLongClickListener {
                longClickOnDiceKey(encryptedDiceKey)
                true
            }

            linearLayoutContainer.addView(diceKeyView.root, index)
        }
    }


}