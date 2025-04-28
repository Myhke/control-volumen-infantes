package common

/**
 * Controlador de audio multiplataforma
 */
class AudioController {
    private var currentVolume: Int = 50 // Valor predeterminado
    
    /**
     * Establece el volumen del sistema
     */
    fun setVolume(level: Int) {
        if (level in 0..100) {
            currentVolume = level
            // Aquí implementaríamos la lógica específica de la plataforma
            // para controlar el volumen del sistema
            println("Volumen establecido a $level")
        } else {
            System.err.println("Nivel de volumen inválido: $level")
        }
    }
    
    /**
     * Obtiene el volumen actual
     */
    fun getVolume(): Int {
        return currentVolume
    }
}