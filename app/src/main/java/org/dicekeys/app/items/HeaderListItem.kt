package org.dicekeys.app.items

import android.view.LayoutInflater
import android.view.ViewGroup
import org.dicekeys.app.R
import org.dicekeys.app.databinding.ListItemHeaderBinding
import org.dicekeys.app.databinding.ListItemTitleBinding

class HeaderListItem(val title: String) : GenericListItem<ListItemHeaderBinding>(R.id.list_item_header) {
    override fun bindView(binding: ListItemHeaderBinding) {
        binding.title = title
    }

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ListItemHeaderBinding =
        ListItemHeaderBinding.inflate(inflater, parent, false)
}