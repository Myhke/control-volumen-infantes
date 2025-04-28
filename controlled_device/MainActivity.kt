package controlled_device

import java.net.InetAddress
import java.util.UUID
import common.NetworkUtils
import common.AudioController

// Esta es la actividad principal para el dispositivo controlado.
class MainActivity {
    private lateinit var deviceId: String
    private var isPaired: Boolean = false
    private lateinit var audioController: AudioController
    private val deviceName = "Controlled Device"
    private val TAG = "MainActivityControlled"
    private lateinit var address: InetAddress
    
    fun start() {
        println("$TAG: Iniciando")
        
        deviceId = UUID.randomUUID().toString()
        audioController = AudioController()
        
        startListening()
        println("$TAG: startListening llamado")
    }

    private fun startListening() {
        println("$TAG: startListening")
        NetworkUtils.listenForMessages { message, address ->
            handleMessage(message, address)
        }
    }

    private fun handleMessage(message: String, address: InetAddress) {
        this.address = address
        println("$TAG: handleMessage: $message from $address")
        
        if (!isPaired) {
            if (message == NetworkUtils.DISCOVER_REQUEST) {
                sendDiscoverResponse(address)
            } else {
                println("$TAG: El dispositivo no está emparejado. Ignorando mensaje: $message")
            }
        } else {
            if (message.startsWith(NetworkUtils.VOLUME_CONTROL_PREFIX)) {
                handleVolumeControl(message)
            } else if (message == NetworkUtils.TERMINATE_CONTROL) {
                terminateControl()
            } else {
                println("$TAG: El dispositivo está emparejado. Ignorando mensaje: $message")
            }
        }
    }

    private fun sendDiscoverResponse(address: InetAddress) {
        println("$TAG: sendDiscoverResponse")
        val response = "${NetworkUtils.DISCOVER_RESPONSE_PREFIX}:$deviceId:$deviceName"
        NetworkUtils.sendMessage(response, address)
    }

    private fun handleVolumeControl(message: String) {
        println("$TAG: handleVolumeControl")
        val level = message.substringAfter("${NetworkUtils.VOLUME_CONTROL_PREFIX}:").toIntOrNull()
        if (level != null) {
            audioController.setVolume(level)
            println("$TAG: Volumen establecido a $level")
        } else {
            System.err.println("$TAG: Nivel de volumen inválido recibido")
        }
    }

    private fun terminateControl() {
        isPaired = false
        NetworkUtils.sendMessage(NetworkUtils.TERMINATE_CONTROL, address)
        println("$TAG: Control terminado")
    }
}