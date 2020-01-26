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
import java.time.Instant
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.min
import kotlin.random.Random


object UsbHid {
    object Commands {
        val CTAPHID_MSG: Byte = (0x03).toByte()
        val CTAPHID_INIT: Byte = (0x06).toByte()
        val CTAPHID_WINK: Byte = (0x08).toByte()
        val CTAPHID_ERROR: Byte = (0x3F).toByte()
        val CTAPHID_LOAD_SOLO_KEY = (0x62).toByte()
    }
    object Defaults {
        val TIMEOUT_MS = 500
    }
    // Interface Descriptor
    object Interface {
        // const val NumEndpoints: Byte = 2 // One IN and one OUT endpoint
        const val Class = UsbConstants.USB_CLASS_HID // HID
        // const val SubClass: Byte = 0x00 // No interface subclass
        // const val Protocol: Byte = 0x00 // No interface protocol
    }
    object Endpoints {
        object InputToSecurityToken {
            const val Address = 0x01 // 1, OUT
//            const val Attributes: Byte = 0x03 // Interrupt transfer
//            const val MaxPacketSize: Byte = 64 // 64-byte packet max
//            const val IntervalMs: Byte = 5 // Poll every 5 millisecond
        }

        object OutputFromSecurityToken {
            const val Address = 0x81 // 1, IN
//            const val Attributes: Byte = 0x03 // Interrupt transfer
//            const val MaxPacketSize: Byte = 64    // 64-byte packet max
//            const val IntervalMs: Byte = 5 // Poll every 5 millisecond
        }
    }
}

open class HidResponse (
    val hidResponsePacketBytes: ByteArray,
    cmdToValidate: Byte? = null,
    lengthToValidate: UShort? = null
) {
    init {
        if (hidResponsePacketBytes.size < 7) {
            // HID packets must have length > 7 bytes: 4 for channel + 1 for command + 2 for length
            throw java.io.IOException("HID packet too short")
        }
        if ((hidResponsePacketBytes[4].toInt() and 0x80) == 0) {
            // All initialization packets have the high bit of the command byte
            throw java.io.IOException("HID continuation packet received when initialization packet expected")
        }

    }
    class InvalidCommandException(message: String) : Exception(message)
    class InvalidLengthException(message: String) : Exception(message)

    private val hidResponsePacketByteBuffer = java.nio.ByteBuffer.wrap(hidResponsePacketBytes)

    val channel: Int get() = hidResponsePacketByteBuffer.getInt (0)
    // remove high bit from command
    val cmd: Byte get() = (hidResponsePacketBytes[4].toInt() and 0x07).toByte()
    val dataLength:  UShort get() = (
        (hidResponsePacketBytes[5].toUByte().toUInt() shl 8) or
        hidResponsePacketBytes[6].toUByte().toUInt()
    ).toUShort()

    init {
        if (cmdToValidate != null && cmd != cmdToValidate) {
            throw InvalidCommandException("Invalid command: $cmd observed when $cmdToValidate expected")
        }
        if (lengthToValidate != null && dataLength != lengthToValidate) {
            throw InvalidLengthException("Invalid length: $dataLength observed when $lengthToValidate expected")
        }
    }

    val morePacketsRequired: Boolean get() = dataLength.toInt() > (hidResponsePacketBytes.size - 7)
    val data: java.nio.ByteBuffer = java.nio.ByteBuffer.wrap(
        hidResponsePacketBytes.sliceArray(IntRange(7, hidResponsePacketBytes.lastIndex))
    ).order(java.nio.ByteOrder.BIG_ENDIAN)
}

data class ctapHidLoadSoloKey(
    val fixedPacketLengthInBytes: Int,
    val channel: Int,
    val keySeedAs96Bytes: ByteArray,
    val fidoCounter: UInt = (
            System.currentTimeMillis() -
                    GregorianCalendar(2020, 0, 0,0, 0, 0).timeInMillis
            ).toUInt()
) {
    val packetData: ByteArray get() =
        java.nio.ByteBuffer.allocate(keySeedAs96Bytes.size + 4)
            .order(java.nio.ByteOrder.BIG_ENDIAN)
            .putInt(fidoCounter.toInt())
            .put(keySeedAs96Bytes)
            .array()

    val packets: List<ByteArray> get() =
        hidPackets(channel, UsbHid.Commands.CTAPHID_LOAD_SOLO_KEY, packetData, fixedPacketLengthInBytes)
}

