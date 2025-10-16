package com.dam2.redpro.Adaptadores

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dam2.redpro.Modelos.ModeloProducto
import com.dam2.redpro.R
import com.dam2.redpro.Vendedor.MainActivityVendedor
import com.dam2.redpro.Vendedor.Productos.AgregarProductoActivity
import com.dam2.redpro.databinding.ItemProductoBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.*

/**
 * Adapter de productos (Vendedor).
 * - Lista productos con su primera imagen, nombre y categoría.
 * - Permite editar y eliminar producto.
 */
class AdaptadorProducto(
    private val mContex: Context,
    private var productosArrayList: ArrayList<ModeloProducto>
) : RecyclerView.Adapter<AdaptadorProducto.HolderProducto>() {

    private lateinit var binding: ItemProductoBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderProducto {
        binding = ItemProductoBinding.inflate(LayoutInflater.from(mContex), parent, false)
        return HolderProducto(binding.root)
    }

    override fun getItemCount(): Int = productosArrayList.size

    override fun onBindViewHolder(holder: HolderProducto, position: Int) {
        val modeloProducto = productosArrayList[position]
        val nombre = modeloProducto.nombre
        val categoria = modeloProducto.categoria

        // Bind básico
        holder.item_nombre_p.text = nombre
        holder.item_categoria_p.text = categoria

        // Cargar primera imagen del producto
        cargarPrimeraImg(modeloProducto.id, holder)

        // Editar producto
        holder.Ib_editar.setOnClickListener {
            val intent = Intent(mContex, AgregarProductoActivity::class.java).apply {
                putExtra("Edicion", true)
                putExtra("idProducto", modeloProducto.id)
            }
            mContex.startActivity(intent)
        }

        // Eliminar producto (confirmación)
        holder.Ib_eliminar.setOnClickListener {
            MaterialAlertDialogBuilder(mContex)
                .setTitle("Eliminar producto")
                .setMessage("¿Estás seguro(a) de eliminar el producto?")
                .setPositiveButton("Eliminar") { dialog, _ ->
                    eliminarProductoBD(modeloProducto.id)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    /** Elimina el producto en RTDB. */
    private fun eliminarProductoBD(idProducto: String) {
        FirebaseDatabase.getInstance()
            .getReference("Productos")
            .child(idProducto)
            .removeValue()
            .addOnSuccessListener {
                mContex.startActivity(Intent(mContex, MainActivityVendedor::class.java))
                Toast.makeText(mContex, "Producto eliminado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(mContex, e.message ?: "Error desconocido", Toast.LENGTH_SHORT).show()
            }
    }

    /** Carga la primera imagen desde /Productos/{id}/Imagenes (limitToFirst(1)). */
    private fun cargarPrimeraImg(idProducto: String, holder: HolderProducto) {
        FirebaseDatabase.getInstance()
            .getReference("Productos")
            .child(idProducto)
            .child("Imagenes")
            .limitToFirst(1)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        val imagenUrl = "${ds.child("imagenUrl").value}"
                        try {
                            Glide.with(mContex)
                                .load(if (imagenUrl.isBlank()) null else imagenUrl)
                                .placeholder(R.drawable.item_img_producto)
                                .error(R.drawable.item_img_producto)
                                .into(holder.imagenP)
                        } catch (_: Exception) { /* Silenciar fallos de UI */ }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Evitar crash en callbacks cancelados
                }
            })
    }

    inner class HolderProducto(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagenP = binding.imagenP
        val item_nombre_p = binding.itemNombreP
        val item_categoria_p = binding.itemCategoriaP
        val Ib_editar = binding.IbEditar
        val Ib_eliminar = binding.IbEliminar
    }
}