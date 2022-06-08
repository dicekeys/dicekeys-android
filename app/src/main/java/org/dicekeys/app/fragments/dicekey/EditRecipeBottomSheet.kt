package org.dicekeys.app.fragments.dicekey

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.api.DerivationRecipe
import org.dicekeys.app.R
import org.dicekeys.app.RecipeBuilder
import org.dicekeys.app.databinding.EditRecipeBottomSheetFragmentBinding
import org.dicekeys.app.extensions.showPopupMenu
import org.dicekeys.app.openDialogDeleteDiceKey
import org.dicekeys.app.viewmodels.EditRecipeViewModel
import org.dicekeys.app.viewmodels.RecipeViewModel
import org.dicekeys.crypto.seeded.DerivationOptions
import javax.inject.Inject

@AndroidEntryPoint
class EditRecipeBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: EditRecipeBottomSheetFragmentBinding

    @Inject
    lateinit var viewModelFactory: EditRecipeViewModel.AssistedFactory
    val type by lazy {
        arguments?.getParcelable<DerivationOptions.Type>(TYPE) ?: error("Type was not provided")
    }

    val viewModel: EditRecipeViewModel by viewModels {
        EditRecipeViewModel.provideFactory(
            assistedFactory = viewModelFactory,
            type = type,
            initRecipeBuilder = null //(parentFragment as? RecipeFragment)?.recipeViewModel?.recipeBuilder,
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


        binding.buttonSequenceUp.setOnClickListener {
            viewModel.recipeBuilder.sequenceUp()
        }

        binding.buttonSequenceDown.setOnClickListener {
            viewModel.recipeBuilder.sequenceDown()
        }

        binding.toggleButton.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                if (checkedId == R.id.buttonWebAddress) {
                    viewModel.recipeBuilder.buildType.postValue(RecipeBuilder.BuildType.Online)
                } else {
                    viewModel.recipeBuilder.buildType.postValue(RecipeBuilder.BuildType.Purpose)
                }
            }
        }

        if (viewModel.recipeBuilder.buildType.value != RecipeBuilder.BuildType.Raw) {
            binding.toggleButton.check(if (viewModel.recipeBuilder.buildType.value == RecipeBuilder.BuildType.Online) R.id.buttonWebAddress else R.id.buttonPurpose)
        }

        binding.buttonOk.setOnClickListener {
            viewModel.recipeBuilder. build()?.also {
                (parentFragment as SecretsFragment).navigate(SecretsFragmentDirections.actionSecretsToRecipeFragment(recipe = it, deriveType = it.type, editable = false))
            }

            dismiss()
        }

        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        binding.buttonMenu.setOnClickListener {
            showPopupMenu(it, R.menu.edit_recipe_popup, { popupMenu ->
                popupMenu.menu.findItem(R.id.edit_json).isVisible = viewModel.recipeBuilder.buildType.value != RecipeBuilder.BuildType.Raw
                popupMenu.menu.findItem(R.id.cancel_edit).isVisible = viewModel.recipeBuilder.buildType.value == RecipeBuilder.BuildType.Raw
            }) { menuItem ->
                when (menuItem.itemId) {
                    R.id.edit_json -> {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Edit Raw Json")
                            .setMessage("Entering a recipe in raw JSON format can be dangerous. \n\nIf you enter a recipe provided by someone else, it could be a trick to get you to re-create a secret you use for another application or purpose.\n\nIf you generate the recipe yourself and forget even a single character, you will be unable to re-generate the same secret again. (Saving the recipe won't help you if you lose the device(s) it's saved on.)")
                            .setPositiveButton("I accept the risk") { _: DialogInterface, _: Int ->
                                viewModel.editRawJson(true)
                                true
                            }
                            .setNegativeButton(R.string.cancel) { _: DialogInterface, _: Int ->
                                true
                            }
                            .show()
                    }
                    R.id.cancel_edit -> {
                        viewModel.editRawJson(false)
                        binding.toggleButton.check(if (viewModel.recipeBuilder.buildType.value == RecipeBuilder.BuildType.Online) R.id.buttonWebAddress else R.id.buttonPurpose)
                    }
                }
                true
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setSoftInputMode(SOFT_INPUT_ADJUST_RESIZE)
    }

    companion object {
        private const val TYPE = "TYPE"

        fun show(type: DerivationOptions.Type, fragmentManager: FragmentManager) {
            EditRecipeBottomSheet().also {
                it.arguments = Bundle().also { bundle ->
                    bundle.putParcelable(TYPE, type)
                }
            }.also {
                it.show(fragmentManager, it.toString())
            }
        }
    }
}