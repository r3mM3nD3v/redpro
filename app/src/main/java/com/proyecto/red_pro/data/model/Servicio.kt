package com.proyecto.red_pro.data.model

data class Servicio(
    val id: String = "",
    val uidProfesional: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val precio: Double = 0.0,
    val ubicacion: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val photoPaths: List<String>? = null   // rutas locales (0..N)
)
