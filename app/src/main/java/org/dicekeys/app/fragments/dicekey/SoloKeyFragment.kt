package org.dicekeys.app.fragments.dicekey

import android.content.Context
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.Application
import org.dicekeys.app.R
import org.dicekeys.app.databinding.SolokeyFragmentBinding
import org.dicekeys.app.extensions.dialog
import org.dicekeys.app.extensions.errorDialog
import org.dicekeys.app.viewmodels.SoloKeyViewModel
import javax.inject.Inject

@AndroidEntryPoint
class SoloKeyFragment : AbstractDiceKeyFragment<SolokeyFragmentBinding>(R.layout.solokey_fragment) {

    @Inject
    lateinit var viewModelFactory: SoloKeyViewModel.AssistedFactory

    private val soloKeyViewModel: SoloKeyViewModel by viewModels {
        SoloKeyViewModel.provideFactory(viewModelFactory, requireActivity().application as Application, requireContext().getSystemService(Context.USB_SERVICE) as UsbManager, viewModel.diceKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.diceKeyVM = viewModel
        binding.vm = soloKeyViewModel

        binding.btnDown.setOnClickListener { soloKeyViewModel.sequencUpDown(false) }
        binding.btnUp.setOnClickListener { soloKeyViewModel.sequencUpDown(true) }

        // Update the sequence number in the model when the text field's recipe changes to a valid input
        binding.etSequenceNumber.doAfterTextChanged { edittext ->
            try{
                soloKeyViewModel.updateSequence(edittext.toString().toInt())
            }catch (e: Exception){
                e.printStackTrace()
            }
        }

        // Show Error
        soloKeyViewModel.onError.observe(viewLifecycleOwner) {
            it.getContentIfNotHandledOrReturnNull()?.let { err ->
                errorDialog(err)
            }
        }

        // Show success message
        soloKeyViewModel.onSuccess.observe(viewLifecycleOwner) {
            it.getContentIfNotHandledOrReturnNull()?.let {
                dialog(R.string.solokey, R.string.successfully_wrote_to_solokey)
            }
        }
    }
}