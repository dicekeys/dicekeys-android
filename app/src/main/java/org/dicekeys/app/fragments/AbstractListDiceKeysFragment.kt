package org.dicekeys.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import androidx.annotation.MenuRes
import androidx.databinding.ViewDataBinding
import org.dicekeys.app.AppFragment
import org.dicekeys.app.R
import org.dicekeys.app.data.DiceKeyDescription
import org.dicekeys.app.databinding.ListItemDicekeyBinding
import org.dicekeys.app.encryption.BiometricsHelper
import org.dicekeys.app.encryption.EncryptedDiceKey
import org.dicekeys.app.encryption.EncryptedStorage
import org.dicekeys.app.repositories.DiceKeyRepository
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face
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

    abstract fun clickOnDiceKey(view: View, diceKeyDescription: DiceKeyDescription)
    abstract fun longClickOnDiceKey(view: View, diceKeyDescription : DiceKeyDescription)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        diceKeyRepository.availableDiceKeys.observe(viewLifecycleOwner) {
            it?.let {
                updateDiceKeys(it)
            }
        }
    }

    private fun updateDiceKeys(list: List<DiceKeyDescription>) {
        // 2 elements are hardcoded in the xml, the rest are dynamically generated
        while (linearLayoutContainer.childCount > staticViewsCount) {
            linearLayoutContainer.removeViewAt(0)
        }

        for (diceKey in list) {
            addDiceKeyView(diceKey)
        }
    }

    private fun addDiceKeyView(diceKeyDescription: DiceKeyDescription){
        val diceKeyView = ListItemDicekeyBinding.inflate(LayoutInflater.from(requireContext()))


        diceKeyView.title = getString(
            if (diceKeyRepository.exists(diceKeyDescription)) {
                R.string.open_dicekey_with_center
            } else {
                R.string.unlock_dicekey_with_center
            }, diceKeyDescription.centerFaceAsFace.toHumanReadableForm(false)
        )

        diceKeyView.centerView.centerFace = diceKeyDescription.centerFaceAsFace

        diceKeyView.root.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1.0f
        )


        diceKeyView.root.setOnClickListener {
            clickOnDiceKey(it, diceKeyDescription)
        }

        diceKeyView.root.setOnLongClickListener {
            longClickOnDiceKey(it, diceKeyDescription)
            true
        }

        linearLayoutContainer.addView(diceKeyView.root, linearLayoutContainer.childCount - staticViewsCount)
    }
}