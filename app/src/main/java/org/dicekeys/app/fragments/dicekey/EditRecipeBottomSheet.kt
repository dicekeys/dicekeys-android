package org.dicekeys.app.fragments.dicekey

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.R
import org.dicekeys.app.RecipeBuilder
import org.dicekeys.app.databinding.EditRecipeBottomSheetFragmentBinding
import org.dicekeys.app.viewmodels.EditRecipeViewModel
import org.dicekeys.app.viewmodels.RecipeViewModel
import javax.inject.Inject

@AndroidEntryPoint
class EditRecipeBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: EditRecipeBottomSheetFragmentBinding

    private val recipeViewModel: RecipeViewModel by lazy {
        (requireParentFragment() as RecipeFragment).recipeViewModel
    }

    @Inject
    lateinit var viewModelFactory: EditRecipeViewModel.AssistedFactory

    val viewModel: EditRecipeViewModel by viewModels {
        EditRecipeViewModel.provideFactory(
            assistedFactory = viewModelFactory,
            recipeBuilder = recipeViewModel.recipeBuilder!!,
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.edit_recipe_bottom_sheet_fragment,
            container,
            false
        )
        binding.lifecycleOwner = viewLifecycleOwner
        binding.vm = viewModel

        binding.toggleButton.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if(checkedId == R.id.buttonWebAddress){
                viewModel.recipeBuilder.buildType.postValue(RecipeBuilder.BuildType.Online)
            }else{
                viewModel.recipeBuilder.buildType.postValue(RecipeBuilder.BuildType.Purpose)
            }
        }

        if(viewModel.recipeBuilder.buildType.value != RecipeBuilder.BuildType.Raw) {
            binding.toggleButton.check(if (viewModel.recipeBuilder.buildType.value == RecipeBuilder.BuildType.Online) R.id.buttonWebAddress else R.id.buttonPurpose)
        }

        binding.buttonOk.setOnClickListener {
            dismiss()
        }

        binding.buttonRawJson.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Edit Raw Json")
                .setMessage("Entering a recipe in raw JSON format can be dangerous. \n\nIf you enter a recipe provided by someone else, it could be a trick to get you to re-create a secret you use for another application or purpose.\n\nIf you generate the recipe yourself and forget even a single character, you will be unable to re-generate the same secret again. (Saving the recipe won't help you if you lose the device(s) it's saved on.)")
                .setPositiveButton("I accept the risk") { _: DialogInterface, _: Int ->
                    viewModel.editRawJson()
                    true
                }
                .setNegativeButton(R.string.cancel) { _: DialogInterface, _: Int ->
                    true
                }
                .show()

        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setSoftInputMode(SOFT_INPUT_ADJUST_RESIZE)
    }
}