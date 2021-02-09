package org.dicekeys.app.fragments

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.api.DerivationRecipe
import org.dicekeys.app.AppFragment
import org.dicekeys.app.R
import org.dicekeys.app.adapters.RecipesAdapter
import org.dicekeys.app.databinding.SecretsFragmentBinding
import org.dicekeys.app.extensions.toast
import org.dicekeys.app.repositories.RecipeRepository
import org.dicekeys.app.viewmodels.DiceKeyViewModel
import javax.inject.Inject

@AndroidEntryPoint
class SecretsFragment : AppFragment<SecretsFragmentBinding>(R.layout.secrets_fragment), RecipesAdapter.OnItemClickListener {

    @Inject
    lateinit var recipesRepository: RecipeRepository

    private lateinit var viewModel: DiceKeyViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (parentFragment as MainDiceKeyFragment).viewModel

        val adapter = RecipesAdapter(this)

        recipesRepository.getRecipesLiveData().observe(viewLifecycleOwner) {
            adapter.set(it)
        }

        binding.recycler.also {
            it.layoutManager = LinearLayoutManager(requireContext())
            it.adapter = adapter
        }
    }

    override fun onItemClicked(position: Int, recipe: DerivationRecipe?) {
        when (position) {
            0 -> {
                // POP UP
                toast("Custom Recipes")
            }
            1 -> {
                // POP UP
                toast("Common Password Recipes")
            }
            else -> {
                recipe?.let {
                    navigate(MainDiceKeyFragmentDirections.actionMainDiceKeyRootFragmentToRecipeFragment(viewModel.diceKey.keyId, recipe = it))
                }

            }
        }
    }
}