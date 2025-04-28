package main

import controlled_device.MainActivity as ControlledMainActivity
import master_device.MainActivity as MasterMainActivity

/**
 * Punto de entrada principal para la aplicación
 */
fun main(args: Array<String>) {
    val isMaster = args.isNotEmpty() && args[0] == "--master"
    
    if (isMaster) {
        println("Iniciando como dispositivo maestro")
        val masterApp = MasterMainActivity()
        masterApp.start()
    } else {
        println("Iniciando como dispositivo controlado")
        val controlledApp = ControlledMainActivity()
        controlledApp.start()
    }
}