package org.dicekeys.trustedapp.state

import android.util.Log
import androidx.databinding.BaseObservable
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face
import org.dicekeys.dicekey.FaceRead
import org.dicekeys.dicekey.SimpleDiceKey
import kotlin.math.max
import kotlin.math.min

object EditableDiceKeyState :BaseObservable(){
    val faces: ArrayList<Face> = ArrayList()
    var faceSelectedIndex: Int = 0
    init {
        for (i in 0..24) {
            faces.add(Face(null,null,'t'))
        }
    }
    var letter: Char? = null
        get ()=field
        set (value){field=value}

    var digit: Char? = null
        get ()=field
        set (value){field=value}

    var orientation: Char ='t'
        get ()=field
        set (value){field=value}

    fun moveNext() {
        faceSelectedIndex = min(24, faceSelectedIndex + 1)
    }

    fun movePrev() {
        faceSelectedIndex = max(0, faceSelectedIndex - 1)
        var selectedface=faces.get(faceSelectedIndex)

        this.letter=selectedface.letter
        this.digit=selectedface.digit
        this.orientation=selectedface.orientationAsLowercaseLetterTrbl
    }

    fun rotateLeft() {
        when (this.orientation) {
            't' -> this.orientation='l'
            'l' -> this.orientation='b'
            'b' -> this.orientation='r'
            'r' -> this.orientation='t'
        }
        rotateFace()
    }

    fun rotateRight() {
        when (this.orientation) {
            't' -> this.orientation='r'
            'r' -> this.orientation='b'
            'b' -> this.orientation='l'
            'l' -> this.orientation='t'
        }
        rotateFace()
    }
    fun enterLetter(letter: Char) {

        if (this.letter != null && this.digit != null && faceSelectedIndex < 24) {
            //Move to next if previous face is inserted
            moveNext()
            delete()
        }
        this.letter = letter
        addFace()


    }

    fun enterDigit(digit: Char) {
        this.digit = digit
        removeFace()
        addFace()
    }

    /**
     * Add face to list
     */
    fun addFace(){
        faces.add(faceSelectedIndex, Face(this.letter,this.digit,this.orientation))
    }

    /**
     * Remove Face from list
     */
    fun removeFace(){
        faces.removeAt(faceSelectedIndex)
    }

    /**
     * Rotate Face from Left to right & right to left
     */
    fun rotateFace(){
        removeFace()
        faces.add(faceSelectedIndex, Face(this.letter,this.digit,this.orientation))
    }

    /**
     * Reset
     */
    fun delete() {
        this.letter = null
        this.digit = null
        this.orientation = 't'
    }

    /**
     * Delete Face
     */
    fun backspace() {
        delete()
        removeFace()
        faces.add(faceSelectedIndex,Face(null,null,'t'))
        movePrev()
    }
}
