package org.dicekeys.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.dicekeys.app.R
import org.dicekeys.app.databinding.ListItemDicekeyBinding
import org.dicekeys.app.encryption.EncryptedDiceKey
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.SimpleDiceKey

// Currently this adapter is not used, maybe will come in handy after v1
// to have a scroll view for more that 4 elements
@Deprecated("Not used")
class DiceKeysAdapter : RecyclerView.Adapter<DiceKeysAdapter.DiceKeyViewHolder>() {
    private var diceKeys = listOf<EncryptedDiceKey>()

    var diceKeyClickListener: OnDiceKeyClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiceKeyViewHolder {
        return DiceKeyViewHolder(ListItemDicekeyBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: DiceKeyViewHolder, position: Int) {
        val diceKey = diceKeys[position]
        holder.binding.title = holder.itemView.context.getString(R.string.unlock_dicekey_with_center, diceKey.centerFace)

        holder.binding.root.setOnClickListener {
            diceKeyClickListener?.onClick(it, diceKey)
        }

        holder.binding.root.setOnLongClickListener {
            diceKeyClickListener?.onLongClick(it, diceKey)
            true
        }
    }

    override fun getItemCount(): Int = diceKeys.size


    fun set(list: List<EncryptedDiceKey>) {
        diceKeys = list
        notifyDataSetChanged()
    }

    class DiceKeyViewHolder(val binding: ListItemDicekeyBinding) : RecyclerView.ViewHolder(binding.root)

    interface OnDiceKeyClickListener {
        fun onClick(view: View, diceKey: EncryptedDiceKey)
        fun onLongClick(view: View, diceKey: EncryptedDiceKey)
    }
}