package common

/**
 * Controlador de interfaz de usuario multiplataforma
 */
class UIController {
    private lateinit var actionCallback: (String, String) -> Unit
    
    /**
     * Configura la interfaz de usuario
     */
    fun setupUI(callback: (String, String) -> Unit) {
        this.actionCallback = callback
        println("Interfaz de usuario configurada")
        // Aquí implementaríamos la lógica específica de la plataforma
        // para configurar la interfaz de usuario
    }
    
    /**
     * Actualiza la lista de dispositivos en la interfaz
     */
    fun updateDeviceList(devices: List<Device>) {
        println("Lista de dispositivos actualizada: ${devices.size} dispositivos")
        // Aquí implementaríamos la lógica específica de la plataforma
        // para actualizar la lista de dispositivos en la interfaz
    }
    
    /**
     * Muestra un mensaje al usuario
     */
    fun showMessage(message: String) {
        println("Mensaje: $message")
        // Aquí implementaríamos la lógica específica de la plataforma
        // para mostrar mensajes al usuario
    }
}