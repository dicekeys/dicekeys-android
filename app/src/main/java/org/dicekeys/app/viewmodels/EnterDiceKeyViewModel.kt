package org.dicekeys.app.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face


class EnterDiceKeyViewModel : ViewModel() {
    val faces: ArrayList<Face> = ArrayList()
    var faceSelectedIndex: Int = 0

    val diceKey = MutableLiveData<DiceKey<Face>>()
    val isValid = MutableLiveData(false)
    var isLetterVisible = MutableLiveData(false)

    init {
        for (i in 0 until 25) {
            faces.add(Face(' ', ' ', 't'))
        }

        updateDiceKey()
    }

    fun updateDiceKey(){
        diceKey.value = DiceKey(faces = (0 until faces.size).map { index ->
            Face(faces[index].letter, faces[index].digit, faces[index].orientationAsLowercaseLetterTrbl)
        })

        val currentFace = faces[faceSelectedIndex]


        // Show Digits Keyboard, only if Letter exits and Digit not
        isLetterVisible.value = !(!currentFace.letter.isWhitespace() && currentFace.digit.isWhitespace())

        isValid.value = faceSelectedIndex == 24 && currentFace.isNotBlank
    }

    fun delete(){
        val currentFace = faces[faceSelectedIndex]

        val hasDigit = !currentFace.digit.isWhitespace()
        val hasLetter = !currentFace.letter.isWhitespace()

        // Remove Letter
        if(hasLetter && !hasDigit){
            faces[faceSelectedIndex] = Face(' ', ' ', ' ')
        }

        // Remove Digit
        if(hasLetter && hasDigit){
            faces[faceSelectedIndex] = Face(currentFace.letter, ' ', ' ')
        }

        // Select previous dice
        if(currentFace.isBlank && faceSelectedIndex > 0){
            faceSelectedIndex--
        }

        updateDiceKey()
    }

    fun add(input: Char){
        val currentFace = faces[faceSelectedIndex]

        if(currentFace.isNotBlank && faceSelectedIndex < 25){
            faceSelectedIndex++
            add(input)
            return
        }

        if(input.isDigit()){
            faces[faceSelectedIndex] = Face(currentFace.letter, input, currentFace.orientationAsLowercaseLetterTrbl)
        }else{
            faces[faceSelectedIndex] = Face(input, currentFace.digit, currentFace.orientationAsLowercaseLetterTrbl)
        }

        updateDiceKey()
    }

    fun rotate(isRight: Boolean) {
        val oldFace = faces[faceSelectedIndex]

        val orientation: Char = if(isRight){
            when (oldFace.orientationAsLowercaseLetterTrbl) {
                't' -> 'r'
                'r' -> 'b'
                'b' -> 'l'
                else -> 't'
            }
        }else{
            when (oldFace.orientationAsLowercaseLetterTrbl) {
                't' -> 'l'
                'l' -> 'b'
                'b' -> 'r'
                else -> 't'
            }
        }

        faces[faceSelectedIndex] = Face(oldFace.letter, oldFace.digit, orientation)

        updateDiceKey()
    }

}