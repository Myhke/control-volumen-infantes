package master_device

import java.net.InetAddress
import common.Device
import common.NetworkUtils
import common.UIController

/**
 * Esta es la actividad principal para el dispositivo maestro.
 */
class MainActivity {

    private val devices: MutableList<Device> = mutableListOf()
    private var selectedDevice: Device? = null
    private val TAG = "MainActivityMaster"
    private lateinit var uiController: UIController

    fun start() {
        println("$TAG: Iniciando")
        
        uiController = UIController()
        uiController.setupUI { action, data ->
            when(action) {
                "scan" -> discoverDevices()
                "terminate" -> terminateControl()
                "volume" -> sendVolumeControl(data.toInt())
                "select" -> selectDevice(data.toInt())
            }
        }

        startListening()
        println("$TAG: startListening llamado")
    }

    private fun startListening() {
        println("$TAG: startListening")
        NetworkUtils.listenForMessages { message, address ->
            handleMessage(message, address)
        }
    }

    private fun discoverDevices() {
        println("$TAG: discoverDevices")
        NetworkUtils.sendBroadcastMessage(NetworkUtils.DISCOVER_REQUEST)
    }

    private fun handleMessage(message: String, address: InetAddress) {
        if (message.startsWith(NetworkUtils.DISCOVER_RESPONSE_PREFIX)) {
            val parts = message.split(":")
            if (parts.size == 3) {
                val deviceId = parts[1]
                val deviceName = parts[2]
                val device =
                    Device(deviceId, deviceName, address.hostAddress ?: "", true, false, "DISCOVERED")
                if (devices.none { it.deviceId == deviceId }) {
                    println("$TAG: Nuevo dispositivo descubierto: $deviceName - $deviceId")
                    devices.add(device)
                    uiController.updateDeviceList(devices)
                } else {
                    println("$TAG: Dispositivo ya descubierto: $deviceName - $deviceId")
                }
            }
        }
    }

    private fun sendVolumeControl(level: Int) {
        println("$TAG: sendVolumeControl con nivel: $level")
        selectedDevice?.let {
            NetworkUtils.sendMessage(
                "${NetworkUtils.VOLUME_CONTROL_PREFIX}:$level",
                InetAddress.getByName(it.ipAddress)
            )
        } ?: run {
            System.err.println("$TAG: Error en sendVolumeControl, ningún dispositivo seleccionado")
            uiController.showMessage("Ningún dispositivo seleccionado")
        }
    }

    private fun terminateControl() {
        println("$TAG: terminateControl")
        selectedDevice?.let {
            NetworkUtils.sendMessage(NetworkUtils.TERMINATE_CONTROL, InetAddress.getByName(it.ipAddress))
        }
    }
    
    private fun selectDevice(position: Int) {
        if (position >= 0 && position < devices.size) {
            selectedDevice = devices[position]
            println("$TAG: Dispositivo seleccionado: ${selectedDevice?.deviceName}")
        }
    }
}
