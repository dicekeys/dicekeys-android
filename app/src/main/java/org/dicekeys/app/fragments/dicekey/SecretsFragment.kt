package org.dicekeys.app.fragments.dicekey

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.api.DerivationRecipe
import org.dicekeys.api.derivationRecipeTemplates
import org.dicekeys.app.R
import org.dicekeys.app.adapters.GenericAdapter
import org.dicekeys.app.databinding.SecretsFragmentBinding
import org.dicekeys.app.extensions.description
import org.dicekeys.app.items.GenericListItem
import org.dicekeys.app.items.HeaderListItem
import org.dicekeys.app.items.TitleListItem
import org.dicekeys.app.repositories.RecipeRepository
import org.dicekeys.crypto.seeded.DerivationOptions
import javax.inject.Inject

@AndroidEntryPoint
class SecretsFragment : AbstractDiceKeyFragment<SecretsFragmentBinding>(R.layout.secrets_fragment),
    GenericAdapter.OnItemClickListener {

    @Inject
    lateinit var recipesRepository: RecipeRepository


    override fun onViewCreatedGuarded(view: View, savedInstanceState: Bundle?) {
        val adapter = GenericAdapter(this)

        recipesRepository.getRecipesLiveData().observe(viewLifecycleOwner) {
            updateAdater(adapter, it)
        }

        updateAdater(adapter, null)

        binding.recycler.also {
            it.layoutManager = LinearLayoutManager(requireContext())
            it.adapter = adapter
        }
    }

    private fun updateAdater(adapter: GenericAdapter, savedRecipes: List<DerivationRecipe>?) {
        val list = mutableListOf<GenericListItem<*>>()
        if (!savedRecipes.isNullOrEmpty()) {
            list += HeaderListItem(getString(R.string.saved_recipes))
            list += savedRecipes.map {
                TitleListItem(it.name, data1 = it)
            }
        }

        list += HeaderListItem(getString(R.string.build_in_recipes))
        list += derivationRecipeTemplates.map {
            // mark it as template
            TitleListItem(it.name, data1 = it, data2 = true)
        }

        list += HeaderListItem(getString(R.string.custom_recipe))
        list += TitleListItem(DerivationOptions.Type.Password.description(), data1 = DerivationOptions.Type.Password)
        list += TitleListItem(DerivationOptions.Type.Secret.description(), data1 = DerivationOptions.Type.Secret)
        list += TitleListItem(DerivationOptions.Type.SigningKey.description(), data1 = DerivationOptions.Type.SigningKey)
        list += TitleListItem(DerivationOptions.Type.SymmetricKey.description(), data1 = DerivationOptions.Type.SymmetricKey)
        list += TitleListItem(DerivationOptions.Type.UnsealingKey.description(), data1 = DerivationOptions.Type.UnsealingKey)

        adapter.set(list)
    }

    override fun onItemClicked(view: View, position: Int, item: GenericListItem<*>) {
        if(item is TitleListItem){
            if(item.data1 is DerivationRecipe) {
                if ((item.data2 as? Boolean) == true) {
                    navigate(SecretsFragmentDirections.actionSecretsToRecipeFragment(recipe = item.data1, deriveType = item.data1.type, editable = true))
                } else {
                    navigate(SecretsFragmentDirections.actionSecretsToRecipeFragment(recipe = item.data1, deriveType = item.data1.type, editable = false))
                }
            }else if(item.data1 is DerivationOptions.Type){
                EditRecipeBottomSheet.show(item.data1, childFragmentManager)
            }
        }
    }
}