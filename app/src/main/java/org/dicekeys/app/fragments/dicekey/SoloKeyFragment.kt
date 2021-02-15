package org.dicekeys.app.fragments.dicekey

import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.dicekeys.fidowriter.ListOfWritableUsbFidoKeys
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.dicekeys.api.DerivationRecipe
import org.dicekeys.api.derivationRecipeTemplates
import org.dicekeys.app.AppFragment
import org.dicekeys.app.R
import org.dicekeys.app.databinding.SolokeyFragmentBinding
import org.dicekeys.app.viewmodels.DiceKeyViewModel
import org.dicekeys.app.viewmodels.SoloKeyViewModel
import org.dicekeys.crypto.seeded.Secret
import org.dicekeys.dicekey.DiceKey

@AndroidEntryPoint
class SoloKeyFragment : AppFragment<SolokeyFragmentBinding>(R.layout.solokey_fragment) {

    private lateinit var permissionIntent: android.app.PendingIntent
    private lateinit var viewModel: DiceKeyViewModel
    lateinit var diceKey: DiceKey<*>
    val soloKeyViewModel: SoloKeyViewModel by viewModels()
    companion object {
        const val INTENT_ACTION_USB_PERMISSION_EVENT = "org.dicekeys.intents.USB_PERMISSION_EVENT"
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getDiceKeyRootFragment().viewModel

        binding.vm = viewModel
        diceKey=viewModel.diceKey
        binding.vmSolo = soloKeyViewModel
        soloKeyViewModel.seedString.postValue(viewModel.diceKey.seed)

        /*Get selected derivationRecipe template*/
        soloKeyViewModel.derivationRecipe.value = derivationRecipeTemplates.get(1)

        permissionIntent = android.app.PendingIntent.getBroadcast(context, 0, Intent(INTENT_ACTION_USB_PERMISSION_EVENT), 0)

        //Get List of connected
        getConnectedDeviceList()
        /*Observer sequence number changes & update derivationRecipe object*/
        soloKeyViewModel.sequenceNumber.observe(viewLifecycleOwner, Observer { intValue ->
            soloKeyViewModel.derivationRecipe.value = DerivationRecipe(derivationRecipeTemplates.get(1), intValue)
        })

        /*Observer derivation recipe change and update password, derivationOptionsJson */
        soloKeyViewModel.derivationRecipe.observe(viewLifecycleOwner, Observer { value ->
            soloKeyViewModel.seedHaXString.value = (Secret.deriveFromSeed(viewModel.diceKey.seed, value.derivationOptionsJson).secretBytes.joinToString(separator = "") { String.format("%02x", (it.toInt() and 0xFF)) })
        })

        /*Sequence number text change event*/
        binding.etSequenceNumber.doAfterTextChanged { edittext ->
            edittext?.let { soloKeyViewModel.doafterchange(it) }
        }

    }



    override fun onResume() {
        super.onResume()
        val usbIntentFilter = IntentFilter()
        usbIntentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        usbIntentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        usbIntentFilter.addAction(INTENT_ACTION_USB_PERMISSION_EVENT)
        requireActivity().registerReceiver(usbReceiver, usbIntentFilter)
       soloKeyViewModel.render()
    }

    override fun onPause() {
        super.onPause()
        requireActivity().unregisterReceiver(usbReceiver)
    }

    /**
     * Get Broadcast of USB attach or detached
     */
    private val usbReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context, intent: Intent) {
            getConnectedDeviceList();
            soloKeyViewModel.render()
        }
    }

    /**
     * Get Connected device list
     */
    private fun getConnectedDeviceList(){
        binding.vmSolo?.deviceList = ListOfWritableUsbFidoKeys(
                context?.getSystemService(android.content.Context.USB_SERVICE) as UsbManager,
                permissionIntent
        )
    }




}