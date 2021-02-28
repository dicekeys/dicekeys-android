
package org.dicekeys.trustedapp.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import org.dicekeys.dicekey.*
import org.dicekeys.trustedapp.adapter.KeyAdapter
import org.dicekeys.trustedapp.databinding.ActivityDicekeyKeyboardBinding
import org.dicekeys.trustedapp.state.EditableDiceKeyState

class DicekeyKeyboard : AppCompatActivity(), KeyAdapter.OnButtonClickListener {
    private lateinit var binding: ActivityDicekeyKeyboardBinding;

    var letterVisible= MutableLiveData<Boolean>(true);

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDicekeyKeyboardBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        letterVisible.observe(this , Observer {
            binding.isLetterVisible=it

        })

        updateFaceView()

        /*Create Adapter for Letters and digits*/
        val adapter_letter = KeyAdapter(this)
        val adapter_digit = KeyAdapter(this)
        adapter_letter.set(FaceLetters)
        adapter_digit.set(FaceDigits)

        /*Initialize letter recyclerview*/
        binding.rcvLetter.also {
            it.layoutManager = GridLayoutManager(this,8)
            it.adapter = adapter_letter
        }
        /*Initialize digit recyclerview*/
        binding.rcvDigit.also {
            it.layoutManager = GridLayoutManager(this,8)
            it.adapter = adapter_digit
        }
        /*Delete decikey face*/
        binding.btnDelete.setOnClickListener {
            EditableDiceKeyState.backspace()
            letterVisible.value=true
            updateFaceView()
        }
        /*Rotate left dicekey face*/
        binding.btnrtLeft.setOnClickListener {
            EditableDiceKeyState.rotateLeft()
            updateFaceView()
        }
        /*Rotate right dicekey face*/
        binding.btnrtRight.setOnClickListener {
            EditableDiceKeyState.rotateRight()
            updateFaceView()
        }

    }

    /**
     * Update dicekeyview
     */
    fun updateFaceView(){
        binding.stickerSheetView.diceKey =DiceKey(faces = (0 until 25).map { index ->
            Face( EditableDiceKeyState.faces.get(index).letter, EditableDiceKeyState.faces.get(index).digit,EditableDiceKeyState.faces.get(index).orientationAsLowercaseLetterTrbl)
        })
        binding.stickerSheetView.highlightedIndexes= setOf(EditableDiceKeyState.faceSelectedIndex)
    }

    /**
     * On Letter or Digit click listener
     */
    override fun onClick(view: View, char: Char) {
        /*Show/hide digits or later keyboard */
        if(letterVisible.value == true){
            letterVisible.value=false
            EditableDiceKeyState.enterLetter(char)
        }else{
            letterVisible.value=true
            EditableDiceKeyState.enterDigit(char)
        }
        updateFaceView()
    }


}




