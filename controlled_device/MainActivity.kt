package controlled_device

import android.content.ContentValues.TAG
import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import common.NetworkUtils
import java.net.InetAddress
import android.widget.TextView
import java.util.UUID

// This is the main activity for the controlled device.
class MainActivity : AppCompatActivity() {
    private lateinit var deviceId: String
    private var isPaired: Boolean = false
    private lateinit var audioManager: AudioManager
    private val deviceName = "Controlled Device"
    private val TAG = "MainActivityControlled"
    private lateinit var messageTextView: TextView
    private lateinit var address: InetAddress
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        messageTextView = findViewById(R.id.message)
        deviceId = UUID.randomUUID().toString()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        startListening()
        Log.d(TAG, "startListening called")
    }

    private fun startListening() {
        Log.d(TAG, "startListening")
        NetworkUtils.listenForMessages { message, address ->
            handleMessage(message, address)
        }
    }

    private fun handleMessage(message: String, address: InetAddress) {
        this.address = address
        Log.d(TAG, "handleMessage: $message from $address")
        runOnUiThread {
            messageTextView.text = "Message: $message"
        }
        if (!isPaired) {
            if (message == NetworkUtils.DISCOVER_REQUEST) {
                sendDiscoverResponse(address)
            } else {
                Log.d(TAG, "The device is not paired. Ignore message: $message")
            }
        } else {
            if (message.startsWith(NetworkUtils.VOLUME_CONTROL_PREFIX)) {
                handleVolumeControl(message)
            } else if (message == NetworkUtils.TERMINATE_CONTROL) {
                terminateControl()

            } else {
                Log.d(TAG, "The device is paired. Ignore message: $message")
            }
        }
    }

    private fun sendDiscoverResponse(address: InetAddress) {
        Log.d(TAG, "sendDiscoverResponse")
        val response = "${NetworkUtils.DISCOVER_RESPONSE_PREFIX}:$deviceId:$deviceName"
        NetworkUtils.sendMessage(response, address) // Encrypt the message before sending

    }

    private fun handleVolumeControl(message: String) {
        Log.d(TAG, "handleVolumeControl")
        val level = message.substringAfter("${NetworkUtils.VOLUME_CONTROL_PREFIX}:").toIntOrNull()
        if (level != null) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, level, 0)
            Log.d(TAG, "Volume set to $level")
        } else {
            Log.e(TAG, "Invalid volume level received")
        }
    }

    private fun terminateControl() {
        isPaired = false // Update isPaired before sending the message
        NetworkUtils.sendMessage(NetworkUtils.TERMINATE_CONTROL, address)
        Log.i(TAG, "Control terminated")
    }
}