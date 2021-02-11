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

@AndroidEntryPoint
class SoloKeyFragment : AppFragment<SolokeyFragmentBinding>(R.layout.solokey_fragment) {

    private lateinit var deviceList: ListOfWritableUsbFidoKeys
    private lateinit var permissionIntent: android.app.PendingIntent
    private lateinit var viewModel: DiceKeyViewModel
    private var isWriteUnderway: Boolean = false
    val soloKeyViewModel: SoloKeyViewModel by viewModels()

    companion object {
        const val INTENT_ACTION_USB_PERMISSION_EVENT = "org.dicekeys.intents.USB_PERMISSION_EVENT"
        const val SIDOKEYWRITETIME = 8000L
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getDiceKeyRootFragment().viewModel

        binding.vm = viewModel
        binding.vmSodo = soloKeyViewModel

        /*Get selected derivationRecipe template*/
        soloKeyViewModel.derivationRecipe.value = derivationRecipeTemplates.get(1)


        binding.btnDown.setOnClickListener { sequenceUpDown(false) }
        binding.btnUp.setOnClickListener { sequenceUpDown(true) }
        permissionIntent = android.app.PendingIntent.getBroadcast(context, 0, Intent(INTENT_ACTION_USB_PERMISSION_EVENT), 0)
        deviceList = ListOfWritableUsbFidoKeys(
                context?.getSystemService(android.content.Context.USB_SERVICE) as UsbManager,
                permissionIntent
        )

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
            if (!edittext.isNullOrBlank() && edittext.toString().toInt() >= 1) {
                soloKeyViewModel.sequenceNumber.value = edittext.toString().toInt()
            } else {
                soloKeyViewModel.sequenceNumber.value = 1
            }
        }

        /*Write soloKey*/
        binding.btnSidoKey.setOnClickListener {
            writeToCurrentSoloKey()
        }
        /*Observe Solo key writing process*/
        soloKeyViewModel.isWritingProcessUnderWay.observe(viewLifecycleOwner, { values ->
            isWriteUnderway = values
        })
    }

    /**
     * Up/down sequence number
     */
    private fun sequenceUpDown(isUp: Boolean) {
        soloKeyViewModel.sequenceNumber.value?.let { a ->
            if (isUp) {
                soloKeyViewModel.sequenceNumber.value = (a + 1)
            } else {
                if (a > 1) soloKeyViewModel.sequenceNumber.value = (a - 1)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val usbIntentFilter = IntentFilter()
        usbIntentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        usbIntentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        usbIntentFilter.addAction(INTENT_ACTION_USB_PERMISSION_EVENT)
        requireActivity().registerReceiver(usbReceiver, usbIntentFilter)
        render()
    }

    override fun onPause() {
        super.onPause()
        requireActivity().unregisterReceiver(usbReceiver)
    }

    /**
     * Get Broadcast of USB attach or detache
     */
    private val usbReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context, intent: Intent) {
            render()
        }
    }

    /**
     * Check seed value is valid or not
     */
    fun isSeedValid(s: ByteArray? = getSeed()): Boolean {
        return s != null && s.size == 32
    }

    /**
     * Check USB Device is ready to write or nor
     */
    fun deviceReadyToWrite(): Boolean =
            deviceList.devices.values.firstOrNull()?.let {
                deviceList.hasPermission(it)
            } ?: false

    /**
     * Get Seed HEXAString
     */
    fun getSeed(): ByteArray? {
        return try {
            soloKeyViewModel.derivationRecipe.value?.let { Secret.deriveFromSeed(viewModel.diceKey.seed, it.derivationOptionsJson).secretBytes }
        } catch (e: Throwable) {
            null
        }
    }

    /**
     * Render attached usb devices and show ready to write view if device ready show view to write key
     */
    fun render() {
        if (isSeedValid() &&
                deviceReadyToWrite() &&
                !isWriteUnderway) {
            soloKeyViewModel.soloKeyConnected()
            deviceList.devices.values.firstOrNull()?.let {
                binding.btnSidoKey.text = String.format(getString(R.string.seed_s_sn_s), it.productName, it.serialNumber)
            }
        } else {
            //No solo key connected
            soloKeyViewModel.noSidoConnected()
        }
        timer.cancel()
    }

    /**
     * Check attached usb and write seed to solo key
     */
    fun writeToCurrentSoloKey() {
        if (deviceList.devices.size == 1) deviceList.devices.values.firstOrNull()?.let {
            writeToSoloKey(it)
        }
    }

    /**
     * Write seed to solokey
     */
    private fun writeToSoloKey(device: UsbDevice) {
        if (isWriteUnderway)
            return
        val seed = getSeed()
        if (seed == null || !isSeedValid(seed))
            return
        //Show Writing underway view
        soloKeyViewModel.writingUnderWay()
        binding.tvSuccess.isVisible = false
        binding.tvError.isVisible = false
        timer.start()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val connection = deviceList.connect(device)
                connection.loadKeySeed(seed)
                //Hide writing underway process and show connected Solo Key details with success
                soloKeyViewModel.soloKeyConnected()
                binding.tvSuccess.text = String.format(getString(R.string.successfully_wrote_to_key), device.deviceName)
                binding.tvSuccess.isVisible = true
                render()
            } catch (e: java.lang.Exception) {
                //Solo Key wring failed
                binding.tvError.isVisible = true
                binding.tvError.text = e.message
                render()
            }
        }
    }

    /**
     * Intialize countdown timer
     */
    val timer = object : CountDownTimer(SIDOKEYWRITETIME, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            //Update writing progress time
            binding.tvWritingProgress.text = String.format(getString(R.string.you_have_seconds_to_do_so), millisUntilFinished / 1000)
        }

        override fun onFinish() {}
    }
}