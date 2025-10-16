package com.dam2.redpro.Network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Cliente Retrofit para realizar solicitudes HTTP al backend.
 * Asegúrate de actualizar BASE_URL según el entorno (desarrollo / producción).
 */
object RetrofitClient {

    // Dirección base del servidor (actualmente apunta a entorno local)
    private const val BASE_URL = "http://192.168.152.176:3000"

    // Instancia única de Retrofit utilizada en toda la app
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}