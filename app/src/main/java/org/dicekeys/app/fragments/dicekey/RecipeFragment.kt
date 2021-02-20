package org.dicekeys.app.fragments.dicekey

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.AppFragment
import org.dicekeys.app.R
import org.dicekeys.app.databinding.RecipeFragmentBinding
import org.dicekeys.app.extensions.toast
import org.dicekeys.app.repositories.DiceKeyRepository
import org.dicekeys.app.repositories.RecipeRepository
import org.dicekeys.app.utils.copyToClipboard
import org.dicekeys.app.viewmodels.RecipeViewModel
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face
import javax.inject.Inject

@AndroidEntryPoint
class RecipeFragment : AbstractDiceKeyFragment<RecipeFragmentBinding>(R.layout.recipe_fragment) {

    private val args: RecipeFragmentArgs by navArgs()

    @Inject
    lateinit var recipeRepository: RecipeRepository

    @Inject
    lateinit var viewModelFactory: RecipeViewModel.AssistedFactory

    private val recipeViewModel: RecipeViewModel by viewModels {
        RecipeViewModel.provideFactory(viewModelFactory, viewModel.diceKey, args.recipe, args.template)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.diceKeyVM = viewModel
        binding.vm = recipeViewModel


        binding.btnDown.setOnClickListener { recipeViewModel.sequencUpDown(false) }
        binding.btnUp.setOnClickListener { recipeViewModel.sequencUpDown(true) }
        binding.etSequenceNumber.doAfterTextChanged { edittext ->
            try{
                recipeViewModel.updateSequence(edittext.toString().toInt())
            }catch (e: Exception){
                e.printStackTrace()
            }
        }

        binding.tvPassword.setOnClickListener {
            recipeViewModel.password.value?.let {
                copyToClipboard("password", it, requireContext())
                toast("Password copied")
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