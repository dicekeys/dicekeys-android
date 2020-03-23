package com.dicekeys.fidowriter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.dicekeys.Api
import org.dicekeys.uses.seedfido.UsbCtapHidDeviceList

class MainActivity : AppCompatActivity() {
    private lateinit var diceKeysApi: org.dicekeys.Api


    private val INTENT_ACTION_USB_PERMISSION_EVENT = "org.dicekeys.intents.USB_PERMISSION_EVENT"

    private val seedKeyDerivationOptionsJson : String = """{
            |"keyType":"Seed",
            |"keyLengthInBytes":96,
            |"hashFunction":{"algorithm":"Argon2id"},
            |"restrictToClientApplicationsIdPrefixes":["com.dicekeys.fidowriter."]
            |}""".trimMargin("|")

    private val REQUEST_CODE_PUBLIC_KEY = 3

    private lateinit var permissionIntent: android.app.PendingIntent
    private lateinit var deviceList: UsbCtapHidDeviceList
    private var isWriteUnderway: Boolean = false
    private lateinit var secretTextView: EditText
    private lateinit var buttonWriteSecretToFidoToken: Button
    private lateinit var forgetSecretButton: Button
    private lateinit var generateSecretFromDiceKeyButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        diceKeysApi = org.dicekeys.Api.create(this)
        setContentView(R.layout.activity_main)
        secretTextView = findViewById(R.id.edit_text_secret)
        buttonWriteSecretToFidoToken = findViewById(R.id.btn_write_secret_to_fido_token)
        forgetSecretButton = findViewById(R.id.btn_forget_secret)
        generateSecretFromDiceKeyButton = findViewById(R.id.btn_generate_secret_from_dicekey)

        permissionIntent = android.app.PendingIntent.getBroadcast(this, 0, Intent(INTENT_ACTION_USB_PERMISSION_EVENT), 0)

        deviceList = UsbCtapHidDeviceList(
                getSystemService(android.content.Context.USB_SERVICE) as UsbManager,
                permissionIntent
        )

        btn_forget_secret.setOnClickListener{
            edit_text_secret.text.clear()
            render()
        }

        btn_write_secret_to_fido_token.setOnClickListener{
            writeToCurrentFidoToken()
        }

        btn_generate_secret_from_dicekey.setOnClickListener {
            diceKeysApi.getSeed(seedKeyDerivationOptionsJson, object: Api.GetSeedCallback {
                override fun onGetSeedSuccess(seed: ByteArray, originalIntent: Intent) {
                    edit_text_secret.text.clear()
                    edit_text_secret.text.insert(0,seed.joinToString(separator = ""){ String.format("%02x",(it.toInt() and 0xFF))})
                    render()
                }
                override fun onGetSeedFail(exception: Exception, originalIntent: Intent) {
                    TODO("Not yet implemented")
                    render()
                }
            })
        }

    }

    private fun render() {
        btn_forget_secret.isEnabled = edit_text_secret.text.isNotEmpty()
        btn_write_secret_to_fido_token.isEnabled =
            isSeedValid() &&
            deviceReadyToWrite() &&
            !isWriteUnderway
    }

    override fun onResume() {
        super.onResume()

        val usbIntentFilter = IntentFilter()
        usbIntentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        usbIntentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        usbIntentFilter.addAction(INTENT_ACTION_USB_PERMISSION_EVENT)
        registerReceiver(usbReceiver, usbIntentFilter)

        render()
    }

    fun getSeed(): ByteArray? {
        return try{edit_text_secret.text.chunked(2).map { it.toUpperCase().toInt(16).toByte() }.toByteArray()}catch(e:Throwable){null}
    }

    fun isSeedValid(s: ByteArray? = getSeed()) : Boolean {
        return s != null && s.size == 96
    }

    fun deviceReadyToWrite(): Boolean =
        deviceList.devices.values.firstOrNull()?.let {
            deviceList.hasPermission(it)
        } ?: false

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        diceKeysApi.handleOnActivityResult(data)
        render()
    }

    private val usbReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context, intent: Intent) {
            render()
        }
    }

    fun writeToCurrentFidoToken() {
        if (deviceList.devices.size == 1) deviceList.devices.values.firstOrNull()?.let {
            writeToFidoToken(it)
        }
    }

    private fun writeToFidoToken(device: UsbDevice) {
        if (isWriteUnderway)
            return
        val seed = getSeed()
        if (seed == null || !isSeedValid(seed))
            return
        isWriteUnderway = true
        val instructionToast = Toast.makeText(applicationContext,
                "Press the button on your FIDO token three times.", Toast.LENGTH_LONG)
        Thread(Runnable {
            try {
                synchronized(this) {
                    val connection = deviceList.connect(device)
                    runOnUiThread{
                        instructionToast.show()
                    }
                    connection.loadKeySeed(seed)
                }
                runOnUiThread {
                    isWriteUnderway = false
                    instructionToast.cancel()
                    val successMessageToast = Toast.makeText(applicationContext,
                            "FIDO token successfully written.",
                            // Integrate this into the above string to see what's written:
                            //   ${seed.asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') } }
                            Toast.LENGTH_SHORT
                    )
                    successMessageToast.show()
                    render()
                }
            } catch (e: java.lang.Exception) {
                runOnUiThread{
                    isWriteUnderway = false
                    val failureMessageToast = Toast.makeText(applicationContext,
                            "FIDO token write failed: ${e.message}", Toast.LENGTH_LONG)
                    failureMessageToast.show()
                    render()
                }
            }
        }).start()
    }
}
