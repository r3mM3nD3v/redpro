package com.dam2.redpro.Vendedor.Orden

import android.app.Dialog
import android.os.Bundle
import android.view.Menu
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.dam2.redpro.Adaptadores.AdaptadorProductoOrden
import com.dam2.redpro.Constantes
import com.dam2.redpro.Modelos.ModeloProductoOrden
import com.dam2.redpro.R
import com.dam2.redpro.databinding.ActivityDetalleOrdenVactivityBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

/**
 * Detalle de la Orden (Vendedor)
 * - Muestra info básica de la orden y productos.
 * - Permite marcar como "Entregado" o "Cancelado".
 * - Muestra datos del cliente.
 */
class DetalleOrdenVActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleOrdenVactivityBinding

    private var idOrden = ""
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var productosArrayList: ArrayList<ModeloProductoOrden>
    private lateinit var productoOrdenAdaptador: AdaptadorProductoOrden

    private var ordenadoPor = "" // uid del cliente

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleOrdenVactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        idOrden = intent.getStringExtra("idOrden") ?: ""

        datosOrden()
        productosOrden()

        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.verInfoCliente.setOnClickListener {
            infoCliente(ordenadoPor)
        }

        binding.IbActualizarOrden.setOnClickListener {
            estadoOrdenMenu()
        }
    }

    /** Muestra un diálogo con la información del cliente. */
    private fun infoCliente(uidCliente: String) {
        if (uidCliente.isBlank()) {
            Toast.makeText(this, "Cliente no disponible", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_info_cliente)

        val ivPerfil: ImageView = dialog.findViewById(R.id.ivPerfil)
        val tvNombresC: TextView = dialog.findViewById(R.id.tvNombresC)
        val tvDniC: TextView = dialog.findViewById(R.id.tvDniC)
        val tvDireccionC: TextView = dialog.findViewById(R.id.tvDireccionC)
        val ibCerrar: ImageButton = dialog.findViewById(R.id.ibCerrar)

        FirebaseDatabase.getInstance()
            .getReference("Usuarios")
            .child(uidCliente)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val imagen = "${snapshot.child("imagen").value}"
                    val nombres = "${snapshot.child("nombres").value}"
                    val dni = "${snapshot.child("dni").value}"
                    val direccion = "${snapshot.child("direccion").value}"

                    tvNombresC.text = nombres
                    tvDniC.text = dni
                    tvDireccionC.text = direccion

                    runCatching {
                        Glide.with(this@DetalleOrdenVActivity)
                            .load(if (imagen.isBlank()) null else imagen)
                            .placeholder(R.drawable.img_perfil)
                            .error(R.drawable.img_perfil)
                            .into(ivPerfil)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // No romper la UI si falla la lectura
                }
            })

        ibCerrar.setOnClickListener { dialog.dismiss() }
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    /** Menú para actualizar el estado de la orden. */
    private fun estadoOrdenMenu() {
        val popupMenu = PopupMenu(this, binding.IbActualizarOrden)
        popupMenu.menu.add(Menu.NONE, 0, 0, "Orden entregada")
        popupMenu.menu.add(Menu.NONE, 1, 1, "Orden cancelada")
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                0 -> actualizarEstado("Entregado")
                1 -> actualizarEstado("Cancelado")
            }
            true
        }
        popupMenu.show()
    }

    private fun actualizarEstado(estado: String) {
        FirebaseDatabase.getInstance()
            .getReference("Ordenes")
            .child(idOrden)
            .updateChildren(hashMapOf<String, Any>("estadoOrden" to estado))
            .addOnSuccessListener {
                Toast.makeText(this, "El estado de la orden ha pasado a: $estado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Ha ocurrido un error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /** Carga y lista los productos de la orden (nombre y cantidad). */
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
                    productoOrdenAdaptador = AdaptadorProductoOrden(this@DetalleOrdenVActivity, productosArrayList)
                    binding.ordenesRv.adapter = productoOrdenAdaptador
                    binding.cantidadOrdenD.text = snapshot.childrenCount.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    // No romper la UI si falla la lectura
                }
            })
    }

    /** Carga datos generales de la orden. */
    private fun datosOrden() {
        FirebaseDatabase.getInstance()
            .getReference("Ordenes")
            .child(idOrden)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val idOrdenDb = "${snapshot.child("idOrden").value}"
                    val tiempoOrden = "${snapshot.child("tiempoOrden").value}"
                    val estadoOrden = "${snapshot.child("estadoOrden").value}"
                    ordenadoPor = "${snapshot.child("ordenadoPor").value}"

                    val fecha = runCatching { Constantes().obtenerFecha(tiempoOrden.toLong()) }.getOrDefault("")

                    binding.idOrdenD.text = idOrdenDb
                    binding.fechaOrdenD.text = fecha
                    binding.estadoOrdenD.text = estadoOrden

                    val color = when (estadoOrden) {
                        "Solicitud recibida" -> R.color.azul_marino_oscuro
                        "En preparación"     -> R.color.naranja
                        "Entregado"          -> R.color.verde_oscuro2
                        "Cancelado"          -> R.color.rojo
                        else                 -> R.color.black
                    }
                    binding.estadoOrdenD.setTextColor(ContextCompat.getColor(this@DetalleOrdenVActivity, color))

                    direccionCliente(ordenadoPor)
                }

                override fun onCancelled(error: DatabaseError) {
                    // No romper la UI si falla la lectura
                }
            })
    }

    /** Pinta la dirección del cliente si existe. */
    private fun direccionCliente(uidCliente: String) {
        if (uidCliente.isBlank()) return
        FirebaseDatabase.getInstance()
            .getReference("Usuarios")
            .child(uidCliente)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val direccion = "${snapshot.child("direccion").value}"
                    binding.direccionOrdenD.text =
                        if (direccion.isNotEmpty()) direccion
                        else "El cliente no registró su dirección."
                }

                override fun onCancelled(error: DatabaseError) {
                    // No romper la UI si falla la lectura
                }
            })
    }
}