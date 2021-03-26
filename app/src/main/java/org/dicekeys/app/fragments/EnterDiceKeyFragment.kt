package org.dicekeys.app.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.distinctUntilChanged
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.AppFragment
import org.dicekeys.app.R
import org.dicekeys.app.adapters.KeyAdapter
import org.dicekeys.app.databinding.FragmentEnterDicekeyBinding
import org.dicekeys.app.repositories.DiceKeyRepository
import org.dicekeys.app.viewmodels.EnterDiceKeyViewModel
import org.dicekeys.dicekey.FaceDigits
import org.dicekeys.dicekey.FaceLetters
import javax.inject.Inject

@AndroidEntryPoint
class EnterDiceKeyFragment : AppFragment<FragmentEnterDicekeyBinding>(R.layout.fragment_enter_dicekey, 0), KeyAdapter.OnButtonClickListener {

    @Inject
    lateinit var diceKeyRepository: DiceKeyRepository

    val viewModel: EnterDiceKeyViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = viewModel

        val keysAdapter = KeyAdapter(this)

        binding.recycler.also {
            it.adapter = keysAdapter
            it.layoutManager = GridLayoutManager(requireContext(), 8)
        }

        binding.btnrtLeft.setOnClickListener {
            viewModel.rotate(false)
        }

        binding.btnrtRight.setOnClickListener {
            viewModel.rotate(true)
        }

        binding.btnDelete.setOnClickListener {
            viewModel.delete()
        }

        binding.buttonContinue.setOnClickListener {

            viewModel.diceKey.value?.let {
                diceKeyRepository.set(it)

                // Go To MainDiceKey view and remove Assemble from the backstack
                val navOptionsBuilder = NavOptions.Builder().setPopUpTo(R.id.listDiceKeysFragment, false)
                findNavController().navigate(EnterDiceKeyFragmentDirections.actionGlobalDicekey(), navOptionsBuilder.build())
            }
        }

        viewModel.isLetterVisible.distinctUntilChanged().observe(viewLifecycleOwner) { showLetters ->
            keysAdapter.set(if (showLetters) FaceLetters else FaceDigits)
        }

        viewModel.diceKey.observe(viewLifecycleOwner) {
            binding.dicekey.highlightedIndexes = setOf(viewModel.faceSelectedIndex)
        }
    }

    override fun onClick(view: View, char: Char) {
        viewModel.add(char)
    }
}