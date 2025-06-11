package com.example.proyectointegrador.network


// RetrofitClient.kt
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8080/") // Cambia por tu IP local si usas dispositivo físico
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: SensorApiService = retrofit.create(SensorApiService::class.java)
}
