package com.dicekeys.fidowriter

import android.hardware.usb.*
import java.util.*

import kotlin.math.min
import android.util.Log
import com.dicekeys.fidowriter.ConnectionToWritableUsbFidoKey.HidResponseInitializationPacket.InvalidCommandException
import kotlin.random.Random

class LoadKeyNotAuthorizedByUserException(
    message: String? = "User failed to perform the authorization step to allow the key to be written"
): Exception(message)


/**
 * A USB connection to a FIDO security key over the CTAP HID protocol.
 *
 * Wow, that's an excessive list of acronyms.  Let's go through them.
 *   * USB: Universal Serial Bus
 *   * CTAP: Client to Authenticator Protocol of the FIDO protocol.
 *     (Wait, we're supposed to be reducing the list of acronyms to learn, not increasing it!)
 *   * FIDO: the Fast IDentity Online alliance, whose member companies are acronymophiles
 *   * HID: Human Interface Device portion of the CTAP protocol (the one for USB devices), as
 *     [documented](https://fidoalliance.org/specs/fido-v2.0-id-20180227/fido-client-to-authenticator-protocol-v2.0-id-20180227.html#usb))
 *     by the FIDO alliance.
 */
@kotlin.ExperimentalUnsignedTypes
open class ConnectionToWritableUsbFidoKey internal constructor(
        /*
         * The underlying USB connection created on construction
         */
        private val connection: UsbDeviceConnection,
        /**
         * The endpoint from the security key to this Android device
         */
        private val fromSecurityKeyEndpoint: UsbEndpoint,
        /**
         * The endpoint from this Android device to the security key
         */
        private val toSecurityKeyEndpoint: UsbEndpoint
) {

    companion object {
        /**
         * The broadcast channel, which is used to send messages requesting the creation of
         * other channels, is hard coded to 0xffffffff.
         */
        private val BroadcastChannel: Int = (0xffffffff).toInt()

        /**
         * Hardcoded command identifiers specified for the CTAP protocol
         */
        protected object Commands {
            const val CTAPHID_MSG: Byte = (0x03).toByte()
            const val CTAPHID_INIT: Byte = (0x06).toByte()
            const val CTAPHID_WINK: Byte = (0x08).toByte()
            const val CTAPHID_ERROR: Byte = (0x3F).toByte()
            const val CTAPHID_LOADKEY: Byte = (0x62).toByte()
        }

        /**
         * The error code returned when an operation is denied
         * (e.g. if the user did not press the key three times to authorize a write.)
         */
        protected object Errors {
            val CTAP2_ERR_OPERATION_DENIED: Byte = (0x27).toByte()
        }

        /**
         * Internal default values for timeouts and reties
         */
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

        /**
         * Construct a [ConnectionToWritableUsbFidoKey] class for a given [device] which can be
         * will be opened via the provided [usbManager].
         */
        @JvmStatic
        fun connect(usbManager: UsbManager, device: UsbDevice): ConnectionToWritableUsbFidoKey {
            try {
                // Step through the set of interfaces offered by a device to look for
                // an HID interface with endpoints to and from the security token.
                for (i in 0 until device.interfaceCount) {
                    val usbInterface = device.getInterface(i)
                    // Reject non HID interfaces
                    val isHID = usbInterface.interfaceClass == Interface.Class
                    if (!isHID) {
                        continue
                    }
                    // The two endpoints to search for in this interface
                    var toSecurityToken: UsbEndpoint? = null
                    var fromSecurityToken: UsbEndpoint? = null
                    for (j in 0 until usbInterface.endpointCount) {
                        val endpoint = usbInterface.getEndpoint(j)
                        if (endpoint.address == Endpoints.ToSecurityToken.Address &&
                                endpoint.direction == UsbConstants.USB_DIR_OUT) {
                            // Found an endpoint from this android device to the security key
                            toSecurityToken = endpoint
                        } else if (endpoint.address == Endpoints.FromSecurityToken.Address &&
                                endpoint.direction == UsbConstants.USB_DIR_IN) {
                            // Found an endpoint from the security key back to this android device
                            fromSecurityToken = endpoint
                        }
                    }
                    if (toSecurityToken != null && fromSecurityToken != null) {
                        // We have found an HID interface that had both required endpoints
                        // Create the underlying USB connection and return a
                        // new instance of this class as an interface to the connection.
                        val connection = usbManager.openDevice(device)
                                ?: throw java.io.IOException("Unable to connect to USB device!")
                        connection.claimInterface(usbInterface, true)
                        return ConnectionToWritableUsbFidoKey(connection, fromSecurityToken, toSecurityToken)
                    }
                }
            } catch (e: Exception) {
                // We failed to find an interface to the FIDO security key.
                Log.d("USB Fido Connect Failed", "${e.message} ${e.stackTrace}")
            }
            throw java.io.IOException("Failed to find an interface to the FIDO security key")
        }
    }


    private val channel: Int = establishChannel()

    protected fun finalize() {
        connection.close()
    }

    /**
     * A class for parsing a byte array representing an
     * HID Response Initialization Packet into an
     * [channel], [cmd], [dataLength], and [data] array.
     *
     * Throw an [InvalidCommandException] if [cmdToValidate] is set and
     * [cmd] is not [cmdToValidate].
     *
     * Throw an [InvalidLengthException] if [dataLengthToValidate] is set and
     * the message [dataLength] is not [lengthToValidate].
     */
    @ExperimentalUnsignedTypes
    open class HidResponseInitializationPacket(
            val hidResponsePacketBytes: ByteArray,
            /**
             * Throw an [InvalidCommandException] if the [cmd] != [cmdToValidate]
             */
            cmdToValidate: Byte? = null,
            /**
             * Throw an [InvalidLengthException] if the message [dataLength] is not [dataLengthToValidate]
             */
            dataLengthToValidate: UShort? = null
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

        /**
         * The channel the packet was received on. (The first four bytes of the packet)
         */
        val channel: Int get() = hidResponsePacketByteBuffer.getInt (0)
        // remove high bit from command
        /**
         * The command is the lower 7 bits of the 5th byte in the packet (index 4,
         * after the 4-byte channel)
         */
        val cmd: Byte get() = (hidResponsePacketBytes[4].toInt() and 0x7f).toByte()

        /**
         * The message's data length is the unsigned two-byte big-endian value starting at the
         * sixth byte (index 5). It may exceed the length of the data in this single packet.
         */
        val dataLength:  UShort get() = (
                (hidResponsePacketBytes[5].toUByte().toUInt() shl 8) or
                        hidResponsePacketBytes[6].toUByte().toUInt()
                ).toUShort()

        init {
            if (cmdToValidate != null && cmd != cmdToValidate) {
                throw InvalidCommandException("Invalid command: $cmd observed when $cmdToValidate expected")
            }
            if (dataLengthToValidate != null && dataLength != dataLengthToValidate) {
                throw InvalidLengthException("Invalid length: $dataLength observed when $dataLengthToValidate expected")
            }
        }

        /**
         * More packets are required if the total bytes in the message is greater than the number
         * of bytes in this packet.
         */
        val morePacketsRequired: Boolean get() = dataLength.toInt() > (hidResponsePacketBytes.size - 7)

        /**
         * The data bytes are the bytes starting with the 8th byte (the byte at index 7)
         */
        val data: java.nio.ByteBuffer = java.nio.ByteBuffer.wrap(
                hidResponsePacketBytes.sliceArray(IntRange(7, hidResponsePacketBytes.lastIndex))
        ).order(java.nio.ByteOrder.BIG_ENDIAN)
    }

    /**
     * A data class for accessing the fields of a CTAP HID Init Response packet
     */
    data class CtapHidInitResponseFields(private val hidResponse: HidResponseInitializationPacket) {
        // DATA	8-byte nonce
        val nonce: Long get() = hidResponse.data.getLong(0)
        // DATA+8	4-byte channel ID
        val channelCreated get() = hidResponse.data.getInt(8)
        // DATA+12	CTAPHID protocol version identifier
        val ctapProtocolVersionIdentifier get() = hidResponse.data.get(12)
        // DATA+13	Major device version number
        val majorDeviceVersionNumber get() = hidResponse.data.get(13)
        // DATA+14	Minor device version number
        val minorDeviceVersionNumber get() = hidResponse.data.get(14)
        // DATA+15	Build device version number
        val buildDeviceVersionNumber get() = hidResponse.data.get(15)
        // DATA+16	Capabilities flags
        val capabilitiesFlags get() = hidResponse.data.get(16)
    }

    /**
     * Create a list of one or more packets to send a [command] and its associated [data]
     * over a [channel] of a [ConnectionToWritableUsbFidoKey].
     *
     * A sequence of packets may be needed given the length restrictions of HID packets.
     *
     *         // HID starts with an initialization packet and, if that cannot hold all the data,
     * adds up to 128 continuation packets.
     *
     * See: https://fidoalliance.org/specs/fido-v2.0-ps-20190130/fido-client-to-authenticator-protocol-v2.0-ps-20190130.html#usb-message-and-packet-structure
     *
     *            INITIALIZATION PACKET
     *            Offset	Length	Mnemonic	Description
     *            0	    4	    CID	        Channel identifier
     *            4	    1	    CMD	        Command identifier (bit 7 always set)
     *            5	    1	    BCNTH	    High part of payload length
     *            6	    1	    BCNTL	    Low part of payload length
     *            7	    (s - 7)	DATA	    Payload data (s is equal to the fixed packet size)
     *
     *            CONTINUATION PACKET
     *            Offset	Length	Mnemonic	Description
     *            0   	4	    CID	        Channel identifier
     *            4	    1	    SEQ	        Packet sequence 0x00..0x7f (bit 7 always cleared)
     *            5   	(s - 5)	DATA	    Payload data (s is equal to the fixed packet size)
     */
    private fun createPackets(channel: Int, command: Byte, data: ByteArray): List<ByteArray> {
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


    /**
     * Transmit a message consisting of a [command] and associated [data] over a [channel].
     */
    private fun transmit(channel: Int, command: Byte, data: ByteArray) {
        val packets = createPackets(channel, command, data)
        for (packet in packets) {
            connection.bulkTransfer(toSecurityKeyEndpoint, packet, packet.size, Defaults.TIMEOUT_MS)
//            Log.d(
//                "Packet sent",
//                "[${packet.size}]=${packet.asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }}"
//            )
        }
    }

    /**
     * Create a communications channel by sending a request to initialize a channel
     * (CTAPHID_INIT) to the broadcast channel and then awaiting a response.
     */
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

    /**
     * Await a message over a [channel], timeout after [timeout] ms
     * and then retrying up to [retries] time with [retryTimeout] ms
     * for each of the retries after the first attempt.
     */
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

    /**
     * Send a [command] and [data], then await and return the response.
     * While awaiting the response, timeout after [timeout] ms
     * and then retry up to [retries] time with [retryTimeout] ms
     * for each of the retries (attempts after the first attempt).
     */
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

    /**
     * Issue the command to re-seed the security key with [keySeedAs32Bytes].
     */
    fun loadKeySeed(
      keySeedAs32Bytes: ByteArray,
      extState: ByteArray = ByteArray(0),
      commandVersion: Byte = 1
    ) {
        if (keySeedAs32Bytes.size != 32)
            throw java.lang.IllegalArgumentException("Key seed must be 32 bytes")
        if (extState.size > 256)
            throw java.lang.IllegalArgumentException("ExtState may not exceed 256 bytes")

        val loadKeySeedData: ByteArray =
                java.nio.ByteBuffer.allocate(1 + keySeedAs32Bytes.size + extState.size)
                  .order(java.nio.ByteOrder.BIG_ENDIAN)
                  .put(commandVersion)
                  .put(keySeedAs32Bytes)
                  .put(extState)
                  .array()
        val response = sendCommand(Commands.CTAPHID_LOADKEY, loadKeySeedData, 14000)
        if (response.cmd == Commands.CTAPHID_LOADKEY && response.dataLength == 0.toUShort()) {
            // Success.
            return
        }
        // Handle errors
        if (response.cmd == Commands.CTAPHID_ERROR && response.data[0] == Errors.CTAP2_ERR_OPERATION_DENIED) {
            throw LoadKeyNotAuthorizedByUserException()
        } else {
            var dataByteArray = ByteArray(response.dataLength.toInt())
            response.data.get(dataByteArray)
            val dataAsHex: String = dataByteArray.asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }
            throw java.io.IOException("Failed to write key due to unforeseen error: command=${response.cmd}, error=$dataAsHex")
        }
    }

}
