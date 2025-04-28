package common

/**
 * Data class for a device.
 */
data class Device(
    val deviceId: String,
    val deviceName: String,
    val ipAddress: String,
    val isMaster: Boolean,
    val isControlled: Boolean,
    val status: String, // "CONNECTED", "DISCONNECTED", "PAIRED"
)

