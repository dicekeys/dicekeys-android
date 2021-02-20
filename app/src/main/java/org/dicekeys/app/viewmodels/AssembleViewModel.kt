package org.dicekeys.app.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.dicekeys.app.repositories.DiceKeyRepository
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face
import javax.inject.Inject


@HiltViewModel
class AssembleViewModel @Inject constructor(val diceKeyRepository: DiceKeyRepository) : ViewModel() {
    var page = MutableLiveData(0)

    var diceKey = MutableLiveData<DiceKey<Face>>()

    // var diceKeyScanned = MutableLiveData(false)
    var diceKeyBackedUp = MutableLiveData(false)

    fun setDiceKey(dk: DiceKey<Face>){
        diceKey.value = dk
        diceKeyRepository.set(dk)
    }

    fun setPage(position: Int) {
        page.value = position
    }

    fun nextPage() {
        page.value = page.value?.plus(1)
    }

    fun previousPage() {
        page.value = page.value?.minus(1)
    }
}