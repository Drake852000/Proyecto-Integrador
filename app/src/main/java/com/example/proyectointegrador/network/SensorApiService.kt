package com.example.proyectointegrador.network

import retrofit2.http.GET

data class CaudalHora(
    val caudal: Float,
    val timestamp: String
)

interface SensorApiService {
    @GET("api/sensores/top10")
    suspend fun obtenerTop10Caudales(): List<CaudalHora>
}