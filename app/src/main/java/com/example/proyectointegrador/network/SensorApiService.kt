package com.example.proyectointegrador.network

import retrofit2.http.GET

interface SensorApiService {
    @GET("api/sensores/ultimos")
    suspend fun obtenerUltimosFlujos(): List<Double> // o Float si tu API devuelve float
}