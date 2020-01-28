package com.keysqr.uses.seedfido

import android.hardware.usb.*
import java.util.*

import kotlin.math.min
import android.util.Log
import kotlin.random.Random

class LoadKeyNotAuthorizedByUserException(
    message: String? = "User failed to perform the authorization step to allow the key to be written"
): Exception(message)

open class UsbCtapHidConnection(// usbManager: UsbManager,
                                // device: UsbDevice,
                                private val connection: UsbDeviceConnection,
                                private val fromSecurityKeyEndpoint: UsbEndpoint,
                                private val toSecurityKeyEndpoint: UsbEndpoint
) {
    private val BroadcastChannel: Int = (0xffffffff).toInt()

    companion object {
        protected object Commands {
            val CTAPHID_MSG: Byte = (0x03).toByte()
            val CTAPHID_INIT: Byte = (0x06).toByte()
            val CTAPHID_WINK: Byte = (0x08).toByte()
            val CTAPHID_ERROR: Byte = (0x3F).toByte()
            val CTAPHID_LOADKEY: Byte = (0x62).toByte()
        }

        protected object Errors {
            val CTAP2_ERR_OPERATION_DENIED: Byte = (0x27).toByte()
        }

        protected object Defaults {
            val TIMEOUT_MS = 500
            val RETRIES = 5
            val RETRY_TIMEOUT_MS = 100
        }

        // Interface Descriptor
        protected object Interface {
            // const val NumEndpoints: Byte = 2 // One IN and one OUT endpoint
            const val Class = UsbConstants.USB_CLASS_HID // HID
            // const val SubClass: Byte = 0x00 // No interface subclass
            // const val Protocol: Byte = 0x00 // No interface protocol
        }

        protected object Endpoints {
            object ToSecurityToken {
                const val Address = 0x01 // 1, OUT
                //            const val Attributes: Byte = 0x03 // Interrupt transfer
                //            const val MaxPacketSize: Byte = 64 // 64-byte packet max
                //            const val IntervalMs: Byte = 5 // Poll every 5 millisecond
            }

            object FromSecurityToken {
                const val Address = 0x81 // 1, IN
                //            const val Attributes: Byte = 0x03 // Interrupt transfer
                //            const val MaxPacketSize: Byte = 64    // 64-byte packet max
                //            const val IntervalMs: Byte = 5 // Poll every 5 millisecond
            }
        }

        fun connect(usbManager: UsbManager, device: UsbDevice): UsbCtapHidConnection {
            try {
                for (i in 0 until device?.interfaceCount) {
                    val usbInterface = device.getInterface(i)
                    val isHID = usbInterface.interfaceClass == Interface.Class
                    if (!isHID) {
                        continue
                    }
                    var toSecurityToken: UsbEndpoint? = null
                    var fromSecurityToken: UsbEndpoint? = null
                    for (j in 0 until usbInterface.endpointCount) {
                        val endpoint = usbInterface.getEndpoint(j)
                        if (endpoint.address == Endpoints.ToSecurityToken.Address &&
                                endpoint.direction == UsbConstants.USB_DIR_OUT) {
                            toSecurityToken = endpoint
                        } else if (endpoint.address == Endpoints.FromSecurityToken.Address &&
                                endpoint.direction == UsbConstants.USB_DIR_IN) {
                            fromSecurityToken = endpoint
                        }
                    }
                    if (toSecurityToken != null && fromSecurityToken != null) {
                        val connection = usbManager.openDevice(device)
                                ?: throw java.io.IOException("Unable to connect to USB device!")
                        connection.claimInterface(usbInterface, true)
                        return UsbCtapHidConnection(connection, fromSecurityToken, toSecurityToken)
                    }
                }
            } catch (e: Exception) {
                Log.d("USB Fido Connect Failed", "${e.message} ${e.stackTrace}")
            }
            throw java.io.IOException("Could not find FIDO interface")
        }
    }

//        fun find(usbManager: UsbManager, permissionIntent: android.app.PendingIntent): UsbCtapHidConnection? {
//            val deviceList: HashMap<String, UsbDevice> = usbManager.deviceList
//
//            Log.d("Hid Devices Detected", "${deviceList.size}")
//
//            for ( (name, device) in deviceList) {
//                val supportsLoadKey: Boolean =
//                        // SoloKeys
//                        (device.productId == 0x8acf && device.vendorId == 0x10c4) ||
//                                (device.productId == 0xa2ca && device.vendorId == 0x483)
//
//                Log.d("Device", "{name: \"$name\", deviceName:${device.deviceName}, " +
//                        "productName: ${device.productName}, " +
//                        "manufacturerName: \"${device.manufacturerName}, " +
//                        "vendorId: ${device.vendorId.toString(16)}, " +
//                        "productId: ${device.productId.toString(16)}, " +
//                        "supportsLoadKey: ${(if (supportsLoadKey) "true" else "false")}"
//                )
//                if (!supportsLoadKey) {
//                    continue
//                }
//                if (!usbManager.hasPermission(device)) {
//                    Log.d("Need permission", "Security key that supports loadkey but need permission")
//                    usbManager.requestPermission(device, permissionIntent)
//                    continue
//                }
//                val usbCtapHidConnection: UsbCtapHidConnection? = connect(usbManager, device)
//                if (usbCtapHidConnection != null) {
//                    return usbCtapHidConnection
//                }
//            }
//            return null
//        }
//    }

    private val channel: Int = establishChannel()

    protected fun finalize() {
        connection.close()
    }

    open class HidResponseInitializationPacket (
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
        val cmd: Byte get() = (hidResponsePacketBytes[4].toInt() and 0x7f).toByte()
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

    data class CtapHidInitResponseFields(private val hidResponse: HidResponseInitializationPacket) {
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

    private fun createPackets(channel: Int, command: Byte, data: ByteArray): List<ByteArray> {
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
        val maxInitializationPacketDataSize = toSecurityKeyEndpoint.maxPacketSize - initializationPacketMetaDataSize
        val continuationPacketMetaDataSize = 5 // channelId (4 bytes) + sequence number (1 byte)
        val maxContinuationPacketDataSize = toSecurityKeyEndpoint.maxPacketSize - continuationPacketMetaDataSize

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
        val initializationPacket = java.nio.ByteBuffer.allocate(toSecurityKeyEndpoint.maxPacketSize)
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
            val continuationPacket = java.nio.ByteBuffer.allocate(toSecurityKeyEndpoint.maxPacketSize)
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


    private fun transmit(channel: Int, command: Byte, data: ByteArray) {
        val packets = createPackets(channel, command, data)
        for (packet in packets) {
            connection.bulkTransfer(toSecurityKeyEndpoint, packet, packet.size, Defaults.TIMEOUT_MS)
            Log.d(
                "Packet sent",
                "[${packet.size}]=${packet.asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }}"
            )
        }
    }

    private fun establishChannel(): Int {
        val nonce = Random.nextLong()
        val nonceBuffer = java.nio.ByteBuffer.allocate(8)
                .order(java.nio.ByteOrder.BIG_ENDIAN)
                .putLong(nonce)
                .array()
        transmit(BroadcastChannel, Commands.CTAPHID_INIT, nonceBuffer)
        val response = receive(BroadcastChannel)
        // Validate that the response is a result sent to the broadcast channel
        if (response.channel == BroadcastChannel && response.cmd == Commands.CTAPHID_INIT) {
            val fields = CtapHidInitResponseFields(response)
            // Validate that the response nonce matches the request nonce
            if (fields.nonce == nonce) {
                return fields.channelCreated
            }
        }
        throw java.io.IOException("Unable to establish a channel to the FIDO key")
    }

    private fun receive(
            channel: Int,
            timeout: Int = Defaults.TIMEOUT_MS,
            retries: Int = 1 + Defaults.RETRIES,
            retryTimeout: Int = Defaults.RETRY_TIMEOUT_MS
    ): HidResponseInitializationPacket {
        for (attempt in 0..retries) {
            val readBuffer = ByteArray(fromSecurityKeyEndpoint.maxPacketSize)
            val bytesRead = connection.bulkTransfer(
                    fromSecurityKeyEndpoint,
                    readBuffer,
                    fromSecurityKeyEndpoint.maxPacketSize,
                    if (attempt == 0) timeout else retryTimeout
            )
            if (bytesRead > 0) {
                Log.d(
                        "Packet received",
                        "[${readBuffer.size}]=${readBuffer.asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }}"
                )
                val response = HidResponseInitializationPacket(readBuffer)
                if (response.channel == channel) {
                    return HidResponseInitializationPacket(readBuffer)
                }
            }
        }
        throw java.io.IOException("Timeout or no data read")
    }

    fun sendCommand(
            command: Byte,
            data: ByteArray,
            responseTimeout: Int = Defaults.TIMEOUT_MS,
            responseRetries: Int = 1 + Defaults.RETRIES,
            responseRetryTimeout: Int = Defaults.RETRY_TIMEOUT_MS
    ): HidResponseInitializationPacket {
        transmit(channel, command, data)
        return receive(channel, responseTimeout, responseRetries, responseRetryTimeout)
    }

    fun loadKeySeed(
            keySeedAs96Bytes: ByteArray,
            // Default the FIDO counter to the number of 10-second intervals since Jan 1, 2020 UTC
            fidoCounter: UInt = ( (
                    System.currentTimeMillis() -
                            GregorianCalendar(2020, 0, 0, 0, 0, 0).timeInMillis
                    ) / 10).toUInt(),
            commandVersion: UInt = 0u
    ) {
        if (keySeedAs96Bytes.size != 96) {
            throw java.lang.IllegalArgumentException("Key seed must be 96 bytes")
        }
        val loadKeySeedData: ByteArray =
                java.nio.ByteBuffer.allocate(4 + 4 + keySeedAs96Bytes.size)
                        .order(java.nio.ByteOrder.BIG_ENDIAN)
                        // 4 byte command version
                        .putInt(commandVersion.toInt())
                        // 4 byte FIDO counter
                        .putInt(fidoCounter.toInt())
                        // This version uses a 96-byte FIDO key seed
                        .put(keySeedAs96Bytes)
                        .array()
        val response = sendCommand(Commands.CTAPHID_LOADKEY, loadKeySeedData, 14000)
        if (response.cmd != Commands.CTAPHID_LOADKEY) {
            val error: Byte = response.data[0]
            if (error == Errors.CTAP2_ERR_OPERATION_DENIED) {
                throw LoadKeyNotAuthorizedByUserException()
            } else {
                throw java.io.IOException("Failed to write key due to unforeseen error # $error.")
            }
        }
    }

}
