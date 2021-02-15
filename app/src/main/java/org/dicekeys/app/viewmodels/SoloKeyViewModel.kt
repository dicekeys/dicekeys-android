package org.dicekeys.app.viewmodels

import android.hardware.usb.UsbDevice
import android.os.CountDownTimer
import android.text.Editable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicekeys.fidowriter.ListOfWritableUsbFidoKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.dicekeys.api.DerivationRecipe
import org.dicekeys.app.fragments.dicekey.SoloKeyFragment
import org.dicekeys.crypto.seeded.Secret


class SoloKeyViewModel : ViewModel() {

    lateinit var deviceList: ListOfWritableUsbFidoKeys
    val isSoloKeyConnected = MutableLiveData(true)
    val isWritingProcessUnderWay = MutableLiveData(false)
    val isSoloKeyNotConnected = MutableLiveData(false)
    var derivationRecipe = MutableLiveData<DerivationRecipe>();
    var sequenceNumber = MutableLiveData<Int>(1)
    val seedHaXString= MutableLiveData<String>("")
    val isSuccess = MutableLiveData(false)
    val isFail = MutableLiveData(false)
    var successMessage = MutableLiveData<String>("");
    var failMessage =  MutableLiveData<String>("");
    var writingProgress =  MutableLiveData<String>("");
    /*Connected USB Details*/
    var productName =  MutableLiveData<String>("");
    var serialNumber =  MutableLiveData<String>("");
   /************************/
    var seedString =  MutableLiveData<String>("");
    companion object {
        const val SIDOKEYWRITETIME = 8000L
    }

    /**
     * Show no solo key connect view and hide others views
     */
    fun noSidoConnected(){
        isWritingProcessUnderWay.postValue(false)
        isSoloKeyConnected.postValue(false)
        isSoloKeyNotConnected.postValue(true);
    }

    /**
     * show writing underway view and hide other views
     */
    fun writingUnderWay(){
        isSoloKeyNotConnected.postValue(false);
        isSoloKeyConnected.postValue(false)
        isWritingProcessUnderWay.postValue(true)
    }

    /**
     * Show solo key connected view and hide other views
     */
    fun soloKeyConnected(){
        isSoloKeyNotConnected.postValue(false);
        isWritingProcessUnderWay.postValue(false)
        isSoloKeyConnected.postValue(true)
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
            derivationRecipe.value?.let { seedString.value?.let { it1 -> Secret.deriveFromSeed(it1, it.derivationOptionsJson).secretBytes } }
        } catch (e: Throwable) {
            null
        }
    }

    /**
     * Intialize countdown timer
     */
    val timer = object : CountDownTimer(SIDOKEYWRITETIME, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            //Update writing progress time
            writingProgress.postValue((millisUntilFinished / 1000).toString())
        }

        override fun onFinish() {}
    }

    /**
     * Write seed to solokey
     */
    private fun writeToSoloKey(device: UsbDevice) {
        if (isWritingProcessUnderWay.value == true)
            return
        val seed = getSeed()
        if (seed == null || !isSeedValid(seed))
            return
        //Show Writing underway view
        writingUnderWay()
        isSuccess.postValue(false)
        isFail.postValue(false)
        timer.start()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val connection = deviceList.connect(device)
                connection.loadKeySeed(seed)
                //Hide writing underway process and show connected Solo Key details with success
                soloKeyConnected()
                successMessage.postValue(device.deviceName)/*device.deviceName*/
                isSuccess.postValue(true)
                render()
            } catch (e: java.lang.Exception) {
                //Solo Key wring failed
                isFail.postValue(true)
                failMessage.postValue(e.message.toString())
                render()
            }
        }
    }


    /**
     * Render attached usb devices and show ready to write view if device ready show view to write key
     */
    fun render() {
        if (isSeedValid() &&
                deviceReadyToWrite() &&
                !(isWritingProcessUnderWay.value)!!) {
            soloKeyConnected()
            deviceList.devices.values.firstOrNull()?.let {
                productName.postValue(it.productName.toString())
                serialNumber.postValue(it.serialNumber.toString())

            }
        } else {
            //No solo key connected
            noSidoConnected()
        }
       // soloKeyConnected()
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
     * Up/down sequence number
     */
    fun sequenceUpDown(isUp: Boolean) {
        sequenceNumber.value?.let { a ->
            if (isUp) {
                sequenceNumber.value = (a + 1)
            } else {
                if (a > 1) sequenceNumber.value = (a - 1)
            }
        }
    }

    /*
    * Edittext Squence Number change
    * */
    fun doafterchange(editable: Editable){
        if (!editable.isNullOrBlank() && editable.toString().toInt() >= 1) {
            sequenceNumber.value = editable.toString().toInt()
        } else {
            sequenceNumber.value = 1
        }
    }
}