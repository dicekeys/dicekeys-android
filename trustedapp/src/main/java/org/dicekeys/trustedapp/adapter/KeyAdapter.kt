package org.dicekeys.trustedapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.dicekeys.trustedapp.databinding.ItemKeyBinding


// Currently this adapter is not used, maybe will come in handy after v1
// to have a scroll view for more that 4 elements

class KeyAdapter(private var buttonClickListener: OnButtonClickListener) : RecyclerView.Adapter<KeyAdapter.FaceLatterViewHolder>() {
    private var latters = listOf<Char>()

   // var buttonClickListener: OnButtonClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaceLatterViewHolder {
        return FaceLatterViewHolder(ItemKeyBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: FaceLatterViewHolder, position: Int) {
        val diceKey = latters[position]
        holder.binding.charvalue = diceKey.toString()
        holder.binding.btnLatter.tag=position
        holder.binding.btnLatter.setOnClickListener {
            buttonClickListener?.onClick(it, diceKey)
        }
    }

    fun setOnButtonClickListener(onButtonClickListener: OnButtonClickListener){
        this.buttonClickListener=onButtonClickListener;
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