package org.dicekeys.app.fragments.dicekey

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.R
import org.dicekeys.app.databinding.RecipeFragmentBinding
import org.dicekeys.app.extensions.toast
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
        RecipeViewModel.provideFactory(assistedFactory = viewModelFactory, diceKey = viewModel.diceKey.value!!, recipe = args.recipe, template = args.template)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.diceKeyVM = viewModel
        binding.vm = recipeViewModel

        binding.btnDown.setOnClickListener { recipeViewModel.sequencUpDown(false) }
        binding.btnUp.setOnClickListener { recipeViewModel.sequencUpDown(true) }

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

        binding.tvPassword.setOnClickListener {
            recipeViewModel.password.value?.let {
                copyToClipboard("password", it, requireContext())
                toast(R.string.password_copied)
            }
        }

        binding.btnSaveRecipeInMenu.setOnClickListener {
            recipeViewModel.saveRecipeInMenu()
        }

        binding.btnRemoveRecipeFromMenu.setOnClickListener {
            recipeViewModel.removeRecipeFromMenu()
        }
    }
}