package com.keysqr.readkeysqrdemo

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.ImageView
import com.keysqr.readkeysqr.KeySqrDrawable
import com.keysqr.readkeysqr.ReadKeySqrActivity
import com.keysqr.readkeysqr.jsonGlobalPublicKey
import com.keysqr.readkeysqr.keySqrFromJsonFacesRead

object UsbHid {
    // Interface Descriptor
    object Interface {
        const val NumEndpoints: Byte = 2 // One IN and one OUT endpoint
        const val Class: Byte = 0x03 // HID
        const val SubClass: Byte = 0x00 // No interface subclass
        const val Protocol: Byte = 0x00 // No interface protocol
    }
    object Endpoints {
        object InputToSecurityToken {
            val Adresss: Byte = 0x01 // 1, OUT
            val Attributes: Byte = 0x03 // Interrupt transfer
            val MaxPacketSize: Byte = 64 // 64-byte packet max
            val IntervalMs: Byte = 5 // Poll every 5 millisecond
        }

        object OutputFromSecurityToken {
            val Adresss: Byte = 0x81 as Byte // 1, IN
            val Attributes: Byte = 0x03 // Interrupt transfer
            val MaxPacketSize: Byte = 64    // 64-byte packet max
            val IntervalMs: Byte = 5 // Poll every 5 millisecond
        }
    }
}

class MainActivity : AppCompatActivity() {

    val RC_READ_KEYSQR = 1

    private val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_start).setOnClickListener{
            val intent = Intent(this, ReadKeySqrActivity::class.java)
            startActivityForResult(intent, RC_READ_KEYSQR)
        }

        //val manager = getSystemService(android.content.Context.USB_SERVICE) as android.hardware.usb.UsbManager
        permissionIntent = android.app.PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
        val filter = android.content.IntentFilter(ACTION_USB_PERMISSION)
        registerReceiver(usbReceiver, filter)

        writeUsbDeviceNames()
    }

    var permissionIntent: android.app.PendingIntent? = null

    private val usbReceiver = object : android.content.BroadcastReceiver() {

        override fun onReceive(context: android.content.Context, intent: Intent) {
            if (ACTION_USB_PERMISSION == intent.action) {
                synchronized(this) {
                    val device: android.hardware.usb.UsbDevice? = intent.getParcelableExtra(android.hardware.usb.UsbManager.EXTRA_DEVICE)

                    if (intent.getBooleanExtra(android.hardware.usb.UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        device?.apply {
                            writeInterfacesAndEndpoints(device)
                        }
                    } else {
                        android.util.Log.d("USB Permission", "permission denied for device $device")
                    }
                }
            }
        }
    }

    fun writeInterfacesAndEndpoints(device: android.hardware.usb.UsbDevice) {
        findViewById<TextView>(R.id.txt_json).text = findViewById<TextView>(R.id.txt_json).text.toString() +
                " " + device?.interfaceCount.toString() + " interfaces"
        for(i in 0 until device?.interfaceCount) with(device.getInterface(i)){
            val isHID = interfaceClass == UsbHid.Interface.Class.toInt()
            findViewById<TextView>(R.id.txt_json).text = findViewById<TextView>(R.id.txt_json).text.toString() +
                    "\ninterface name=$name id=$id protocol=$interfaceProtocol class=$interfaceClass subclass=$interfaceSubclass isHID=$isHID;"
            if (isHID) {
                for (j in 0 until endpointCount) with(getEndpoint(j)) {
                    findViewById<TextView>(R.id.txt_json).text = findViewById<TextView>(R.id.txt_json).text.toString() +
                            "\n  endpoint #=$endpointNumber type=$type address=$address direction=$direction interval=$interval"
                }
            }
        }
    }

    fun writeUsbDeviceNames() {
        val manager = getSystemService( android.content.Context.USB_SERVICE) as android.hardware.usb.UsbManager
        val deviceList: HashMap<String, android.hardware.usb.UsbDevice> = manager.deviceList
        findViewById<TextView>(R.id.txt_json).text =
            findViewById<TextView>(R.id.txt_json).text.toString() +
                "\n" + deviceList.size.toString() + " devices detected"

        deviceList.forEach { (name, device) -> run {
            val isSolo: Boolean = (device.productId == 0x8acf && device.vendorId == 0x10c4)||
                    (device.productId == 0xa2ca && device.vendorId == 0x483)

            val newText: String =
                    findViewById<TextView>(R.id.txt_json).text.toString() +
                            "\n{ deviceName: \"" + name + ":" + device.deviceName +
                            "\" productName: \"" + device.productName +
                            "\", manufacturerName: \"" + device.manufacturerName +
                            "\", vendorId: " + device.vendorId.toString(16) +
                            ", productId: " + device.productId.toString(16) +
                            ", isSolo: " + (if (isSolo) "true" else "false") +
            "}"
            findViewById<TextView>(R.id.txt_json).text = newText
            if (isSolo && permissionIntent != null) {
                if (!manager.hasPermission(device)) {
                    manager.requestPermission(device, permissionIntent)
                } else {
                    writeInterfacesAndEndpoints(device)
                }
            }
        }}
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

                    writeUsbDeviceNames()
                    //val imageView = findViewById<KeySqrDrawable>(R.id.keysqr_canvas_container)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


}
