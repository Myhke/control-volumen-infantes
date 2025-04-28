// NetworkUtils.kt
package common

import android.util.Log
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.MulticastSocket

/**
 * This class provides utility functions for network operations.
 */
class NetworkUtils {
    companion object {
        private const val TAG = "NetworkUtils"
        const val PORT = 9000
        const val DISCOVER_REQUEST = "DISCOVER_REQUEST"
        const val DISCOVER_RESPONSE_PREFIX = "DISCOVER_RESPONSE"
        const val VOLUME_CONTROL_PREFIX = "VOLUME_CONTROL"
        const val TERMINATE_CONTROL = "TERMINATE_CONTROL"
        private const val MULTICAST_ADDRESS = "224.0.0.1"
        private const val SECRET_KEY = "mysecretkey" // Replace with a strong key

        /**
         * Sends a message to a specific address.
         */
        fun sendMessage(message: String, address: InetAddress) {
            Thread {
                try {
                    val encryptedMessage = encryptMessage(message, SECRET_KEY)
                    val socket = DatagramSocket()
                    val data = encryptedMessage.toByteArray()
                    val packet = DatagramPacket(data, data.size, address, PORT)
                    socket.send(packet)
                    socket.close()
                    Log.d(TAG, "Message sent to $address: $message")
                } catch (e: IOException) {
                    Log.e(TAG, "Error sending message to $address", e)
                }
            }.start()
        }

        /**
         * Sends a broadcast message to discover devices.
         */
        fun sendBroadcastMessage(message: String) {
            Thread {
                try {
                    val socket = DatagramSocket()
                    socket.broadcast = true
                    val encryptedMessage = encryptMessage(message, SECRET_KEY)
                    val data = encryptedMessage.toByteArray()
                    val broadcastAddress = InetAddress.getByName("255.255.255.255")
                    val packet = DatagramPacket(data, encryptedMessage.length, broadcastAddress, PORT)
                    socket.send(packet)
                    socket.close()
                    Log.d(TAG, "Broadcast message sent: $message")
                } catch (e: IOException) {
                    Log.e(TAG, "Error sending broadcast message", e)
                }
            }.start()
        }
        
        /**
         * Listens for incoming messages.
         */
        fun listenForMessages(callback: (String, InetAddress) -> Unit) {
            Thread {
                try {
                    val socket = DatagramSocket(PORT)
                    val buffer = ByteArray(1024)
                    while (true) {
                        val packet = DatagramPacket(buffer, buffer.size)
                        socket.receive(packet)
                        val encryptedMessage = String(packet.data, packet.offset, packet.length)
                       val message = decryptMessage(encryptedMessage, SECRET_KEY)
                        val address = packet.address
                        callback(message, address)
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "Error listening for messages", e)
                }
            }.start()
        }

        /**
         * Sends a multicast message to discover devices.
         */
        fun sendMulticastMessage(message: String) {
            Thread {
                try {
                    val multicastAddress = InetAddress.getByName(MULTICAST_ADDRESS)
                    val socket = MulticastSocket()
                    val encryptedMessage = encryptMessage(message, SECRET_KEY)
                    val data = encryptedMessage.toByteArray()
                    val packet = DatagramPacket(data, encryptedMessage.length, multicastAddress, PORT)
                    socket.send(packet)
                    socket.close()
                   Log.d(TAG, "Multicast message sent: $message")
                } catch (e: IOException) {
                    Log.e(TAG, "Error sending multicast message", e)
               }
            }.start()
        }

        /**
         * Encrypts a message using XOR encryption.
         */
        fun encryptMessage(message: String, key: String): String {
            val keyBytes = key.toByteArray()
            val messageBytes = message.toByteArray()
            val encrypted = ByteArray(messageBytes.size)
            for (i in messageBytes.indices) {
                encrypted[i] = messageBytes[i].xor(keyBytes[i % keyBytes.size])
            }
            return String(encrypted, Charsets.ISO_8859_1)
        }

        /**
         * Decrypts a message using XOR decryption.
         */
        fun decryptMessage(message: String, key: String): String {
          return String(encryptMessage(message, key).toByteArray(Charsets.ISO_8859_1))
        }
    }
}