package org.dicekeys.app.fragments.bip39

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.api.bip39MnemonicCodeRecipeTemplate
import org.dicekeys.app.R
import org.dicekeys.app.adapters.Bip39Adapter
import org.dicekeys.app.adapters.KeyAdapter
import org.dicekeys.app.bip39.Mnemonics
import org.dicekeys.app.databinding.Bip39FragmentBinding
import org.dicekeys.app.fragments.dicekey.AbstractDiceKeyFragment
import org.dicekeys.app.viewmodels.Bip39ViewModel
import org.dicekeys.app.viewmodels.RecipeViewModel
import org.dicekeys.crypto.seeded.Secret
import javax.inject.Inject

@AndroidEntryPoint
class Bip39Fragment : AbstractDiceKeyFragment<Bip39FragmentBinding>(R.layout.bip39_fragment) {

    @Inject
    lateinit var viewModelFactory: Bip39ViewModel.AssistedFactory

    private val bip39ViewModel: Bip39ViewModel by viewModels {
        Bip39ViewModel.provideFactory(assistedFactory = viewModelFactory, diceKey = viewModel.diceKey.value!!)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(isGuarded) return

        binding.vm = viewModel


        val adapter = Bip39Adapter().also {
            it.set(Mnemonics.MnemonicCode(Secret.deriveFromSeed(diceKey.seed, bip39MnemonicCodeRecipeTemplate.recipeJson).secretBytes.copyOf(16)))
        }

        binding.recycler.also {
            it.adapter = adapter
            it.layoutManager = GridLayoutManager(requireContext(), 3)
        }

        binding.dicekey.setOnClickListener {
            viewModel.toggleHideFaces()
        }

        bip39ViewModel.mnemonic.observe(viewLifecycleOwner){
            adapter.set(it)
        }


        val spinnerAdapter = ArrayAdapter(requireContext(),
                R.layout.item_words_count, listOf("12","24"))

        binding.spinner.also {
            it.adapter = spinnerAdapter
            it.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    bip39ViewModel.setNumberOfWords(if(position == 0) 12 else 24)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

            }

//            it.onItemSelectedListener = object :
//                    AdapterView.OnItemSelectedListener {
//                override fun onItemSelected(parent: AdapterView<*>,
//                                            view: View, position: Int, id: Long) {
//                    Toast.makeText(this@MainActivity,
//                            getString(R.string.selected_item) + " " +
//                                    "" + languages[position], Toast.LENGTH_SHORT).show()
//                }
//
//                override fun onNothingSelected(parent: AdapterView<*>) {
//                    // write code to perform some action
//                }
//            }
        }

    }
}