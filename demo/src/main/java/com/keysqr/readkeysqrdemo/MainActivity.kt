package com.keysqr.readkeysqrdemo

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.keysqr.readkeysqr.ReadKeySqrActivity
import com.keysqr.uses.seedfido.UsbCtapHidDeviceList
import kotlin.random.Random


class MainActivity : AppCompatActivity() {
    val RC_READ_KEYSQR = 1
    val RC_DISPLAY_DICE = 2

    private lateinit var buttonStart: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        buttonStart = findViewById(R.id.btn_start)

        buttonStart.setOnClickListener{
            val intent = Intent(this, ReadKeySqrActivity::class.java)
            startActivityForResult(intent, RC_READ_KEYSQR)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_READ_KEYSQR && resultCode == Activity.RESULT_OK && data!=null) {
            // After a DiceKey has been returned by the ReadKeySqrActivity,
            // launch the DisplayDiceKey activity to display it
            val keySqrAsJson: String? = data.getStringExtra("keySqrAsJson")
            if (keySqrAsJson != null && keySqrAsJson != "null") {
                val intent = Intent(this, DisplayDiceActivity::class.java)
                intent.putExtra("keySqrAsJson", keySqrAsJson)
                startActivityForResult(intent, RC_DISPLAY_DICE)
            }
        }
    }


}
