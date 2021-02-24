package org.dicekeys.app.fragments.dicekey

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.api.DerivationRecipe
import org.dicekeys.api.derivationRecipeTemplates
import org.dicekeys.app.AppFragment
import org.dicekeys.app.R
import org.dicekeys.app.adapters.RecipesAdapter
import org.dicekeys.app.databinding.SecretsFragmentBinding
import org.dicekeys.app.extensions.toast
import org.dicekeys.app.repositories.RecipeRepository
import org.dicekeys.app.viewmodels.DiceKeyViewModel
import javax.inject.Inject

@AndroidEntryPoint
class SecretsFragment : AbstractDiceKeyFragment<SecretsFragmentBinding>(R.layout.secrets_fragment), RecipesAdapter.OnItemClickListener {

    @Inject
    lateinit var recipesRepository: RecipeRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = RecipesAdapter(this)

        recipesRepository.getRecipesLiveData().observe(viewLifecycleOwner) {
            adapter.set(it)
        }

        binding.recycler.also {
            it.layoutManager = LinearLayoutManager(requireContext())
            it.adapter = adapter
        }
    }

    override fun onItemClicked(view: View, position: Int, recipe: DerivationRecipe?) {
        when (position) {
            0 -> {
                navigate(SecretsFragmentDirections.actionSecretsToRecipeFragment())
            }
            1 -> {
                val popupMenu = PopupMenu(requireContext(), view)
                val menu = popupMenu.menu

                for ((index, recipes) in derivationRecipeTemplates.withIndex()) {
                    menu.add(0, 0 , index, recipes.name)
                }

                popupMenu.setOnMenuItemClickListener { item ->
                    val derivationRecipe = derivationRecipeTemplates[item.order]
                    navigate(SecretsFragmentDirections.actionSecretsToRecipeFragment(template = derivationRecipe))
                    true
                }
                popupMenu.show()

            }
            else -> {
                recipe?.let {
                    navigate(SecretsFragmentDirections.actionSecretsToRecipeFragment(recipe = it))
                }
            }
        }
    }
}