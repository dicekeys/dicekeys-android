package org.dicekeys.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import org.dicekeys.api.DerivationRecipe
import org.dicekeys.app.R
import org.dicekeys.app.databinding.ListItemRecipeBinding


class RecipesAdapter(private val clickListener: OnItemClickListener) : RecyclerView.Adapter<RecipesAdapter.RecipesViewHolder>() {
    private var recipes = listOf<DerivationRecipe>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipesViewHolder {
        return RecipesViewHolder(ListItemRecipeBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecipesViewHolder, position: Int) {
        var recipe : DerivationRecipe? = null
        when (position) {
            0 -> {
                holder.binding.name = holder.itemView.context.getString(R.string.custom_recipe)
            }
            1 -> {
                holder.binding.name = holder.itemView.context.getString(R.string.common_password_recipes)
            }
            else -> {
                // Get the actual position in the list minus the hardcoded elements
                recipes[position - HARDCODED_ELEMENTS_SIZE].let {
                    recipe = it
                    holder.binding.name = it.name
                }
            }
        }

        holder.itemView.setOnClickListener {
            clickListener.onItemClicked(holder.itemView, position, recipe)
        }
    }

    /*
     * There are 2 hardcoded elements in the recycler. Custom Recipe & Common password recipes
     */
    override fun getItemCount(): Int = recipes.size + HARDCODED_ELEMENTS_SIZE

    fun set(list: List<DerivationRecipe>) {
        recipes = list
        notifyDataSetChanged()
    }

    class RecipesViewHolder(val binding: ListItemRecipeBinding) : RecyclerView.ViewHolder(binding.root)

    interface OnItemClickListener{
        fun onItemClicked(view: View, position: Int, recipe: DerivationRecipe?)
    }

    companion object {
        const val HARDCODED_ELEMENTS_SIZE = 2
    }
}