data class ctapHidInitResponseFields(private val hidResponse: HidResponse) {
    // DATA	8-byte nonce
    val nonce: Long get() = hidResponse.data.getLong(0)
    // DATA+8	4-byte channel ID
    val channelCreated get() = hidResponse.data.getInt(8)
    // DATA+12	CTAPHID protocol version identifier
    val ctapProtocalVersionIdentifier get() = hidResponse.data.get(12)
    // DATA+13	Major device version number
    val majorDeviceVersionNumber get() = hidResponse.data.get(13)
    // DATA+14	Minor device version number
    val minorDeviceVersionNumber get() = hidResponse.data.get(14)
    // DATA+15	Build device version number
    val buildDeviceVersionNumber get() = hidResponse.data.get(15)
    // DATA+16	Capabilities flags
    val capabilitiesFalgs get() = hidResponse.data.get(16)
}

private fun hidPackets(channel: Int, command: Byte, data: ByteArray, fixedPacketLengthInBytes: Int): List<ByteArray> {
    // HID starts with an initialization packet and, if that cannot hold all the data,
    // adds up to 128 continuation packets.
    //
    // See: https://fidoalliance.org/specs/fido-v2.0-ps-20190130/fido-client-to-authenticator-protocol-v2.0-ps-20190130.html#usb-message-and-packet-structure
    //
    //            INITIALIZATION PACKET
    //            Offset	Length	Mnemonic	Description
    //            0	    4	    CID	        Channel identifier
    //            4	    1	    CMD	        Command identifier (bit 7 always set)
    //            5	    1	    BCNTH	    High part of payload length
    //            6	    1	    BCNTL	    Low part of payload length
    //            7	    (s - 7)	DATA	    Payload data (s is equal to the fixed packet size)
    //
    //            CONTINUATION PACKET
    //            Offset	Length	Mnemonic	Description
    //            0   	4	    CID	        Channel identifier
    //            4	    1	    SEQ	        Packet sequence 0x00..0x7f (bit 7 always cleared)
    //            5   	(s - 5)	DATA	    Payload data (s is equal to the fixed packet size)

    // Using the maxPacketSize, calculate the max number of bytes allowed in each packet type
    val initializationPacketMetaDataSize = 7 // channelId (4 bytes) + length (2 bytes) + command (1 byte)
    val maxInitializationPacketDataSize = fixedPacketLengthInBytes - initializationPacketMetaDataSize
    val continuationPacketMetaDataSize = 5 // channelId (4 bytes) + sequence number (1 byte)
    val maxContinuationPacketDataSize = fixedPacketLengthInBytes - continuationPacketMetaDataSize

    // Ensure the data being sent can be fit in the 129 packets
    if (data.size > (
                    maxInitializationPacketDataSize + 128 * maxContinuationPacketDataSize
                    ) ||
            data.size > 0xFFFF
    ) {
        throw java.io.IOException("Data too long to transmit")
    }
    val packets = mutableListOf<ByteArray>()

    //
    // Create an initialization packet to start
    //

    val initializationPacketDataSize = min(maxInitializationPacketDataSize, data.size)
    val initializationPacket = java.nio.ByteBuffer.allocate(fixedPacketLengthInBytes)
            // In this implementation we'll opt for the CID to be big endian.
            // Since the only hard-coded value is 0xFFFFFFFF, which is the same in both endians,
            // either convention should work so long as we stick to it.
            // CID	Channel identifier (4 bytes).
            .order(java.nio.ByteOrder.BIG_ENDIAN)
            .putInt(channel)
            // CMD	Command identifier (bit 7 always set)
            .put((command.toInt() or 0x80).toByte())
            // Payload length, high-order byte first (big endian)
            .putShort(data.size.toShort())
            // Payload data (up to the first max number of bytes allowed in this packet)
            .put(data, 0, initializationPacketDataSize)
    // All packets must be padded to the max length
    if (initializationPacketDataSize < maxInitializationPacketDataSize) {
        val paddingBytesRequired = maxInitializationPacketDataSize - initializationPacketDataSize
        for (paddingBytesAdded in 1..paddingBytesRequired) {
            initializationPacket.put(0)
        }
    }
    packets.add(initializationPacket.array())

    // If more packets are required to send all the data, add continuation packets until
    // all data can be included in the packet sequence
    var bytesAlreadySent = initializationPacketDataSize
    var packetSequenceId: Byte = 0
    while (bytesAlreadySent < data.size) {
        val bytesRemainingToSend = data.size - bytesAlreadySent
        val continuationPacketDataSize = min(maxContinuationPacketDataSize, bytesRemainingToSend)
        val continuationPacket = java.nio.ByteBuffer.allocate(fixedPacketLengthInBytes)
                // In this implementation we'll opt for the CID to be big endian.
                // Since the only hard-coded value is 0xFFFFFFFF, which is the same in both endians,
                // either convention should work so long as we stick to it.
                // CID	Channel identifier (4 bytes)
                .order(java.nio.ByteOrder.BIG_ENDIAN)
                .putInt(channel)
                // SEQ	Packet sequence 0x00..0x7f (bit 7 always cleared),
                .put(packetSequenceId++)
                // Payload data (up to the first max number of bytes allowed in this packet)
                .put(data, bytesAlreadySent, continuationPacketDataSize)
        bytesAlreadySent += continuationPacketDataSize
        // All packets must be padded to the max length
        if (continuationPacketDataSize < maxContinuationPacketDataSize) {
            val paddingBytesRequired = maxContinuationPacketDataSize - continuationPacketDataSize
            for (paddingBytesAdded in 1..paddingBytesRequired) {
                continuationPacket.put(0)
            }
        }
        packets.add(continuationPacket.array())
    }
    return packets
}




