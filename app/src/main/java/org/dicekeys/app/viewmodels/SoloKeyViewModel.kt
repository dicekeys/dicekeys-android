package org.dicekeys.app.viewmodels

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.CountDownTimer
import android.view.View
import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.dicekeys.api.DerivationRecipe
import org.dicekeys.api.seedSecurityKeyRecipeTemplate
import org.dicekeys.app.Application
import org.dicekeys.app.ConsumableEvent
import org.dicekeys.app.fido.ConnectionToWritableUsbFidoKey
import org.dicekeys.crypto.seeded.Secret
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face


class SoloKeyViewModel @AssistedInject constructor(
        @Assisted application: Application,
        @Assisted val usbManager: UsbManager,
        @Assisted val diceKey: DiceKey<Face>,
) : AndroidViewModel(application) {

    val soloDevice = MutableLiveData<UsbDevice>(null)
    val hasPermissions = MutableLiveData(false)

    var derivationRecipe = MutableLiveData(seedSecurityKeyRecipeTemplate);
    var sequenceNumber = MutableLiveData(seedSecurityKeyRecipeTemplate.sequence.toString())
    val seed = MutableLiveData("")

    val writingProgress = MutableLiveData("")
    val productName = MutableLiveData("-")
    val serialNumber = MutableLiveData("-")

    val isWritingProcessUnderWay = MutableLiveData(false)
    val onError = MutableLiveData<ConsumableEvent<Throwable>>()
    val onSuccess = MutableLiveData<ConsumableEvent<Boolean>>()

    // Listener that updates state when USB devices are attached, detached,
    // or when the app is granted permission to access them
    // Intent has the USB device as an extra param, but not required for the current implementation
    // as we always scan for usb devices on any event, simpler for our use case.
    private var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            if (UsbManager.ACTION_USB_DEVICE_ATTACHED == intent.action) {
                scanUsbDevices()
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED == intent.action) {
                scanUsbDevices()
            } else if (ACTION_USB_PERMISSION == intent.action) {
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    checkUsbPermissions()
                }
            }
        }
    }

    init {
        // Listen for USB devices being attached, detached, or for the app being granted permission to access them.
        val intentFilter = IntentFilter().also {
            it.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            it.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
            it.addAction(ACTION_USB_PERMISSION)
        }

        // Registering the BroadcastReceiver, it's important to unregister in onCleared method
        getApplication<Application>().registerReceiver(broadcastReceiver, intentFilter)

        generateSeed()
        scanUsbDevices()
    }

    private fun generateSeed(){
        derivationRecipe.value?.let{ derivationRecipe ->
            seed.value = (Secret.deriveFromSeed(diceKey.seed, derivationRecipe.recipeJson).secretBytes.joinToString(separator = "") { String.format("%02x", (it.toInt() and 0xFF)) })
        }
    }

    /*
     * Warning: Only one USB device is supported, connecting more than one can result on unexpected behavior.
     * This is OK for now, as this is the most common scenario for Android
     */
    fun scanUsbDevices() {
        usbManager.deviceList.values.firstOrNull { device ->
            ConnectionToWritableUsbFidoKey.isSoloKey(device) || true
        }.also { usbDevice ->
            soloDevice.value = usbDevice
            isWritingProcessUnderWay.value = false
            checkUsbPermissions()
        }
    }

    fun checkUsbPermissions(){
        soloDevice.value?.let {
            val usbHasPermission = usbManager.hasPermission(it)
            hasPermissions.postValue(usbHasPermission)

            productName.postValue(if(usbHasPermission) it.productName else "")
            serialNumber.postValue(if(usbHasPermission) it.serialNumber else "")
        }
    }

    fun askForPermissions(v: View?){
        soloDevice.value?.let{
            val permissionIntent =
                    PendingIntent.getBroadcast(getApplication(), 0, Intent(ACTION_USB_PERMISSION), 0)
            usbManager.requestPermission(it, permissionIntent)
        }
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().unregisterReceiver(broadcastReceiver)
    }

    /**
     * Check seed value is valid or not
     */
    fun isSeedValid(s: ByteArray? = getSeedAsByteArray()): Boolean {
        return s != null && s.size == 32
    }

    /**
     * Get Seed HEXAString
     */
    fun getSeedAsByteArray(): ByteArray? {
        return try {
            derivationRecipe.value?.let { seed.value?.let { it1 -> Secret.deriveFromSeed(it1, it.recipeJson).secretBytes } }
        } catch (e: Throwable) {
            null
        }
    }

    /**
     * Intialize countdown timer
     */
    private val timer = object : CountDownTimer(FIDOKEY_WRITE_TIME, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            //Update writing progress time
            writingProgress.postValue((millisUntilFinished / 1000).toString())
        }

        override fun onFinish() {}
    }

    /**
     * Write seed to solokey
     */
    fun writeToSoloKey(v: View?) {
        soloDevice.value?.let { device ->
            if (isWritingProcessUnderWay.value == true)
                return

            val seed = getSeedAsByteArray()
            if (seed == null || !isSeedValid(seed))
                return

            isWritingProcessUnderWay.postValue(true)
            timer.start()

            viewModelScope.launch(Dispatchers.IO) {
                try {
                    ConnectionToWritableUsbFidoKey
                            .connect(usbManager, device)
                            .loadKeySeed(seed)

                    onSuccess.postValue(ConsumableEvent(true))
                } catch (e: Exception) {
                    onError.postValue(ConsumableEvent(e))
                }finally {
                    timer.cancel()
                    isWritingProcessUnderWay.postValue(false)
                }
            }
        }
    }

    /**
     * Up/down sequence number
     */
    fun sequencUpDown(isUp: Boolean) {
        try{
            sequenceNumber.value?.toInt()?.let { seq ->
                updateSequence(seq + if (isUp) 1 else -1)
            }
        }catch (e : Exception){
            e.printStackTrace()
        }
    }

    fun updateSequence(sequence: Int){
        if(sequence > 0) {
            derivationRecipe.value = DerivationRecipe(seedSecurityKeyRecipeTemplate, sequence)
            sequenceNumber.value = sequence.toString()

            generateSeed()
        }
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(application: Application, usbManager: UsbManager, diceKey: DiceKey<Face>): SoloKeyViewModel
    }

    companion object {
        const val FIDOKEY_WRITE_TIME = 8000L

        private const val ACTION_USB_PERMISSION = "org.dicekeys.USB_PERMISSION"

        fun provideFactory(
                assistedFactory: AssistedFactory,
                application: Application,
                usbManager: UsbManager,
                diceKey: DiceKey<Face>
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(application, usbManager, diceKey) as T
            }
        }
    }
}