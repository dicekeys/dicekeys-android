package org.dicekeys.activities

import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.dicekeys.state.KeySqrState
import org.dicekeys.readkeysqr.KeySqrDrawable
//import com.dicekeys.fidowriter.UsbCtapHidDeviceList
import org.dicekeys.R.id


class DisplayDiceKeyActivity : AppCompatActivity() {
    private val INTENT_ACTION_USB_PERMISSION_EVENT = "org.dicekeys.intents.USB_PERMISSION_EVENT"

    private val seedKeyDerivationOptions : String = """{
            |"keyType":"Seed",
            |"keyLengthInBytes":96,
            |"hashFunction":{"algorithm":"Argon2id"},
            |"restrictToClientApplicationsIdPrefixes":["org.dicekeys.fido"]
            |}""".trimMargin("|")

    private val REQUEST_CODE_PUBLIC_KEY = 3

    private lateinit var permissionIntent: android.app.PendingIntent
//    private lateinit var deviceList: UsbCtapHidDeviceList
    private lateinit var writeButton: Button
    private lateinit var forgetDiceKeyButton: Button
    private lateinit var viewPublicKeyButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(org.dicekeys.R.layout.activity_display_dice_key)
        writeButton = findViewById(id.btn_write_to_fido)
        forgetDiceKeyButton = findViewById(id.btn_forget)
        viewPublicKeyButton = findViewById(id.btn_view_public_key)

        permissionIntent = android.app.PendingIntent.getBroadcast(this, 0, Intent(INTENT_ACTION_USB_PERMISSION_EVENT), 0)

//        deviceList = UsbCtapHidDeviceList(
//                getSystemService(android.content.Context.USB_SERVICE) as UsbManager,
//                permissionIntent
//        )

        forgetDiceKeyButton.setOnClickListener{
            KeySqrState.clear()
            var newIntent = Intent()
            setResult(RESULT_OK, newIntent)
            unregisterReceiver(usbReceiver)
            finish()
        }

        viewPublicKeyButton.setOnClickListener{
            val newIntent = Intent(this, DisplayPublicKeyActivity::class.java)
            startActivityForResult(newIntent, REQUEST_CODE_PUBLIC_KEY)
        }

//        writeButton.setOnClickListener{
//            writeToCurrentFidoToken()
//        }

    }

    private fun render() {

        try {
            KeySqrState.keySqr?.let {
                val humanReadableForm: String = it.toCanonicalRotation().toHumanReadableForm(true)
                val myDrawing = KeySqrDrawable(this, it)
                val image: ImageView = findViewById(id.keysqr_view)
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

        render()
        renderButtonChanges()
    }

    fun renderButtonChanges() {
//        if (deviceList.devices.size == 1) deviceList.devices.values.firstOrNull()?.let {
//            val device = it
//            if (deviceList.hasPermission(device)) {
//                // Enable the write button
//                writeButton.isEnabled = true
//                writeButton.visibility = android.view.View.VISIBLE
//                return
//            }
//        }
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
        return KeySqrState.keySqr?.getSeed(seedKeyDerivationOptions, "org.dicekeys.fido")?.seedBytes
    }


//    fun writeToCurrentFidoToken() {
//        if (deviceList.devices.size == 1) deviceList.devices.values.firstOrNull()?.let {
//            writeToFidoToken(it)
//        }
//    }

//    private fun writeToFidoToken(device: UsbDevice) {
//        writeButton.isEnabled = false
//        val instructionToast = Toast.makeText(applicationContext,
//                "Press the button your FIDO token three times.", Toast.LENGTH_LONG)
//        Thread(Runnable {
//            try {
//                getSeed()?.let {
//                    val seed = it
//                    synchronized(this) {
//                        val connection = deviceList.connect(device)
//                        runOnUiThread{
//                            instructionToast.show()
//                        }
//                        connection.loadKeySeed(seed)
//                    }
//                    runOnUiThread {
//                        instructionToast.cancel()
//                        val successMessageToast = Toast.makeText(applicationContext,
//                                "FIDO token written.",
//                                // Integrate this into the above string to see what's written:
//                                //   ${seed.asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') } }
//                                Toast.LENGTH_SHORT
//                        )
//                        successMessageToast.show()
//                        writeButton.isEnabled = true
//                    }
//                }
//            } catch (e: java.lang.Exception) {
//                runOnUiThread{
//                    val failureMessageToast = Toast.makeText(applicationContext,
//                            "FIDO token write failed: ${e.message}", Toast.LENGTH_LONG)
//                    failureMessageToast.show()
//                    writeButton.isEnabled = true
//                }
//            }
//        }).start()
//    }
}
