package org.dicekeys.app.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face
import javax.inject.Inject

@HiltViewModel
class ApiRequestViewModel @Inject constructor() : ViewModel() {

    var diceKey = MutableLiveData<DiceKey<Face>>()

    var title = MutableLiveData("")
    var approve = MutableLiveData("")

    var recipe = MutableLiveData("")

    var createLabel = MutableLiveData("")
    var dataCreated = MutableLiveData("")


    override fun onCleared() {
        super.onCleared()
    }
}