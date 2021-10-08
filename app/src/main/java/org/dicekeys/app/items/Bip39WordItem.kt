package org.dicekeys.app.items

import android.view.LayoutInflater
import android.view.ViewGroup
import org.dicekeys.app.R
import org.dicekeys.app.databinding.ListItemBip39WordBinding
import org.dicekeys.app.databinding.ListItemTitleBinding

class Bip39WordItem constructor(val index: String, val word: String) : GenericListItem<ListItemBip39WordBinding>(R.id.list_item_bip39_word) {
    override fun bindView(binding: ListItemBip39WordBinding) {
        binding.word = word
        binding.index = index
    }

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ListItemBip39WordBinding =
        ListItemBip39WordBinding.inflate(inflater, parent, false)
}