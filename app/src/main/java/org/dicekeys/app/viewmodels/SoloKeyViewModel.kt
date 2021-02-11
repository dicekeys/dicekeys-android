package org.dicekeys.app.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.dicekeys.api.DerivationRecipe


class SoloKeyViewModel : ViewModel() {

    val isSoloKeyConnected = MutableLiveData(true)
    val isWritingProcessUnderWay = MutableLiveData(false)
    val isSoloKeyNotConnected = MutableLiveData(false)
    var derivationRecipe = MutableLiveData<DerivationRecipe>();
    var sequenceNumber = MutableLiveData<Int>(1)
    val seedHaXString= MutableLiveData<String>("")

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


}