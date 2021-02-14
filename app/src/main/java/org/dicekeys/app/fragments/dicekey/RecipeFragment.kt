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
class RecipeFragment : AppFragment<RecipeFragmentBinding>(R.layout.recipe_fragment) {

    @Inject
    lateinit var repository: DiceKeyRepository

    lateinit var diceKey: DiceKey<Face>

    private val args: RecipeFragmentArgs by navArgs()

    @Inject
    lateinit var recipeRepository: RecipeRepository

    @Inject
    lateinit var viewModelFactory: RecipeViewModel.AssistedFactory

    val viewModel: RecipeViewModel by viewModels {
        RecipeViewModel.provideFactory(viewModelFactory, diceKey, args.recipe, args.template)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Guard: If DiceKey is not available, return
        repository.get(args.diceKeyId)?.also {
            diceKey = it as DiceKey<Face>
        } ?: run {
            findNavController().popBackStack()
            return
        }

        binding.diceKeyViewModel = getDiceKeyRootFragment().viewModel

        binding.vm = viewModel

        binding.dicekey.diceKey = viewModel.diceKey


        binding.btnDown.setOnClickListener { viewModel.sequencUpDown(false) }
        binding.btnUp.setOnClickListener { viewModel.sequencUpDown(true) }

        binding.etSequenceNumber.doAfterTextChanged { edittext ->
            try{
                viewModel.updateSequence(edittext.toString().toInt())
            }catch (e: Exception){
                e.printStackTrace()
            }
        }

        binding.tvPassword.setOnClickListener {
            viewModel.password.value?.let {
                copyToClipboard("password", it, requireContext())
                toast("Password copied")
            }
        }

        binding.btnSaveRecipeInMenu.setOnClickListener {
            viewModel.saveRecipeInMenu()
        }

        binding.btnRemoveRecipeFromMenu.setOnClickListener {
            viewModel.removeRecipeFromMenu()
        }
    }
}