package com.keysqr.readkeysqrdemo

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.*
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.keysqr.readkeysqr.KeySqrDrawable
import com.keysqr.readkeysqr.ReadKeySqrActivity
import com.keysqr.readkeysqr.jsonGlobalPublicKey
import com.keysqr.readkeysqr.keySqrFromJsonFacesRead
import com.keysqr.uses.seedfido.UsbCtapHidConnection
import kotlin.random.Random


class MainActivity : AppCompatActivity() {


//    data class SoloKeyInterface(val usbManager: android.hardware.usb.UsbManager,
//                                val device: UsbDevice,
//                                val usbInterface: UsbInterface,
//                                val toSecurityToken: UsbEndpoint,
//                                val fromSecurityToken: UsbEndpoint
//    )

//    private fun getSoloKeyEndpoints(usbManager: android.hardware.usb.UsbManager, device: android.hardware.usb.UsbDevice) {
//        var debugStr: String = "${device?.interfaceCount.toString()} interfaces:"
//
//        for(i in 0 until device?.interfaceCount) {
//            val usbInterface = device.getInterface(i)
//            val isHID = usbInterface.interfaceClass == UsbHidConstants.Interface.Class
//            with(usbInterface) {
//                debugStr += "\ninterface name=$name id=$id protocol=$interfaceProtocol class=$interfaceClass subclass=$interfaceSubclass isHID=$isHID;"
//            }
//            if (isHID) {
//                var toSecurityToken: UsbEndpoint? = null
//                var fromSecurityToken: UsbEndpoint? = null
//                for (j in 0 until usbInterface.endpointCount) {
//                    val endpoint = usbInterface.getEndpoint(j)
//                    with (endpoint) {
//                        debugStr += "\n  endpoint #=$endpointNumber type=$type address=$address direction=$direction interval=$interval"
//                    }
//                    if (endpoint.address == UsbHidConstants.Endpoints.InputToSecurityToken.Address &&
//                            endpoint.direction == UsbConstants.USB_DIR_OUT) {
//                        toSecurityToken = endpoint
//                    } else if (endpoint.address == UsbHidConstants.Endpoints.OutputFromSecurityToken.Address &&
//                            endpoint.direction == UsbConstants.USB_DIR_IN) {
//                        fromSecurityToken = endpoint
//                    }
//                }
//                if (toSecurityToken != null && fromSecurityToken != null) {
//                    return writeToSoloKey(SoloKeyInterface(usbManager, device, usbInterface, toSecurityToken, fromSecurityToken))
//                }
//            }
//        }
//        throw java.io.IOException(debugStr)
//    }


//
//    private fun writeToSoloKey(soloKeyInterface: SoloKeyInterface) {
//        findViewById<TextView>(R.id.txt_json).text = "Ready to start write "
//        try {
//            val fidoKeyConnection = UsbCtapHidConnection(
//                    soloKeyInterface.usbManager, soloKeyInterface.device,
//                    soloKeyInterface.fromSecurityToken, soloKeyInterface.toSecurityToken
//            )
//            var bogusKeySeed = ByteArray(96)
//            Random.nextBytes(bogusKeySeed)
//            fidoKeyConnection.loadKeySeed(bogusKeySeed)
////
////            val connection = soloKeyInterface.usbManager.openDevice(soloKeyInterface.device)?:
////                throw java.io.IOException("Unable to connect to USB device!")
////
////            connection.claimInterface(soloKeyInterface.usbInterface, true)
////            findViewById<TextView>(R.id.txt_json).text = "Interface claimed"
////            val nonce = Random.nextLong()
////            val initPackets = hidCtapInitPacket(soloKeyInterface.toSecurityToken.maxPacketSize, nonce)
////            for (packet in initPackets) {
////                connection.bulkTransfer(soloKeyInterface.toSecurityToken, packet, packet.size, UsbHidConstants.Defaults.TIMEOUT_MS)
////                android.util.Log.d(
////                        "Packet sent",
////                        "[${packet.size}]=${packet.asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }}"
////                )
////            }
////            findViewById<TextView>(R.id.txt_json).text = "Data written, awaiting response"
////
////            var hidInitCommandResponseBuffer = ByteArray(soloKeyInterface.fromSecurityToken.maxPacketSize)
////            connection.bulkTransfer(soloKeyInterface.fromSecurityToken, hidInitCommandResponseBuffer, soloKeyInterface.fromSecurityToken.maxPacketSize, UsbHidConstants.Defaults.TIMEOUT_MS)
////            android.util.Log.d(
////                    "Packet received",
////                    "[${hidInitCommandResponseBuffer.size}]=${hidInitCommandResponseBuffer.asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }}"
////            )
////            val responseToInitCommand = HidResponse(hidInitCommandResponseBuffer)
////            findViewById<TextView>(R.id.txt_json).text = "Response received with command ${responseToInitCommand.cmd}"
////
////            if (responseToInitCommand.cmd == UsbHidConstants.Commands.CTAPHID_INIT) {
////                val fields = ctapHidInitResponseFields(responseToInitCommand)
////                findViewById<TextView>(R.id.txt_json).text = "Response received with nonce ${fields.nonce} expecting nonce $nonce, channel ${fields.channelCreated}"
////                if (fields.nonce == nonce) {
////                    val channelId = fields.channelCreated
////                    val loadSoloKeyPacketCreator = CtapHidLoadSoloKey(
////                            soloKeyInterface.fromSecurityToken.maxPacketSize,
////                            channelId,
////                            keySeedAs96Bytes = bogusKeySeed
////                        )
////                    val loadSoloKeyPackets = loadSoloKeyPacketCreator.packets
////                    for (packet in loadSoloKeyPackets) {
////                        connection.bulkTransfer(soloKeyInterface.toSecurityToken, packet, packet.size, UsbHidConstants.Defaults.TIMEOUT_MS)
////                        android.util.Log.d(
////                                "Packet sent",
////                                "[${packet.size}]=${packet.asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }}"
////                        )
////                    }
////                    var loadKeyResponseBuffer = ByteArray(soloKeyInterface.fromSecurityToken.maxPacketSize)
////                    connection.bulkTransfer(soloKeyInterface.fromSecurityToken, loadKeyResponseBuffer, soloKeyInterface.fromSecurityToken.maxPacketSize,
////                            // allow 16 seconds for user to click buttons
////                            16 * 1000) // UsbHid.Defaults.TIMEOUT_MS)
////                    android.util.Log.d(
////                            "Packet received",
////                            "[${loadKeyResponseBuffer.size}]=${loadKeyResponseBuffer.asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }}"
////                    )
////                    var loadKeyResponse = HidResponse(loadKeyResponseBuffer)
////                    findViewById<TextView>(R.id.txt_json).text = "Received response ${loadKeyResponse.cmd}"
////                }
////
////            }
//
//
//        } catch (e: Exception) {
//            findViewById<TextView>(R.id.txt_json).text = "Exception ${e.message}\n${e.stackTrace}"
//        }
//    }
//
//    private fun findSoloKey(usbManager: android.hardware.usb.UsbManager) {
//        // findViewById<TextView>(R.id.txt_json).text = "Trying to find SoloKey ${Instant.now()}"
//
//        val deviceList: HashMap<String, android.hardware.usb.UsbDevice> = usbManager.deviceList
//
//        var debugStr: String = "${deviceList.size.toString()} devices detected:"
//
//        deviceList.forEach { (name, device) -> run {
//            findViewById<TextView>(R.id.txt_json).text = "Device $name"
//            val isSolo: Boolean = (device.productId == 0x8acf && device.vendorId == 0x10c4)||
//                    (device.productId == 0xa2ca && device.vendorId == 0x483)
//
//            debugStr += "\n  {name: \"$name\", deviceName:${device.deviceName}, " +
//                        "productName: ${device.productName}, " +
//                        "manufacturerName: \"${device.manufacturerName}, " +
//                        "vendorId: ${device.vendorId.toString(16)}, " +
//                        "productId: ${device.productId.toString(16)}, " +
//                        "isSolo: ${(if (isSolo) "true" else "false")}"
//            if (isSolo) {
//                if (!usbManager.hasPermission(device)) {
//                    findViewById<TextView>(R.id.txt_json).text = "Found SoloKey but need permission"
//                    if (permissionIntent != null) {
//                        findViewById<TextView>(R.id.txt_json).text = "Device $name permission requested"
//                        usbManager.requestPermission(device, permissionIntent)
//                    }
//                } else {
//                    findViewById<TextView>(R.id.txt_json).text = "Found SoloKey - trying to connect"
//                    try {
//                        return connectToDeviceIfItIsASoloKey(device, usbManager)
//                    } catch (e: java.io.IOException) {
//                        findViewById<TextView>(R.id.txt_json).text = findViewById<TextView>(R.id.txt_json).text.toString() +
//                                "Exception:\n ${e.message}\n${e.stackTrace} "
//
//                    }
//                }
//            }
//        }}
//    }
//
//    private fun connectToDeviceIfItIsASoloKey(
//            device: UsbDevice,
//            manager: android.hardware.usb.UsbManager = getSystemService(android.content.Context.USB_SERVICE) as android.hardware.usb.UsbManager
//    ) {
//        val isSolo: Boolean = (device.productId == 0x8acf && device.vendorId == 0x10c4)||
//                (device.productId == 0xa2ca && device.vendorId == 0x483)
//
//        if (isSolo && permissionIntent != null) {
//            if (!manager.hasPermission(device)) {
//                manager.requestPermission(device, permissionIntent)
//            } else {
//                try {
//                    return getSoloKeyEndpoints(manager, device)
//                } catch (e: java.io.IOException) {
//                    findViewById<TextView>(R.id.txt_json).text = findViewById<TextView>(R.id.txt_json).text.toString() +
//                            e.message
//
//                }
//            }
//        }
//    }

