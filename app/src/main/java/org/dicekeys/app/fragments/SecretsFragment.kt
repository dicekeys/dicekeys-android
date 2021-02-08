package org.dicekeys.app.fragments

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.api.derivationRecipeTemplates
import org.dicekeys.app.AppFragment
import org.dicekeys.app.R
import org.dicekeys.app.adapters.RecipesAdapter
import org.dicekeys.app.databinding.SecretsFragmentBinding
import org.dicekeys.app.repositories.RecipeRepository
import javax.inject.Inject

@AndroidEntryPoint
class SecretsFragment : AppFragment<SecretsFragmentBinding>(R.layout.secrets_fragment) {

    @Inject
    lateinit var recipesRepository: RecipeRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = RecipesAdapter()

        recipesRepository.getRecipesLiveData().observe(viewLifecycleOwner) {
            adapter.set(it)
        }

        binding.recycler.also {
            it.layoutManager = LinearLayoutManager(requireContext())
            it.adapter = adapter
        }
    }
}