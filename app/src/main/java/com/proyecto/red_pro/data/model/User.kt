package com.proyecto.red_pro.data.model

data class User(
    val uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val rol: String = "",       // "cliente" | "profesional"
    val telefono: String = "",
    val photoPath: String? = ""   // ruta local (interno de la app)
)
