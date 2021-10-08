package org.dicekeys.app.adapters

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import org.dicekeys.api.DerivationRecipe
import org.dicekeys.app.databinding.ListItemRecipeBinding
import org.dicekeys.app.items.BindingViewHolder
import org.dicekeys.app.items.GenericListItem


class GenericAdapter(private val clickListener: OnItemClickListener? = null) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var items = listOf<GenericListItem<*>>()

    override fun getItemViewType(position: Int) = items[position].itemViewType

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return items.find { it.itemViewType == viewType }!!.getViewHolder(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        items[position].bindView(holder as BindingViewHolder<*>)

        clickListener?.let { clickListener ->
            holder.binding.root.setOnClickListener {
                clickListener.onItemClicked(it, position, items[position])
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun set(list: List<GenericListItem<*>>) {
        items = list
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClicked(view: View, position: Int, item: GenericListItem<*>)
    }
}