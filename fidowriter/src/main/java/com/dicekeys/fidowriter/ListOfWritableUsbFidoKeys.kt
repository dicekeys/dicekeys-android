package com.dicekeys.fidowriter

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log

/**
 * An class that maintains a list of FIDO security keys
 * that support the write operation and are available
 * over the USB bus and to which the app has permission to
 * write to.
 */
class ListOfWritableUsbFidoKeys(
    private val usbManager: UsbManager,
    private val permissionIntent: android.app.PendingIntent
) {

    val devices: Map<String, UsbDevice> get() =
        usbManager.deviceList.filter { (name, device) -> run{
            val supportsLoadKey: Boolean =
                    // SoloKeys
                    (device.productId == 0x8acf && device.vendorId == 0x10c4) ||
                            (device.productId == 0xa2ca && device.vendorId == 0x483)

//            Log.d("Device", "{name: \"$name\", deviceName:${device.deviceName}, " +
//                    "productName: ${device.productName}, " +
//                    "manufacturerName: \"${device.manufacturerName}, " +
//                    "vendorId: ${device.vendorId.toString(16)}, " +
//                    "productId: ${device.productId.toString(16)}, " +
//                    "supportsLoadKey: ${(if (supportsLoadKey) "true" else "false")}"
//            )

            if (!supportsLoadKey) {
                return@filter false
            }
            val hasPermission = usbManager.hasPermission(device)
            if (!hasPermission) {
                Log.d("Need permission", "Security key that supports loadkey but need permission")
                usbManager.requestPermission(device, permissionIntent)
            }
            return@filter supportsLoadKey && hasPermission
        }}

    /**
     * Establish a connection to a device by name
     */
    fun connect(deviceName: String): ConnectionToWritableUsbFidoKey {
        val device = devices[deviceName]
        if (device == null) {
            throw java.lang.IllegalArgumentException("No such device")
        } else {
            return connect(device)
        }
    }

    /**
     * Establish a connection to a USB device that supports FIDO writing
     */
    fun connect(device: UsbDevice): ConnectionToWritableUsbFidoKey {
        return ConnectionToWritableUsbFidoKey.connect(usbManager, device)
    }

    /**
     * Check if a device is one the app has the user's permission to connect to.
     */
    fun hasPermission(device: UsbDevice): Boolean {
        return usbManager.hasPermission(device)
    }

    /**
     * Request permission to connect to a device
     */
    fun requestPermission(device: UsbDevice) {
        usbManager.requestPermission(device, permissionIntent)
    }
}