class MainActivity : AppCompatActivity() {


    data class SoloKeyInterface(val usbManager: android.hardware.usb.UsbManager,
                                val device: UsbDevice,
                                val usbInterface: UsbInterface,
                                val toSecurityToken: UsbEndpoint,
                                val fromSecurityToken: UsbEndpoint
    )

    private fun getSoloKeyEndpoints(usbManager: android.hardware.usb.UsbManager, device: android.hardware.usb.UsbDevice) {
        var debugStr: String = "${device?.interfaceCount.toString()} interfaces:"

        for(i in 0 until device?.interfaceCount) {
            val usbInterface = device.getInterface(i)
            val isHID = usbInterface.interfaceClass == UsbHid.Interface.Class
            with(usbInterface) {
                debugStr += "\ninterface name=$name id=$id protocol=$interfaceProtocol class=$interfaceClass subclass=$interfaceSubclass isHID=$isHID;"
            }
            if (isHID) {
                var toSecurityToken: UsbEndpoint? = null
                var fromSecurityToken: UsbEndpoint? = null
                for (j in 0 until usbInterface.endpointCount) {
                    val endpoint = usbInterface.getEndpoint(j)
                    with (endpoint) {
                        debugStr += "\n  endpoint #=$endpointNumber type=$type address=$address direction=$direction interval=$interval"
                    }
                    if (endpoint.address == UsbHid.Endpoints.InputToSecurityToken.Address &&
                            endpoint.direction == UsbConstants.USB_DIR_OUT) {
                        toSecurityToken = endpoint
                    } else if (endpoint.address == UsbHid.Endpoints.OutputFromSecurityToken.Address &&
                            endpoint.direction == UsbConstants.USB_DIR_IN) {
                        fromSecurityToken = endpoint
                    }
                }
                if (toSecurityToken != null && fromSecurityToken != null) {
                    return writeToSoloKey(SoloKeyInterface(usbManager, device, usbInterface, toSecurityToken, fromSecurityToken))
                }
            }
        }
        throw java.io.IOException(debugStr)
    }

