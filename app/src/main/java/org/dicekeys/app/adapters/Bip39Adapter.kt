package org.dicekeys.app.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.dicekeys.app.bip39.Mnemonics
import org.dicekeys.app.databinding.ItemBip39WordBinding
import org.dicekeys.app.databinding.ItemKeyBinding


class Bip39Adapter() : RecyclerView.Adapter<Bip39Adapter.Bip39WordViewHolder>() {
    var words : List<CharArray>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Bip39WordViewHolder {
        return Bip39WordViewHolder(ItemBip39WordBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }


    override fun onBindViewHolder(holder: Bip39WordViewHolder, position: Int) {
        holder.binding.number = "%2d".format(position + 1)
        words?.get(position)?.let{
            holder.binding.word = String( it)
        }

    }

    override fun getItemCount(): Int = words?.size ?: 0

    fun set(mnemonicCode: Mnemonics.MnemonicCode) {
        words = mnemonicCode.words
        notifyDataSetChanged()
    }

    class Bip39WordViewHolder(val binding: ItemBip39WordBinding) : RecyclerView.ViewHolder(binding.root)
}