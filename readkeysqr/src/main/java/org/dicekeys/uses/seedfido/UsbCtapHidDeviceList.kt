package org.dicekeys.uses.seedfido

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log

class UsbCtapHidDeviceList(
    private val usbManager: UsbManager,
    private val permissionIntent: android.app.PendingIntent
) {

    val devices: Map<String, UsbDevice> get() =
        usbManager.deviceList.filter { (name, device) -> run{
            val supportsLoadKey: Boolean =
                    // SoloKeys
                    (device.productId == 0x8acf && device.vendorId == 0x10c4) ||
                            (device.productId == 0xa2ca && device.vendorId == 0x483)

            Log.d("Device", "{name: \"$name\", deviceName:${device.deviceName}, " +
                    "productName: ${device.productName}, " +
                    "manufacturerName: \"${device.manufacturerName}, " +
                    "vendorId: ${device.vendorId.toString(16)}, " +
                    "productId: ${device.productId.toString(16)}, " +
                    "supportsLoadKey: ${(if (supportsLoadKey) "true" else "false")}"
            )

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

    fun connect(deviceName: String): UsbCtapHidConnection {
        val device = devices[deviceName]
        if (device == null) {
            throw java.lang.IllegalArgumentException("No such device")
        } else {
            return connect(device)
        }
    }

    fun connect(device: UsbDevice): UsbCtapHidConnection {
        return UsbCtapHidConnection.connect(usbManager, device)
    }

    fun hasPermission(device: UsbDevice): Boolean {
        return usbManager.hasPermission(device)
    }

    fun requestPermission(device: UsbDevice) {
        usbManager.requestPermission(device, permissionIntent)
    }
}