    private fun hidCtapInitPacket(maxPacketSize: Int, nonce: Long = Random.nextLong()): List<ByteArray> {
        val broadcastChannel: Int = (0xffffffff).toInt()
        val nonceBuffer = java.nio.ByteBuffer.allocate(8)
                .order(java.nio.ByteOrder.BIG_ENDIAN)
                .putLong(nonce)
                .array()
        return hidPackets(broadcastChannel, UsbHid.Commands.CTAPHID_INIT, nonceBuffer, maxPacketSize)
    }


    private fun writeToSoloKey(soloKeyInterface: SoloKeyInterface) {
        findViewById<TextView>(R.id.txt_json).text = "Ready to start write "
        try {
            val connection = soloKeyInterface.usbManager.openDevice(soloKeyInterface.device)?:
                throw java.io.IOException("Unable to connect to USB device!")

            connection.claimInterface(soloKeyInterface.usbInterface, true)
            findViewById<TextView>(R.id.txt_json).text = "Interface claimed"
            val nonce = Random.nextLong()
            val initPackets = hidCtapInitPacket(soloKeyInterface.toSecurityToken.maxPacketSize, nonce)
            for (packet in initPackets) {
                connection.bulkTransfer(soloKeyInterface.toSecurityToken, packet, packet.size, UsbHid.Defaults.TIMEOUT_MS)
            }
            findViewById<TextView>(R.id.txt_json).text = "Data written, awaiting response"

            var responseBuffer = ByteArray(soloKeyInterface.fromSecurityToken.maxPacketSize)
            connection.bulkTransfer(soloKeyInterface.fromSecurityToken, responseBuffer, soloKeyInterface.fromSecurityToken.maxPacketSize, UsbHid.Defaults.TIMEOUT_MS)
            val response = HidResponse(responseBuffer)
            findViewById<TextView>(R.id.txt_json).text = "Response received with command ${response.cmd}"

            if (response.cmd == UsbHid.Commands.CTAPHID_INIT) {
                val fields = ctapHidInitResponseFields(response)
                val bogusKeySeed = ByteArray(96) { 0.toByte() }
                findViewById<TextView>(R.id.txt_json).text = "Response received with nonce ${fields.nonce} expecting nonce $nonce, channel ${fields.channelCreated}"
                if (fields.nonce == nonce) {
                    val channelId = fields.channelCreated
                    val loadSoloKeyPacketCreator = ctapHidLoadSoloKey(
                            soloKeyInterface.fromSecurityToken.maxPacketSize,
                            channelId,
                            keySeedAs96Bytes = bogusKeySeed
                        )
                    val loadSoloKeyPackets = loadSoloKeyPacketCreator.packets
                    for (packet in loadSoloKeyPackets) {
                        connection.bulkTransfer(soloKeyInterface.toSecurityToken, packet, packet.size, UsbHid.Defaults.TIMEOUT_MS)
                    }
                    var loadKeyResponseBuffer = ByteArray(soloKeyInterface.fromSecurityToken.maxPacketSize)
                    connection.bulkTransfer(soloKeyInterface.fromSecurityToken, responseBuffer, soloKeyInterface.fromSecurityToken.maxPacketSize, UsbHid.Defaults.TIMEOUT_MS)
                    var loadKeyResponse = HidResponse(loadKeyResponseBuffer)
                    findViewById<TextView>(R.id.txt_json).text = "Received response ${loadKeyResponse.cmd}"
                }

            }


        } catch (e: Exception) {
            findViewById<TextView>(R.id.txt_json).text = "Exception ${e.message}\n${e.stackTrace}"
        }
    }

