package org.dicekeys.app.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.dicekeys.api.bip39MnemonicCodeRecipeTemplate
import org.dicekeys.app.bip39.Mnemonics
import org.dicekeys.crypto.seeded.Secret
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face


class Bip39ViewModel @AssistedInject constructor(
        @Assisted val diceKey: DiceKey<Face>
) : ViewModel() {

    val numberOfWords = MutableLiveData(12)
    val mnemonic = MutableLiveData<Mnemonics.MnemonicCode>();

    init {
        generateMnemonic()
    }

    private fun generateMnemonic(){
        val entropy = if(numberOfWords.value == 12) 16 else 32
        mnemonic.value = Mnemonics.MnemonicCode(Secret.deriveFromSeed(diceKey.seed, bip39MnemonicCodeRecipeTemplate.recipeJson).secretBytes.copyOf(entropy))
    }

    fun setNumberOfWords(number: Int) {
        numberOfWords.value = number
        generateMnemonic()
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(diceKey: DiceKey<Face>): Bip39ViewModel
    }

    companion object {
        fun provideFactory(
                assistedFactory: AssistedFactory,
                diceKey: DiceKey<Face>,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(diceKey = diceKey) as T
            }
        }
    }
}