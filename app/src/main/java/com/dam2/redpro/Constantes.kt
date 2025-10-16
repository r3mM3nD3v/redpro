package com.dam2.redpro

import android.content.Context
import android.text.format.DateFormat
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar
import java.util.Locale

/**
 * Clase utilitaria con funciones de apoyo comunes en la app.
 * Incluye utilidades de tiempo, formato de fecha y manejo de favoritos.
 */
class Constantes {

    /**
     * Devuelve el tiempo actual del sistema en milisegundos.
     * Se usa principalmente para generar IDs únicos basados en tiempo.
     */
    fun obtenerTiempoD(): Long {
        return System.currentTimeMillis()
    }

    /**
     * Convierte un valor de tiempo en milisegundos a una fecha legible (dd/MM/yyyy).
     * @param tiempo Milisegundos del tiempo a convertir.
     */
    fun obtenerFecha(tiempo: Long): String {
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = tiempo
        return DateFormat.format("dd/MM/yyyy", calendar).toString()
    }

    /**
     * Agrega un producto a la lista de favoritos del usuario autenticado en Firebase.
     * @param context Contexto para mostrar los Toasts.
     * @param idProducto ID del producto a agregar.
     */
    fun agregarProductoFav(context: Context, idProducto: String) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val tiempo = obtenerTiempoD() // usa la misma instancia

        // Datos a guardar
        val hashMap = HashMap<String, Any>().apply {
            put("idProducto", idProducto)
            put("idFav", tiempo)
        }

        // Referencia a la ruta del usuario actual en la base de datos
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!)
            .child("Favoritos")
            .child(idProducto)
            .setValue(hashMap)
            .addOnSuccessListener {
                Toast.makeText(context, "Producto agregado a favoritos", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, e.message ?: "Error desconocido", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Elimina un producto de la lista de favoritos del usuario autenticado.
     * @param context Contexto para mostrar los Toasts.
     * @param idProducto ID del producto a eliminar.
     */
    fun eliminarProductoFav(context: Context, idProducto: String) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")

        ref.child(firebaseAuth.uid!!)
            .child("Favoritos")
            .child(idProducto)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Producto eliminado de favoritos", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, e.message ?: "Error desconocido", Toast.LENGTH_SHORT).show()
            }
    }
}
