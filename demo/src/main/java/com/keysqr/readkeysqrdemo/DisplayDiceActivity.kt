package com.keysqr.readkeysqrdemo

import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.keysqr.FaceRead
import com.keysqr.KeySqr
import com.keysqr.readkeysqr.KeySqrDrawable
import com.keysqr.uses.seedfido.UsbCtapHidDeviceList


class DisplayDiceActivity : AppCompatActivity() {
    private val INTENT_ACTION_USB_PERMISSION_EVENT = "com.dicekeys.intents.USB_PERMISSION_EVENT"

    private val seedKeyDerivationOptions : String = """{
            |"keyType":"Seed",
            |"keyLengthInBytes":96,
            |"hashFunction":{"algorithm":"Argon2id"},
            |"restrictToClientApplicationsIdPrefixes":["com.dicekeys.fido"]
            |}""".trimMargin("|")

    private val REQUEST_CODE_PUBLIC_KEY = 3

    private var keySqr: KeySqr<FaceRead>? = null

    private lateinit var permissionIntent: android.app.PendingIntent
    private lateinit var deviceList: UsbCtapHidDeviceList
    private lateinit var writeButton: Button
    private lateinit var forgetDiceKeyButton: Button
    private lateinit var viewPublicKeyButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_dice)
        writeButton = findViewById(R.id.btn_write_to_fido)
        forgetDiceKeyButton = findViewById(R.id.btn_forget)
        viewPublicKeyButton = findViewById(R.id.btn_view_public_key)

        val keySqrAsJson = intent.extras?.getString("keySqrAsJson")

        permissionIntent = android.app.PendingIntent.getBroadcast(this, 0, Intent(INTENT_ACTION_USB_PERMISSION_EVENT), 0)

        deviceList = UsbCtapHidDeviceList(
                getSystemService(android.content.Context.USB_SERVICE) as UsbManager,
                permissionIntent
        )

        forgetDiceKeyButton.setOnClickListener{
            setResult(RESULT_OK, intent)
            finish()
        }

        viewPublicKeyButton.setOnClickListener{
            val newIntent = Intent(this, DisplayPublicKeyActivity::class.java)
            newIntent.putExtra("keySqrAsJson", keySqrAsJson)
            startActivityForResult(newIntent, REQUEST_CODE_PUBLIC_KEY)
        }

        writeButton.setOnClickListener{
            writeToCurrentFidoToken()
        }

        if (keySqrAsJson != null && keySqrAsJson != "null") try {
            keySqr = FaceRead.keySqrFromJsonFacesRead(keySqrAsJson)
            keySqr?.let {
                val humanReadableForm: String = it.toCanonicalRotation().toHumanReadableForm(true)
                val myDrawing = KeySqrDrawable(this, it)
                val image: ImageView = findViewById(R.id.keysqr_view)
                image.setImageDrawable(myDrawing)
                image.contentDescription = humanReadableForm
            }
        } catch (e: Exception) {
            val sw = java.io.StringWriter()
            val pw = java.io.PrintWriter(sw)
            e.printStackTrace(pw)
            val stackTrace: String = sw.toString()
            android.util.Log.e("We caught exception", stackTrace)
            // findViewById<TextView>(R.id.txt_json).text = stackTrace
        }

    }

    override fun onResume() {
        super.onResume()

        val usbIntentFilter = IntentFilter()
        usbIntentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        usbIntentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        usbIntentFilter.addAction(INTENT_ACTION_USB_PERMISSION_EVENT)
        registerReceiver(usbReceiver, usbIntentFilter)

        renderButtonChanges()
    }

    fun renderButtonChanges() {
        if (deviceList.devices.size == 1) deviceList.devices.values.firstOrNull()?.let {
            val device = it
            if (deviceList.hasPermission(device)) {
                // Enable the write button
                writeButton.isEnabled = true
                writeButton.visibility = android.view.View.VISIBLE
                return
            }
        }
        // Disable the write button
        writeButton.isEnabled = false
        writeButton.visibility = android.view.View.INVISIBLE
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        renderButtonChanges()
    }

    private val usbReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context, intent: Intent) {
            renderButtonChanges()
        }
    }


    private fun getSeed(): ByteArray? {
        // Should not be run on UI thread
        return keySqr?.getSeed(seedKeyDerivationOptions, "com.dicekeys.fido")
    }


    fun writeToCurrentFidoToken() {
        if (deviceList.devices.size == 1) deviceList.devices.values.firstOrNull()?.let {
            writeToFidoToken(it)
        }
    }

    private fun writeToFidoToken(device: UsbDevice) {
        Thread(Runnable {
            getSeed()?.let {
                val seed = it
                try {
                    synchronized(this) {
                        val connection = deviceList.connect(device)
                        val result = connection.loadKeySeed(seed)
                    }
                    runOnUiThread(Runnable {
                        // FIXME -- report result
                    })
                } catch (e: java.lang.Exception) {
                    runOnUiThread(Runnable {
                        // FIXME -- report write failure
                    })
                }
            }
        }).start()
    }



}

//        deviceList?.devices?.values?.firstOrNull()?.let {
//            val connection = deviceList?.connect(it)
//            var bogusKeySeed = ByteArray(96)
//            Random.Default.nextBytes(bogusKeySeed)
//            connection?.loadKeySeed(bogusKeySeed)
//        }

// val keySqrAsJson = intent.extras?.getString("keySqrAsJson")


//            val publicKey = keySqr.getPublicKey(
//                    """{"keyType":"Public"}""",
//                    ""
//            )
//            image.setImageDrawable(publicKey.getJsonQrCode().toDrawable(resources))

//            val seed: ByteArray = keySqr.getSeed(
//                    "{" +
//                            "\"keyType\":\"Seed\"," +
//                            "\"keyLengthInBytes\":96," +
//                            "\"hashFunction\":{\"algorithm\":\"Argon2id\"}," +
//                            "\"restrictToClientApplicationsIdPrefixes\":[\"com.dicekeys.fido\"]" +
//                            "}",
//                    "com.dicekeys.fido"
//            )
//            val seedStr = seed.asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }


//            var wroteToFidoKey = false
//            deviceList?.devices?.values?.firstOrNull()?.let {
//                val connection = deviceList?.connect(it)
//                connection?.loadKeySeed(seed)
//                wroteToFidoKey = true
//            }


//            findViewById<TextView>(R.id.txt_json).text =
//                    "{wroteToFidoKey:$wroteToFidoKey,\ndice:\"$humanReadableForm,\"\n" +
//                            "publicKey:0x${publicKey.asHexDigits},\n"+
//                            "seed:0x$seedStr}"

//val imageView = findViewById<KeySqrDrawable>(R.id.keysqr_canvas_container)
