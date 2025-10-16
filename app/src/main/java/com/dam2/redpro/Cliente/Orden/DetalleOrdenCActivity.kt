package com.dam2.redpro.Cliente.Orden

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dam2.redpro.Adaptadores.AdaptadorProductoOrden
import com.dam2.redpro.Constantes
import com.dam2.redpro.Modelos.ModeloProductoOrden
import com.dam2.redpro.R
import com.dam2.redpro.databinding.ActivityDetalleOrdenCactivityBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

/**
 * Detalle de orden (Cliente) SIN monetización.
 * - Muestra datos de la orden, productos y dirección registrada.
 */
class DetalleOrdenCActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleOrdenCactivityBinding
    private var idOrden: String = ""
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var productosArrayList: ArrayList<ModeloProductoOrden>
    private lateinit var productoOrdenAdapter: AdaptadorProductoOrden

    // Para mostrar quién generó la orden (si se necesita en UI)
    private var ordenadoPor: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleOrdenCactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        idOrden = intent.getStringExtra("idOrden") ?: run {
            finish(); return
        }

        // Botón regresar
        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Cargar datos
        datosOrden()
        direccionCliente()
        productosOrden()
    }

    /** Lista los productos de la orden desde RTDB y muestra la cantidad total. */
    private fun productosOrden() {
        productosArrayList = ArrayList()
        FirebaseDatabase.getInstance()
            .getReference("Ordenes")
            .child(idOrden)
            .child("Productos")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    productosArrayList.clear()
                    for (ds in snapshot.children) {
                        ds.getValue(ModeloProductoOrden::class.java)?.let { productosArrayList.add(it) }
                    }
                    productoOrdenAdapter = AdaptadorProductoOrden(this@DetalleOrdenCActivity, productosArrayList)
                    binding.ordenesRv.adapter = productoOrdenAdapter
                    binding.cantidadOrdenD.text = snapshot.childrenCount.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Silenciar para no romper la UI si falla la lectura
                }
            })
    }

    /** Muestra la dirección del cliente si existe; si no, sugiere registrarla. */
    private fun direccionCliente() {
        FirebaseDatabase.getInstance()
            .getReference("Usuarios")
            .child(firebaseAuth.uid ?: return)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val direccion = "${snapshot.child("direccion").value}"
                    if (direccion.isNotEmpty()) {
                        binding.direccionOrdenD.text = direccion
                    } else {
                        binding.direccionOrdenD.text = "Registre su ubicación para continuar"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Sin acción
                }
            })
    }

    /** Carga y presenta los metadatos de la orden (id, fecha, estado). */
    private fun datosOrden() {
        FirebaseDatabase.getInstance()
            .getReference("Ordenes")
            .child(idOrden)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val id = "${snapshot.child("idOrden").value}"
                    val tiempoOrden = "${snapshot.child("tiempoOrden").value}"
                    val estado = "${snapshot.child("estadoOrden").value}"
                    ordenadoPor = "${snapshot.child("ordenadoPor").value}"

                    val fecha = runCatching { Constantes().obtenerFecha(tiempoOrden.toLong()) }
                        .getOrDefault("")

                    binding.idOrdenD.text = id
                    binding.fechaOrdenD.text = fecha
                    binding.estadoOrdenD.text = estado

                    // Color por estado (sin estados de pago)
                    val colorRes = when (estado) {
                        "Solicitud recibida" -> R.color.azul_marino_oscuro
                        "En preparación"     -> R.color.naranja
                        "Entregado"          -> R.color.verde_oscuro2
                        "Cancelado"          -> R.color.rojo
                        else                 -> R.color.negro
                    }
                    binding.estadoOrdenD.setTextColor(ContextCompat.getColor(this@DetalleOrdenCActivity, colorRes))
                }

                override fun onCancelled(error: DatabaseError) {
                    // Sin acción
                }
            })
    }
}