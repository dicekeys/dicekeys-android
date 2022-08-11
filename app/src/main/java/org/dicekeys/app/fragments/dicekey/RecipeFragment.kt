package org.dicekeys.app.fragments.dicekey

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.R
import org.dicekeys.app.adapters.GenericAdapter
import org.dicekeys.app.data.DerivedValueView
import org.dicekeys.app.databinding.RecipeFragmentBinding
import org.dicekeys.app.extensions.askToCopyToClipboard
import org.dicekeys.app.extensions.dialogQR
import org.dicekeys.app.items.Bip39WordItem
import org.dicekeys.app.items.GenericListItem
import org.dicekeys.app.repositories.RecipeRepository
import org.dicekeys.app.utils.copyToClipboard
import org.dicekeys.app.viewmodels.RecipeViewModel
import org.dicekeys.crypto.seeded.DerivationOptions
import javax.inject.Inject

@AndroidEntryPoint
class RecipeFragment : AbstractDiceKeyFragment<RecipeFragmentBinding>(R.layout.recipe_fragment), GenericAdapter.OnItemClickListener{

    private lateinit var bip39Adapter: GenericAdapter
    private val args: RecipeFragmentArgs by navArgs()

    @Inject
    lateinit var recipeRepository: RecipeRepository

    @Inject
    lateinit var viewModelFactory: RecipeViewModel.AssistedFactory

    private val recipeViewModel: RecipeViewModel by viewModels {
        RecipeViewModel.provideFactory(assistedFactory = viewModelFactory, diceKey = viewModel.diceKey.value!!, recipe = args.recipe, deriveType = args.deriveType, isEditable = args.editable)
    }

    override fun onViewCreatedGuarded(view: View, savedInstanceState: Bundle?) {
        binding.diceKeyVM = viewModel
        binding.vm = recipeViewModel

        binding.btnDown.setOnClickListener { recipeViewModel.sequenceUpDown(false) }
        binding.btnUp.setOnClickListener { recipeViewModel.sequenceUpDown(true) }


        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, mutableListOf<String>())
        binding.type.setAdapter(typeAdapter)

        binding.type.setOnItemClickListener { _, _, position, _ ->
            recipeViewModel.derivedValue.value?.views?.get(position)?.let{
                recipeViewModel.setView(it)
            }
        }

        recipeViewModel.derivedValue.observe(viewLifecycleOwner){ derivedValue ->
            typeAdapter.clear()

            derivedValue?.views?.map { it.description }?.also { list ->
                typeAdapter.addAll(list)

                val currentSelection = binding.type.text.toString()
                if(currentSelection.isBlank() || list.find { it == currentSelection } == null){

                    // If recipe has a specific purpose select the appropriate view
                    when {
                        recipeViewModel.derivationRecipe.value?.let { it.purpose == "pgp" && it.type == DerivationOptions.Type.SigningKey } == true -> {
                            DerivedValueView.OpenPGPPrivateKey()
                        }
                        recipeViewModel.derivationRecipe.value?.let { it.purpose == "ssh" && it.type == DerivationOptions.Type.SigningKey } == true -> {
                            DerivedValueView.OpenSSHPrivateKey()
                        }
                        else -> {
                            derivedValue.views[0]
                        }
                    }.also { derivedValueView ->
                        binding.type.setText(derivedValueView.description, false)
                        recipeViewModel.setView(derivedValueView)
                    }
                }
            }
        }

        binding.derivedValue.setOnClickListener {
            copyDerivedValue()
        }

        binding.buttonQrCode.setOnClickListener {
            recipeViewModel.derivedValueAsString.value?.let {
                dialogQR(title = recipeViewModel.derivedValueView.value!!.description, content = it)
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

        bip39Adapter = GenericAdapter(this)

        binding.recycler.also {
            it.layoutManager = StaggeredGridLayoutManager(3 , StaggeredGridLayoutManager.VERTICAL)
            it.adapter = bip39Adapter
        }

        binding.buttonEditRecipe.setOnClickListener {
            EditRecipeBottomSheet().also {
                it.show(childFragmentManager, it.toString())
            }
        }

        recipeViewModel.derivedValueAsString.observe(viewLifecycleOwner) {
            if(recipeViewModel.derivedValueView.value is DerivedValueView.BIP39){
                updateBip39Words(it)
            }
        }

        if(recipeViewModel.derivationRecipe.value == null){
            EditRecipeBottomSheet().also {
                it.show(childFragmentManager, it.toString())
            }
        }
    }

    private fun copyDerivedValue(){
        recipeViewModel.derivedValueAsString.value?.let {
            askToCopyToClipboard("Do you want to copy the derived value to the clipboard?", it, binding.cardDerivedValue)
        }
    }

    private fun updateBip39Words(value: String) {
        val list = mutableListOf<GenericListItem<*>>()

        for((index, word) in value.split(" ").withIndex()){
            list += Bip39WordItem((index + 1).toString(),word)
        }

        bip39Adapter.set(list)
    }

    override fun onItemClicked(view: View, position: Int, item: GenericListItem<*>) {
        copyDerivedValue()
    }
}