package org.dicekeys.trustedapp.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.dicekeys.keysqr.Face
import org.dicekeys.keysqr.FaceRead
import org.dicekeys.keysqr.KeySqr
import org.dicekeys.trustedapp.state.KeySqrState
import org.dicekeys.read.KeySqrDrawable
import org.dicekeys.read.ReadKeySqrActivity
import org.dicekeys.trustedapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.btnReadDicekey.setOnClickListener{ getDiceKeyOrTriggerRead()
            return@setOnClickListener
        }
        binding.btnForget.setOnClickListener{ forget() }
        binding.btnViewPublicKey.setOnClickListener{ viewPublicKey() }
    }

    private fun viewPublicKey() {
        val newIntent = Intent(this, DisplayPublicKeyActivity::class.java)
        startActivityForResult(newIntent, 0)
    }

    private fun forget() {
        KeySqrState.clear()
        render()
    }

    private var keySqrReadActivityStarted = false
    private fun getDiceKeyOrTriggerRead(): KeySqr<Face>? {
        val keySqr = KeySqrState.diceKey
        if (keySqr == null && !keySqrReadActivityStarted) {
            // We need to first trigger an action to load the key square, then come back to this
            // intent.
            keySqrReadActivityStarted = true
            val intent = Intent(this, ReadKeySqrActivity::class.java)
            startActivityForResult(intent, 0)
        }
        return keySqr
    }

    private fun render() {
        try {
            // Render button changes
            val diceKeyPresent = KeySqrState.diceKey != null
            val visibleIfDiceKeyPresent = if (diceKeyPresent) android.view.View.VISIBLE else android.view.View.GONE
            val visibleIfDiceKeyAbsent = if (!diceKeyPresent) android.view.View.VISIBLE else android.view.View.GONE
            binding.btnReadDicekey.visibility = visibleIfDiceKeyAbsent
            binding.btnForget.visibility = visibleIfDiceKeyPresent
            binding.btnViewPublicKey.visibility = visibleIfDiceKeyPresent
            val keySqr = KeySqrState.diceKey
            if (keySqr == null) {
                binding.keysqrView.setImageDrawable(null)
                binding.keysqrView.contentDescription = ""
            } else {
                val humanReadableForm: String = keySqr.toCanonicalRotation().toHumanReadableForm()
                val myDrawing = KeySqrDrawable(this, keySqr)
                binding.keysqrView.setImageDrawable(myDrawing)
                binding.keysqrView.contentDescription = humanReadableForm
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
        keySqrReadActivityStarted = false
        if (
                resultCode == Activity.RESULT_OK &&
                data != null &&
                data.hasExtra("keySqrAsJson")
        ) {
            data.getStringExtra("keySqrAsJson")?.let { keySqrAsJson ->
                FaceRead.keySqrFromJsonFacesRead(keySqrAsJson)?.let { keySqr ->
                    KeySqrState.setDiceKeyRead(keySqr)
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
