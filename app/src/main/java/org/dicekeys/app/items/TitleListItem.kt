package org.dicekeys.app.items

import android.view.LayoutInflater
import android.view.ViewGroup
import org.dicekeys.app.R
import org.dicekeys.app.databinding.ListItemTitleBinding

class TitleListItem(val title: String, val data1: Any? = null, val data2: Any? = null) : GenericListItem<ListItemTitleBinding>(R.id.list_item_title) {
    override fun bindView(binding: ListItemTitleBinding) {
        binding.title = title
    }

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ListItemTitleBinding =
        ListItemTitleBinding.inflate(inflater, parent, false)
}