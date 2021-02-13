package org.dicekeys.trustedapp.activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.preference.PreferenceManager
import org.dicekeys.api.derivationRecipeTemplates
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face
import org.dicekeys.dicekey.FaceRead
import org.dicekeys.read.DiceKeyDrawable
import org.dicekeys.read.ReadDiceKeyActivity
import org.dicekeys.trustedapp.R
import org.dicekeys.trustedapp.databinding.ActivityMainBinding
import org.dicekeys.trustedapp.state.DiceKeyState



class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferenceManager : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferenceManager = PreferenceManager.getDefaultSharedPreferences(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.btnReadDicekey.setOnClickListener{ getDiceKeyOrTriggerRead()
            return@setOnClickListener
        }
        binding.btnForget.setOnClickListener{ forget() }
        binding.btnViewPublicKey.setOnClickListener{ viewPublicKey() }
        binding.btnAssembleFirstDicekey.setOnClickListener { startAssembleWizard() }
        binding.btnSelectHost.setOnClickListener{ viewOptions()}

    }

    private  fun viewOptions(){
        val popupMenu: PopupMenu = PopupMenu(this,binding.btnSelectHost)
        var i=0;
        for (recepitemplate in derivationRecipeTemplates) {
            popupMenu.menu.add(0,0,i++,recepitemplate.name)
        }
        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            val newIntent =Intent(this,DiceKeyWithDerivedValue::class.java)
            newIntent.putExtra("derivationRecipeTemplateIndex",item.order)
            startActivity(newIntent)
            //Toast.makeText(this@MainActivity, "You Clicked : " + derivationRecipeTemplates.get(item.order).name, Toast.LENGTH_SHORT).show()

            true
        })
        popupMenu.show()
    }
    private fun viewPublicKey() {
        val newIntent = Intent(this, DisplayPublicKeyActivity::class.java)
        startActivityForResult(newIntent, 0)
    }

    private fun forget() {
        DiceKeyState.clear()
        render()
    }

    private var diceKeyReadActivityStarted = false
    private fun getDiceKeyOrTriggerRead(): DiceKey<Face>? {
        val diceKey = DiceKeyState.diceKey
        if (diceKey == null && !diceKeyReadActivityStarted) {
            // We need to first trigger an action to load the key square, then come back to this
            // intent.
            diceKeyReadActivityStarted = true
            val intent = Intent(this, ReadDiceKeyActivity::class.java)
            startActivityForResult(intent, 0)
        }
        return diceKey
    }

    private fun startAssembleWizard() {
        val intent = Intent(this, AssembleInstructionsActivity::class.java)
        startActivity(intent)
    }

    private fun render() {
        try {
            // Render button changes
            val diceKeyPresent = DiceKeyState.diceKey != null
            val visibleIfDiceKeyPresent = if (diceKeyPresent) android.view.View.VISIBLE else android.view.View.GONE
            val visibleIfDiceKeyAbsent = if (!diceKeyPresent) android.view.View.VISIBLE else android.view.View.GONE
            binding.btnReadDicekey.visibility = visibleIfDiceKeyAbsent
            binding.btnAssembleFirstDicekey.visibility = visibleIfDiceKeyAbsent
            binding.btnForget.visibility = visibleIfDiceKeyPresent
            binding.btnViewPublicKey.visibility = visibleIfDiceKeyPresent
            binding.btnSelectHost.visibility = visibleIfDiceKeyPresent

            val diceKey = DiceKeyState.diceKey
            if (diceKey == null) {
                binding.diceKeyView.setImageDrawable(null)
                binding.diceKeyView.contentDescription = ""
            } else {
                val humanReadableForm: String = diceKey.toCanonicalRotation().toHumanReadableForm()
                val myDrawing = DiceKeyDrawable(this, diceKey)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        diceKeyReadActivityStarted = false
        if (
                resultCode == Activity.RESULT_OK &&
                data != null &&
                data.hasExtra(ReadDiceKeyActivity.Companion.Parameters.Response.diceKeyAsJson)
        ) {
            data.getStringExtra(ReadDiceKeyActivity.Companion.Parameters.Response.diceKeyAsJson)?.let { diceKeyAsJson ->
                FaceRead.diceKeyFromJsonFacesRead(diceKeyAsJson)?.let { diceKey ->
                    DiceKeyState.setDiceKeyRead(diceKey)
                }
            }
        }
        render()
    }

    override fun onResume() {
        super.onResume()
        render()
    }


}
