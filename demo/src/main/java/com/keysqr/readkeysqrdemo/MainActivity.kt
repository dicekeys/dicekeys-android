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

    private val INTENT_ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED"

    private var permissionIntent: android.app.PendingIntent? = null
    private lateinit var buttonStart: Button

    private val emptyUsbReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context, intent: Intent) {
            if (INTENT_ACTION_USB_ATTACHED == intent.action ) {
                // This is here so that the OS knows we're listening to these events
                synchronized(this) {
                    val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    val withPermission = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                    if (device != null && withPermission) {
                        // Not actually doing anything here
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        buttonStart = findViewById(R.id.btn_start)

        buttonStart.setOnClickListener{
            val intent = Intent(this, ReadKeySqrActivity::class.java)
            startActivityForResult(intent, RC_READ_KEYSQR)
        }
    }

    override fun onResume() {
        super.onResume()
        permissionIntent = android.app.PendingIntent.getBroadcast(this, 0, Intent(INTENT_ACTION_USB_ATTACHED), 0)
        val usbIntentFilter = IntentFilter()
        usbIntentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        registerReceiver(emptyUsbReceiver, usbIntentFilter)
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
