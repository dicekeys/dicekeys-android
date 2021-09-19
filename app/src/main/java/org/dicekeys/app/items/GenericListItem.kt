package org.dicekeys.app.items

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class GenericListItem<Binding : ViewBinding> constructor(val itemViewType: Int) {

    open fun bindView(binding: Binding) {

    }

    fun bindView(holder: BindingViewHolder<*>) {
         bindView(holder.binding as Binding)
    }

    /**
     * This method is called by generateView(Context ctx), generateView(Context ctx, ViewGroup parent) and getViewHolder(ViewGroup parent)
     * it will generate the ViewBinding. You have to provide the correct binding class.
     */
    abstract fun createBinding(inflater: LayoutInflater, parent: ViewGroup? = null): Binding

    /** Generates a ViewHolder from this Item with the given parent */
    fun getViewHolder(parent: ViewGroup): BindingViewHolder<Binding> {
        return getViewHolder(createBinding(LayoutInflater.from(parent.context), parent))
    }

    /** Generates a ViewHolder from this Item with the given ViewBinding */
    open fun getViewHolder(viewBinding: Binding): BindingViewHolder<Binding> {
        return BindingViewHolder(viewBinding)
    }

}

open class BindingViewHolder<VB : ViewBinding>(val binding: VB) : RecyclerView.ViewHolder(binding.root)

