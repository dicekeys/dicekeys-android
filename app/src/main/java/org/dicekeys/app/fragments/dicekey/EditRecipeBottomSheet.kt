package org.dicekeys.app.fragments.dicekey

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.dicekeys.app.R
import org.dicekeys.app.databinding.EditRecipeBottomSheetFragmentBinding
import org.dicekeys.app.viewmodels.RecipeViewModel

class EditRecipeBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: EditRecipeBottomSheetFragmentBinding

    internal val viewModel: RecipeViewModel by lazy {
        (requireParentFragment() as RecipeFragment).recipeViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.edit_recipe_bottom_sheet_fragment,
            container,
            false
        )
        binding.lifecycleOwner = viewLifecycleOwner

        binding.vm = viewModel

        binding.toggleButton.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if(isChecked) {
                binding.checkedId = checkedId
            }

            // Chear purpose
            if(checkedId == R.id.buttonWebAddress){
                viewModel.purpose.postValue("")
            }
        }

        binding.toggleButton.check(if (viewModel.recipeBuilder.purpose.isNullOrBlank()) R.id.buttonWebAddress else R.id.buttonPurpose)

        binding.buttonOk.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

}