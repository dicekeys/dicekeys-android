package org.dicekeys.app.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.dicekeys.app.databinding.ItemKeyBinding


class KeyAdapter(private var buttonClickListener: OnButtonClickListener) : RecyclerView.Adapter<KeyAdapter.FaceLatterViewHolder>() {
    private var latters = listOf<Char>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaceLatterViewHolder {
        return FaceLatterViewHolder(ItemKeyBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: FaceLatterViewHolder, position: Int) {
        val diceKey = latters[position]
        holder.binding.value = diceKey.toString()
        holder.binding.root.setOnClickListener {
            buttonClickListener.onClick(it, diceKey)
        }
    }

    override fun getItemCount(): Int = latters.size

    fun set(list: List<Char>) {
        latters = list
        notifyDataSetChanged()
    }

    class FaceLatterViewHolder(val binding: ItemKeyBinding) : RecyclerView.ViewHolder(binding.root)

    interface OnButtonClickListener {
        fun onClick(view: View, char: Char)
    }
}