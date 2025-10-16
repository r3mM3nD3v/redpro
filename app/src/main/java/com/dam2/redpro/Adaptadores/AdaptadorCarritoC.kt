package com.dam2.redpro.Adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dam2.redpro.Modelos.ModeloProductoCarrito
import com.dam2.redpro.R
import com.dam2.redpro.databinding.ItemCarritoCBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * Adapter del carrito SIN monetización ni calificaciones:
 * - Muestra imagen, nombre y cantidad.
 * - Permite aumentar/disminuir cantidad y eliminar item.
 */
class AdaptadorCarritoC(
    private val mContext: Context,
    var productosArrayList: ArrayList<ModeloProductoCarrito>
) : RecyclerView.Adapter<AdaptadorCarritoC.HolderProductoCarrito>() {

    private lateinit var binding: ItemCarritoCBinding
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderProductoCarrito {
        binding = ItemCarritoCBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return HolderProductoCarrito(binding.root)
    }

    override fun getItemCount(): Int = productosArrayList.size

    override fun onBindViewHolder(holder: HolderProductoCarrito, position: Int) {
        val modelo = productosArrayList[position]

        val nombre = modelo.nombre
        var cantidad = modelo.cantidad

        holder.nombrePCar.text = nombre
        holder.cantidadPCar.text = cantidad.toString()

        // Cargar primera imagen del producto (si existe)
        cargarPrimeraImg(modelo.idProducto, holder)

        // Eliminar del carrito
        holder.btnEliminar.setOnClickListener {
            eliminarProdCarrito(mContext, modelo.idProducto)
        }

        // Aumentar cantidad (solo cantidad)
        holder.btnAumentar.setOnClickListener {
            cantidad++
            holder.cantidadPCar.text = cantidad.toString()
            actualizarCantidad(mContext, modelo.idProducto, cantidad)
        }

        // Disminuir cantidad (mínimo 1)
        holder.btnDisminuir.setOnClickListener {
            if (cantidad > 1) {
                cantidad--
                holder.cantidadPCar.text = cantidad.toString()
                actualizarCantidad(mContext, modelo.idProducto, cantidad)
            }
        }
    }

    /**
     * Actualiza la cantidad del producto en el carrito del usuario autenticado.
     * Se eliminó cualquier campo relacionado con precios/total.
     */
    private fun actualizarCantidad(mContext: Context, idProducto: String, cantidad: Int) {
        val cambios = hashMapOf<String, Any>("cantidad" to cantidad)

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!)
            .child("CarritoCompras")
            .child(idProducto)
            .updateChildren(cambios)
            .addOnSuccessListener {
                Toast.makeText(mContext, "Se actualizó la cantidad", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(mContext, e.message ?: "Error desconocido", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Elimina un producto del carrito.
     * (Fix aplicado: addOnSuccessListener correctamente agregado)
     */
    private fun eliminarProdCarrito(mContext: Context, idProducto: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!)
            .child("CarritoCompras")
            .child(idProducto)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(mContext, "Producto eliminado del carrito", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(mContext, e.message ?: "Error desconocido", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Carga la primera imagen de /Productos/{id}/Imagenes (limitToFirst(1)).
     */
    private fun cargarPrimeraImg(idProducto: String, holder: HolderProductoCarrito) {
        val ref = FirebaseDatabase.getInstance().getReference("Productos")
        ref.child(idProducto)
            .child("Imagenes")
            .limitToFirst(1)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        val imagenUrl = "${ds.child("imagenUrl").value}"
                        try {
                            Glide.with(mContext)
                                .load(imagenUrl)
                                .placeholder(R.drawable.item_img_producto)
                                .into(holder.imagenPCar)
                        } catch (_: Exception) { /* Ignorar fallos de carga visual */ }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Silenciar para evitar crasheos en UI
                }
            })
    }

    inner class HolderProductoCarrito(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagenPCar = binding.imagenPCar
        val nombrePCar = binding.nombrePCar
        val cantidadPCar = binding.cantidadPCar
        val btnAumentar = binding.btnAumentar
        val btnDisminuir = binding.btnDisminuir
        val btnEliminar = binding.btnEliminar
    }
}