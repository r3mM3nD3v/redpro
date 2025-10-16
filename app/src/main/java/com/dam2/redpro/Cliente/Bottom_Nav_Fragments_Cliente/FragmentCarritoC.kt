package com.dam2.redpro.Cliente.Bottom_Nav_Fragments_Cliente

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.dam2.redpro.Adaptadores.AdaptadorCarritoC
import com.dam2.redpro.Cliente.Orden.DetalleOrdenCActivity
import com.dam2.redpro.Constantes
import com.dam2.redpro.Modelos.ModeloProductoCarrito
import com.dam2.redpro.databinding.FragmentCarritoCBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FragmentCarritoC : Fragment() {

    private lateinit var binding: FragmentCarritoCBinding

    private lateinit var mContext: Context
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var productosArrayList: ArrayList<ModeloProductoCarrito>
    private lateinit var productoAdaptadorCarritoC: AdaptadorCarritoC

    override fun onAttach(context: Context) {
        this.mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCarritoCBinding.inflate(inflater, container, false)

        binding.btnCrearOrden.setOnClickListener {
            if (!::productosArrayList.isInitialized || productosArrayList.isEmpty()) {
                Toast.makeText(mContext, "No hay productos en el carrito", Toast.LENGTH_SHORT).show()
            } else {
                crearOrden()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        cargarProdCarrito()
    }

    /** Crea una orden y vacía el carrito. */
    private fun crearOrden() {
        val uid = firebaseAuth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            Toast.makeText(mContext, "Sesión no válida", Toast.LENGTH_SHORT).show()
            return
        }

        val tiempo = Constantes().obtenerTiempoD()
        val ref = FirebaseDatabase.getInstance().getReference("Ordenes")
        val keyId = ref.push().key ?: run {
            Toast.makeText(mContext, "No se pudo crear la orden", Toast.LENGTH_SHORT).show()
            return
        }

        val datosOrden = hashMapOf<String, Any>(
            "idOrden" to keyId,
            "tiempoOrden" to "$tiempo",
            "estadoOrden" to "Solicitud recibida",
            "ordenadoPor" to uid
        )

        // Guardar cabecera y luego los productos
        ref.child(keyId).setValue(datosOrden)
            .addOnSuccessListener {
                for (producto in productosArrayList) {
                    val hashMapProd = hashMapOf<String, Any>(
                        "idProducto" to producto.idProducto,
                        "nombre" to producto.nombre,
                        "cantidad" to producto.cantidad
                    )
                    ref.child(keyId).child("Productos").child(producto.idProducto).setValue(hashMapProd)
                }
                eliminarProductosCarrito()
                Toast.makeText(mContext, "Orden realizada con éxito", Toast.LENGTH_SHORT).show()

                // Ir al detalle de la orden
                startActivity(Intent(mContext, DetalleOrdenCActivity::class.java).apply {
                    putExtra("idOrden", keyId)
                })
            }
            .addOnFailureListener { e ->
                Toast.makeText(mContext, e.message ?: "Error al crear la orden", Toast.LENGTH_SHORT).show()
            }
    }

    /** Vacía el carrito del usuario. */
    private fun eliminarProductosCarrito() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance()
            .getReference("Usuarios")
            .child(uid).child("CarritoCompras")

        ref.removeValue()
            .addOnCompleteListener {
                Toast.makeText(mContext, "Los productos se han eliminado del carrito", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(mContext, e.message ?: "No se pudo limpiar el carrito", Toast.LENGTH_SHORT).show()
            }
    }

    /** Carga productos del carrito del usuario y setea el adapter. */
    private fun cargarProdCarrito() {
        productosArrayList = ArrayList()
        val uid = firebaseAuth.uid ?: return

        FirebaseDatabase.getInstance()
            .getReference("Usuarios")
            .child(uid).child("CarritoCompras")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    productosArrayList.clear()
                    for (ds in snapshot.children) {
                        ds.getValue(ModeloProductoCarrito::class.java)?.let { productosArrayList.add(it) }
                    }
                    productoAdaptadorCarritoC = AdaptadorCarritoC(mContext, productosArrayList)
                    binding.carritoRv.adapter = productoAdaptadorCarritoC
                }

                override fun onCancelled(error: DatabaseError) {
                    // No crashear si se cancela la lectura
                }
            })
    }
}
