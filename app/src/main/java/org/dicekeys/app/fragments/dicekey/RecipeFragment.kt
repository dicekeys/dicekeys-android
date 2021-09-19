package org.dicekeys.app.fragments.dicekey

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.R
import org.dicekeys.app.databinding.RecipeFragmentBinding
import org.dicekeys.app.repositories.RecipeRepository
import org.dicekeys.app.utils.copyToClipboard
import org.dicekeys.app.viewmodels.RecipeViewModel
import javax.inject.Inject

@AndroidEntryPoint
class RecipeFragment : AbstractDiceKeyFragment<RecipeFragmentBinding>(R.layout.recipe_fragment) {

    private val args: RecipeFragmentArgs by navArgs()

    @Inject
    lateinit var recipeRepository: RecipeRepository

    @Inject
    lateinit var viewModelFactory: RecipeViewModel.AssistedFactory

    private val recipeViewModel: RecipeViewModel by viewModels {
        RecipeViewModel.provideFactory(assistedFactory = viewModelFactory, diceKey = viewModel.diceKey.value!!, recipe = args.recipe, template = args.template, args.deriveType)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(isGuarded) return

        binding.diceKeyVM = viewModel
        binding.vm = recipeViewModel

        binding.btnDown.setOnClickListener { recipeViewModel.sequencUpDown(false) }
        binding.btnUp.setOnClickListener { recipeViewModel.sequencUpDown(true) }


        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, mutableListOf<String>())
        binding.type.setAdapter(adapter)

        binding.type.setOnItemClickListener { _, _, position, id ->
            recipeViewModel.derivedValue.value?.views?.get(position)?.let{
                recipeViewModel.setView(it)
            }
        }

        recipeViewModel.derivedValue.observe(viewLifecycleOwner){
            adapter.clear()
            val list = it.views.map { it.description }
            adapter.addAll(list)

            val currentSelection = binding.type.text.toString()
            if(currentSelection.isBlank() || list.find { it == currentSelection } == null){
                binding.type.setText(it.views[0].description, false)
                recipeViewModel.setView(it.views[0])
            }
        }

        binding.etSequenceNumber.doAfterTextChanged { edittext ->
            var seq = 1
            try {
                seq = edittext.toString().toInt()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            recipeViewModel.updateSequence(seq)
        }

        binding.domains.doAfterTextChanged { edittext ->
            try {
                recipeViewModel.updateDomains(edittext.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.maxChars.doAfterTextChanged { edittext ->
            var length = 0
            try {
                length = edittext.toString().toInt()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            recipeViewModel.updateLengthInChars(length)
        }

        binding.derivedValue.setOnClickListener {
            recipeViewModel.derivedValueAsString.value?.let {
                copyToClipboard("password", it, requireContext(), binding.derivedValue)
            }
        }

        binding.btnSaveRecipeInMenu.setOnClickListener {
            recipeViewModel.saveRecipeInMenu()
        }

        binding.btnRemoveRecipeFromMenu.setOnClickListener {
            recipeViewModel.removeRecipeFromMenu()
        }

        binding.dicekey.setOnClickListener {
            viewModel.toggleHideFaces()
        }
    }
}