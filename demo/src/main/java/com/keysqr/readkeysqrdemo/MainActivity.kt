package com.keysqr.readkeysqrdemo

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import com.keysqr.FaceRead
import com.keysqr.readkeysqr.KeySqrDrawable
import com.keysqr.readkeysqr.ReadKeySqrActivity
import com.keysqr.uses.seedfido.UsbCtapHidDeviceList
import kotlin.random.Random


class MainActivity : AppCompatActivity() {
    val RC_READ_KEYSQR = 1

    private val INTENT_ACTION_USB_PERMISSION = "com.dicekeys.intents.GET_USB_PERMISSION"
    private val INTENT_ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED"

    private var permissionIntent: android.app.PendingIntent? = null
    private var deviceList: com.keysqr.uses.seedfido.UsbCtapHidDeviceList? = null

    private val usbReceiver = object : android.content.BroadcastReceiver() {

        override fun onReceive(context: android.content.Context, intent: Intent) {
            if (INTENT_ACTION_USB_PERMISSION == intent.action ||
                INTENT_ACTION_USB_ATTACHED == intent.action) {
                synchronized(this) {
                    val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            try {
                                // FIXME -- don't load bogus key
                                val connection = deviceList?.connect(device)
                                var bogusKeySeed = ByteArray(96)
                                Random.Default.nextBytes(bogusKeySeed)
                                connection?.loadKeySeed(bogusKeySeed)
                            } catch (e: Exception) {
                                android.util.Log.d("Exception", "${e.message}, ${e.stackTrace}")
                            }
                        }
                    } else {
                        android.util.Log.d("USB Permission", "permission denied for device $device")
                    }
                    return
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_start).setOnClickListener{
            val intent = Intent(this, ReadKeySqrActivity::class.java)
            startActivityForResult(intent, RC_READ_KEYSQR)
        }

        permissionIntent = android.app.PendingIntent.getBroadcast(this, 0, Intent(INTENT_ACTION_USB_PERMISSION), 0)
        val usbIntentFilter = IntentFilter()
        usbIntentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        usbIntentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        usbIntentFilter.addAction(INTENT_ACTION_USB_PERMISSION)
        registerReceiver(usbReceiver, usbIntentFilter)

        val usbManager = getSystemService(android.content.Context.USB_SERVICE) as UsbManager
        permissionIntent?.let {
            deviceList = UsbCtapHidDeviceList(usbManager, it)
        }
        deviceList?.devices?.values?.firstOrNull()?.let {
            val connection = deviceList?.connect(it)
            var bogusKeySeed = ByteArray(96)
            Random.Default.nextBytes(bogusKeySeed)
            connection?.loadKeySeed(bogusKeySeed)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_READ_KEYSQR && resultCode == Activity.RESULT_OK && data!=null) {
            val keySqrAsJson: String? = data.getStringExtra("keySqrAsJson")
            if (keySqrAsJson != null && keySqrAsJson != "null") try {
                val keySqr = FaceRead.keySqrFromJsonFacesRead(keySqrAsJson)
                if (keySqr == null) {
                    return
                }
                val humanReadableForm: String = keySqr.toCanonicalRotation().toHumanReadableForm(true)
                val myDrawing = KeySqrDrawable(this, keySqr)
                val image: ImageView = findViewById(R.id.keysqr_view)
                image.setImageDrawable(myDrawing)
                image.contentDescription = humanReadableForm


                val publicKey = keySqr.getPublicKey(
                        """{"keyType":"Public"}""",
                        ""
                )
                image.setImageDrawable(publicKey.getJsonQrCode().toDrawable(resources))

                val seed: ByteArray = keySqr.getSeed(
                        "{" +
                                "\"keyType\":\"Seed\"," +
                                "\"keyLengthInBytes\":96," +
                                "\"hashFunction\":{\"algorithm\":\"Argon2id\"}," +
                                "\"restrictToClientApplicationsIdPrefixes\":[\"com.dicekeys.fido\"]" +
                                "}",
                        "com.dicekeys.fido"
                )
                val seedStr = seed.asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }


                var wroteToFidoKey = false
                deviceList?.devices?.values?.firstOrNull()?.let {
                    val connection = deviceList?.connect(it)
                    connection?.loadKeySeed(seed)
                    wroteToFidoKey = true
                }


                findViewById<TextView>(R.id.txt_json).text =
                        "{wroteToFidoKey:$wroteToFidoKey,\ndice:\"$humanReadableForm,\"\n" +
                        "publicKey:0x${publicKey.asHexDigits},\n"+
                        "seed:0x$seedStr}"

                //val imageView = findViewById<KeySqrDrawable>(R.id.keysqr_canvas_container)
            } catch (e: Exception) {
                val sw = java.io.StringWriter()
                val pw = java.io.PrintWriter(sw)
                e.printStackTrace(pw)
                val stackTrace: String = sw.toString()
                findViewById<TextView>(R.id.txt_json).text = stackTrace

            }
        }
    }


}
