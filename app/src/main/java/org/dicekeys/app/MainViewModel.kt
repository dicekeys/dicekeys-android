package org.dicekeys.app

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.dicekeys.app.repositories.DiceKeyRepository
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(diceKeyRepository: DiceKeyRepository): ViewModel() {

}