    private fun findSoloKey(usbManager: android.hardware.usb.UsbManager) {
        // findViewById<TextView>(R.id.txt_json).text = "Trying to find SoloKey ${Instant.now()}"

        val deviceList: HashMap<String, android.hardware.usb.UsbDevice> = usbManager.deviceList

        var debugStr: String = "${deviceList.size.toString()} devices detected:"

        deviceList.forEach { (name, device) -> run {
            findViewById<TextView>(R.id.txt_json).text = "Device $name"
            val isSolo: Boolean = (device.productId == 0x8acf && device.vendorId == 0x10c4)||
                    (device.productId == 0xa2ca && device.vendorId == 0x483)

            debugStr += "\n  {name: \"$name\", deviceName:${device.deviceName}, " +
                        "productName: ${device.productName}, " +
                        "manufacturerName: \"${device.manufacturerName}, " +
                        "vendorId: ${device.vendorId.toString(16)}, " +
                        "productId: ${device.productId.toString(16)}, " +
                        "isSolo: ${(if (isSolo) "true" else "false")}"
            if (isSolo) {
                if (!usbManager.hasPermission(device)) {
                    findViewById<TextView>(R.id.txt_json).text = "Found SoloKey but need permission"
                    if (permissionIntent != null) {
                        findViewById<TextView>(R.id.txt_json).text = "Device $name permission requested"
                        usbManager.requestPermission(device, permissionIntent)
                    }
                } else {
                    findViewById<TextView>(R.id.txt_json).text = "Found SoloKey - trying to connect"
                    try {
                        return connectToDeviceIfItIsASoloKey(device, usbManager)
                    } catch (e: java.io.IOException) {
                        findViewById<TextView>(R.id.txt_json).text = findViewById<TextView>(R.id.txt_json).text.toString() +
                                "Exception:\n ${e.message}\n${e.stackTrace} "

                    }
                }
            }
        }}
    }

    private fun connectToDeviceIfItIsASoloKey(
            device: UsbDevice,
            manager: android.hardware.usb.UsbManager = getSystemService(android.content.Context.USB_SERVICE) as android.hardware.usb.UsbManager
    ) {
        val isSolo: Boolean = (device.productId == 0x8acf && device.vendorId == 0x10c4)||
                (device.productId == 0xa2ca && device.vendorId == 0x483)

        if (isSolo && permissionIntent != null) {
            if (!manager.hasPermission(device)) {
                manager.requestPermission(device, permissionIntent)
            } else {
                try {
                    return getSoloKeyEndpoints(manager, device)
                } catch (e: java.io.IOException) {
                    findViewById<TextView>(R.id.txt_json).text = findViewById<TextView>(R.id.txt_json).text.toString() +
                            e.message

                }
            }
        }
    }

    val RC_READ_KEYSQR = 1

    private val INTENT_ACTION_USB_PERMISSION = "com.dicekeys.intents.GET_USB_PERMISSION"
    private val INTENT_ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED"

    private var permissionIntent: android.app.PendingIntent? = null

    private val usbReceiver = object : android.content.BroadcastReceiver() {

        override fun onReceive(context: android.content.Context, intent: Intent) {
            findViewById<TextView>(R.id.txt_json).text = "onReceive called"
            findViewById<TextView>(R.id.txt_json).text = "onReceive called ${intent.action?.format("%d")}"

            if (INTENT_ACTION_USB_PERMISSION == intent.action ||
                INTENT_ACTION_USB_ATTACHED == intent.action) {
                synchronized(this) {
                    findViewById<TextView>(R.id.txt_json).text = "onReceive called with permission intent"

                    val device: android.hardware.usb.UsbDevice? = intent.getParcelableExtra(android.hardware.usb.UsbManager.EXTRA_DEVICE)
                    if (intent.getBooleanExtra(android.hardware.usb.UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            connectToDeviceIfItIsASoloKey(device)
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

        val usbManager = getSystemService(android.content.Context.USB_SERVICE) as android.hardware.usb.UsbManager

        permissionIntent = android.app.PendingIntent.getBroadcast(this, 0, Intent(INTENT_ACTION_USB_PERMISSION), 0)
        val usbIntentFilter = IntentFilter()
        usbIntentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        usbIntentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        usbIntentFilter.addAction(INTENT_ACTION_USB_PERMISSION)
        registerReceiver(usbReceiver, usbIntentFilter)

        findSoloKey(usbManager)
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

                    val usbManager = getSystemService(android.content.Context.USB_SERVICE) as android.hardware.usb.UsbManager
                    findSoloKey(usbManager)
                    //val imageView = findViewById<KeySqrDrawable>(R.id.keysqr_canvas_container)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


}