    val RC_READ_KEYSQR = 1

    private val INTENT_ACTION_USB_PERMISSION = "com.dicekeys.intents.GET_USB_PERMISSION"
    private val INTENT_ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED"

    private var permissionIntent: android.app.PendingIntent? = null

    private var usbCtapHidConnection: UsbCtapHidConnection? = null

    private val usbReceiver = object : android.content.BroadcastReceiver() {

        override fun onReceive(context: android.content.Context, intent: Intent) {
            findViewById<TextView>(R.id.txt_json).text = "onReceive called"
            findViewById<TextView>(R.id.txt_json).text = "onReceive called ${intent.action?.format("%d")}"

            if (INTENT_ACTION_USB_PERMISSION == intent.action ||
                INTENT_ACTION_USB_ATTACHED == intent.action) {
                synchronized(this) {
                    findViewById<TextView>(R.id.txt_json).text = "onReceive called with permission intent"

                    val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            val usbManager = getSystemService(android.content.Context.USB_SERVICE) as UsbManager
                            usbCtapHidConnection = UsbCtapHidConnection.find(usbManager, device)
                            // fixme -- remove me
                            var bogusKeySeed = ByteArray(96)
                            Random.Default.nextBytes(bogusKeySeed)
                            usbCtapHidConnection?.let {
                                it.loadKeySeed(bogusKeySeed)
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
            usbCtapHidConnection = UsbCtapHidConnection.find(usbManager, it)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_READ_KEYSQR && resultCode == Activity.RESULT_OK && data!=null) {
            val keySqrAsJson: String? = data.getStringExtra("keySqrAsJson")
            if (keySqrAsJson != null && keySqrAsJson != "null") {
                val keySqr = keySqrFromJsonFacesRead(keySqrAsJson)
                if (keySqr != null) {
                    val humanReadableForm: String = keySqr.toCanonicalRotation().toHumanReadableForm(true)
                    val publicKeyStr = jsonGlobalPublicKey(
                            humanReadableForm,
                            "{\"purpose\":\"ForPublicKeySealedMessagesWithRestrictionsEnforcedPostDecryption\"}"
                    )
                    findViewById<TextView>(R.id.txt_json).text = publicKeyStr

                    val myDrawing = KeySqrDrawable(this, keySqr)
                    val image: ImageView = findViewById(R.id.keysqr_view)
                    image.setImageDrawable(myDrawing)
                    image.contentDescription = humanReadableForm

                    findViewById<TextView>(R.id.txt_json).text = "" +
                            "${findViewById<TextView>(R.id.txt_json).text} ${humanReadableForm}$"

                    // FIXME -- use C libraries to derive key
                    var bogusKeySeed = ByteArray(96)
                    Random.Default.nextBytes(bogusKeySeed)
                    usbCtapHidConnection?.let {
                        it.loadKeySeed(bogusKeySeed)
                    }
                    //val imageView = findViewById<KeySqrDrawable>(R.id.keysqr_canvas_container)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


}
