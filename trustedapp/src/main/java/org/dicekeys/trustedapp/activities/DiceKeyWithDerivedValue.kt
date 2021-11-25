package org.dicekeys.trustedapp.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import org.dicekeys.api.DerivationRecipe
import org.dicekeys.api.derivationRecipeTemplates
import org.dicekeys.crypto.seeded.Password
import org.dicekeys.read.DiceKeyDrawable
import org.dicekeys.trustedapp.R
import org.dicekeys.trustedapp.databinding.ActivityDiceKeyWithDerivedValueBinding
import org.dicekeys.trustedapp.state.DiceKeyState


class DiceKeyWithDerivedValue : AppCompatActivity() {
    private lateinit var binding: ActivityDiceKeyWithDerivedValueBinding;
    private  var derivationRecipe = MutableLiveData<DerivationRecipe>();
    private var sequenceNumber=MutableLiveData<Int>(1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiceKeyWithDerivedValueBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        renderdicekeyview()

        /*Get selected derivationRecipe template*/
        derivationRecipe.value=derivationRecipeTemplates.get(intent.getIntExtra("derivationRecipeTemplateIndex",0))

        binding.tvRecipeFor.text= String.format(getString(R.string.recipe_for_password, this.derivationRecipe.value!!.name))
        binding.tvRecipeJson.text= derivationRecipe.value!!.recipeJson
        binding.tvPassword.text= DiceKeyState.diceKey?.toCanonicalRotation()?.let { Password.deriveFromSeed(it.toHumanReadableForm(), derivationRecipe.value!!.recipeJson,"").password }
        binding.btnDown.setOnClickListener {sequencUpDown(false)}
        binding.btnUp.setOnClickListener {sequencUpDown(true)}
        binding.btnCopyPassword.setOnClickListener{copyPassword()}

        /*Observer sequence number changes & update derivationRecipe object*/
        sequenceNumber.observe(this, Observer { intValue ->
            derivationRecipe.value = DerivationRecipe.createRecipeFromTemplate(derivationRecipeTemplates[intent.getIntExtra("derivationRecipeTemplateIndex", 0)], intValue)
        })

        /*Observer derivation recipe change and update password, recipeJson */
        derivationRecipe.observe(this, Observer {value ->
            binding.tvPassword.text=DiceKeyState.diceKey?.toCanonicalRotation()?.let { Password.deriveFromSeed(it.toHumanReadableForm(),value.recipeJson).password }
            binding.tvRecipeJson.text=value.recipeJson
        })

        /*Sequence number text change event*/
        binding.etSequenceNumber.doAfterTextChanged { edittext ->
            if(!edittext.isNullOrBlank() && edittext.toString().toInt()>=1) {
                sequenceNumber.value=edittext.toString().toInt()
            }else {
                binding.etSequenceNumber.setText(""+1)
                binding.etSequenceNumber.selectAll()
            }
        }



    }

    /**
     * Copy password to clipboard
     */
    private fun copyPassword(){
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", binding.tvPassword.text)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(this, "Password copied...", Toast.LENGTH_LONG).show()
    }

    /**
     * Up/down sequence number
     */
    private fun sequencUpDown(isUp: Boolean ){
        sequenceNumber.value?.let { a ->
            if(isUp){ binding.etSequenceNumber.setText( (a + 1).toString())}
            else{if(a>1)binding.etSequenceNumber.setText( (a - 1).toString())}
        }
    }

    /**
     * Render dicekey to imageview
     */
    private fun renderdicekeyview(){
        try {
            val diceKey = DiceKeyState.diceKey
            if (diceKey == null) {
                binding.diceKeyView.setImageDrawable(null)
                binding.diceKeyView.contentDescription = ""
            } else {
                val humanReadableForm: String = diceKey.toCanonicalRotation().toHumanReadableForm()
                val myDrawing = DiceKeyDrawable(this, diceKey)
                myDrawing.setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY );
                binding.diceKeyView.setImageDrawable(myDrawing)
                binding.diceKeyView.contentDescription = humanReadableForm

            }
        } catch (e: Exception) {
            val sw = java.io.StringWriter()
            val pw = java.io.PrintWriter(sw)
            e.printStackTrace(pw)
            val stackTrace: String = sw.toString()
            android.util.Log.e("We caught exception", stackTrace)
        }
    }
}