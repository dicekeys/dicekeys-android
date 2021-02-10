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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.dicekeys.fidowriter.ListOfWritableUsbFidoKeys
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.api.DerivationRecipe
import org.dicekeys.api.derivationRecipeTemplates
import org.dicekeys.app.AppFragment
import org.dicekeys.app.R
import org.dicekeys.app.databinding.SolokeyFragmentBinding
import org.dicekeys.app.viewmodels.DiceKeyViewModel
import org.dicekeys.crypto.seeded.Secret

@AndroidEntryPoint
class SoloKeyFragment: AppFragment<SolokeyFragmentBinding>(R.layout.solokey_fragment) {

    private lateinit var deviceList: ListOfWritableUsbFidoKeys
    private lateinit var permissionIntent: android.app.PendingIntent
    private lateinit var viewModel: DiceKeyViewModel
    private val INTENT_ACTION_USB_PERMISSION_EVENT = "org.dicekeys.intents.USB_PERMISSION_EVENT"
    private var isWriteUnderway: Boolean = false
    private var derivationRecipe = MutableLiveData<DerivationRecipe>()
    private var sequenceNumber = MutableLiveData<Int>(1)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getDiceKeyRootFragment().viewModel

        binding.vm = viewModel

        /*Get selected derivationRecipe template*/
        derivationRecipe.value = derivationRecipeTemplates.get(1)

        binding.tvRecipeJson.text = derivationRecipe.value!!.derivationOptionsJson
        binding.btnDown.setOnClickListener { sequencUpDown(false) }
        binding.btnUp.setOnClickListener { sequencUpDown(true) }
        permissionIntent = android.app.PendingIntent.getBroadcast(context, 0, Intent(INTENT_ACTION_USB_PERMISSION_EVENT), 0)
        deviceList = ListOfWritableUsbFidoKeys(
                context?.getSystemService(android.content.Context.USB_SERVICE) as UsbManager,
                permissionIntent
        )

        /*Observer sequence number changes & update derivationRecipe object*/
        sequenceNumber.observe(viewLifecycleOwner, Observer { intValue ->
            derivationRecipe.value = DerivationRecipe(derivationRecipeTemplates.get(1), intValue)
        })

        /*Observer derivation recipe change and update password, derivationOptionsJson */
        derivationRecipe.observe(viewLifecycleOwner, Observer { value ->
            val seed = Secret.deriveFromSeed(viewModel.diceKey.seed, value.derivationOptionsJson).secretBytes
            binding.tvSeedHexString.text = (seed.joinToString(separator = "") { String.format("%02x", (it.toInt() and 0xFF)) })
            binding.tvRecipeJson.text = value.derivationOptionsJson
        })

        /*Sequence number text change event*/
        binding.etSequenceNumber.doAfterTextChanged { edittext ->
            if (!edittext.isNullOrBlank() && edittext.toString().toInt() >= 1) {
                sequenceNumber.value = edittext.toString().toInt()
            } else {
                binding.etSequenceNumber.setText("" + 1)
                binding.etSequenceNumber.selectAll()
            }
        }
        /*Write soloKey*/
        binding.btnSidoKey.setOnClickListener {
            writeToCurrentSoloKey()
        }
    }
    /**
     * Up/down sequence number
     */
    private fun sequencUpDown(isUp: Boolean) {
        sequenceNumber.value?.let { a ->
            if (isUp) {
                binding.etSequenceNumber.setText((a + 1).toString())
            } else {
                if (a > 1) binding.etSequenceNumber.setText((a - 1).toString())
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
            derivationRecipe.value?.let { Secret.deriveFromSeed(viewModel.diceKey.seed, it.derivationOptionsJson).secretBytes }
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
            viewModel.soloKeyConnected()
            deviceList.devices.values.firstOrNull()?.let {
                binding.btnSidoKey.text = String.format(getString(R.string.seed_s_sn_s), it.productName, it.serialNumber)
            }
        }else{
            //No solo key connected
            viewModel.noSidoConnected()
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
        isWriteUnderway = true

        //Show Writing underway view
        viewModel.writingUnderWay()
        binding.tvSuccess.isVisible = false
        binding.tvError.isVisible = true
        timer.start()

        Thread(Runnable {
            try {
                synchronized(this) {
                    val connection = deviceList.connect(device)
                    connection.loadKeySeed(seed)
                }
                isWriteUnderway = false

                //Hide writing underway process and show connected Solo Key details with success
                viewModel.soloKeyConnected()
                requireActivity().runOnUiThread {
                    isWriteUnderway = false
                    binding.tvSuccess.isVisible = true
                    render()
                }


            } catch (e: java.lang.Exception) {

                //Solo Key wring failed
                requireActivity().runOnUiThread {
                    isWriteUnderway = false
                    binding.tvError.isVisible = true
                    binding.tvError.text = e.message
                    render()
                }
            }
        }).start()
    }

    /**
     * Intialize countdown timer
     */
    val timer = object : CountDownTimer(2000000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            //Update writing progress time
            binding.tvWritingProgress.text = String.format(getString(R.string.you_have_seconds_to_do_so), millisUntilFinished)
        }

        override fun onFinish() {

        }
    }
}