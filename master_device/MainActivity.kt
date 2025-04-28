package master_device

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import common.Device
import common.NetworkUtils
import java.net.InetAddress

/**
 * This is the main activity for the master device.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var scanButton: Button
    private lateinit var terminateButton: Button
    private lateinit var deviceList: ListView
    private lateinit var volumeSlider: SeekBar
    private lateinit var deviceAdapter: ArrayAdapter<String>
    private val devices: MutableList<Device> = mutableListOf()
    private var selectedDevice: Device? = null
    private val TAG = "MainActivityMaster"

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scanButton = findViewById(R.id.scanButton)
        terminateButton = findViewById(R.id.terminateButton)
        deviceList = findViewById(R.id.deviceList)
        volumeSlider = findViewById(R.id.volumeSlider)

        deviceAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        deviceList.adapter = deviceAdapter

        startListening()
        Log.d(TAG, "startListening called")

        scanButton.setOnClickListener {
            discoverDevices()
        }
        Log.d(TAG, "discoverDevices called")

        terminateButton.setOnClickListener {
            terminateControl()
        }

        volumeSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    sendVolumeControl(progress)
                    Log.d(TAG, "sendVolumeControl called with level: $progress")
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        deviceList.setOnItemClickListener { _, _, position, _ ->
            selectedDevice = devices[position]
            Log.d(TAG, "Device selected: ${selectedDevice?.deviceName}")
        }
    }

    private fun startListening() {
        Log.d(TAG, "startListening")
        NetworkUtils.listenForMessages { message, address ->
            handleMessage(message, address)
        }
    }

    private fun discoverDevices() {
        Log.d(TAG, "discoverDevices")
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
                    Log.d(TAG, "New device discovered: $deviceName - $deviceId")
                    devices.add(device)
                    runOnUiThread {
                        deviceAdapter.add("$deviceName - ${address.hostAddress}")
                        deviceAdapter.notifyDataSetChanged()
                    }
                }else{
                    Log.d(TAG, "Device already discovered: $deviceName - $deviceId")
                }
            }
        }
    }

    private fun sendVolumeControl(level: Int) {
        Log.d(TAG, "sendVolumeControl with level: $level")
        selectedDevice?.let {
            NetworkUtils.sendMessage(
                "${NetworkUtils.VOLUME_CONTROL_PREFIX}:$level",
                InetAddress.getByName(it.ipAddress)
            )
        } ?: run{
            Log.e(TAG, "sendVolumeControl error, no device selected")
            Toast.makeText(this,"No device selected",Toast.LENGTH_SHORT).show()
        }
    }

    private fun terminateControl() {
        Log.d(TAG, "terminateControl")
        selectedDevice?.let {
            NetworkUtils.sendMessage(NetworkUtils.TERMINATE_CONTROL, InetAddress.getByName(it.ipAddress))
        }
    }
}
