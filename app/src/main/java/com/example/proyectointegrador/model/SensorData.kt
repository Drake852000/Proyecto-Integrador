package com.example.proyectointegrador.model

data class SensorData(
    val nivelAgua: Int = 99,
    val flujoAgua: Float = 99f
)

data class SystemState(
    var state: Boolean =  false
)