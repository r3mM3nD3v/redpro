package com.dam2.redpro.Adaptadores

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dam2.redpro.Constantes
import com.dam2.redpro.DetalleProducto.DetalleProductoActivity
import com.dam2.redpro.Filtro.FiltroProducto
import com.dam2.redpro.Modelos.ModeloProducto
import com.dam2.redpro.R
import com.dam2.redpro.databinding.ItemProductoCBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

/**
 * Adaptador de productos para el cliente:
 * - Muestra imagen, nombre y opción de marcar como favorito.
 */
class AdaptadorProductoC(
    private val mContext: Context,
    var productosArrayList: ArrayList<ModeloProducto>
) : RecyclerView.Adapter<AdaptadorProductoC.HolderProducto>(), Filterable {

    private lateinit var binding: ItemProductoCBinding
    private var filtroLista: ArrayList<ModeloProducto> = productosArrayList
    private var filtro: FiltroProducto? = null
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderProducto {
        binding = ItemProductoCBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return HolderProducto(binding.root)
    }

    override fun getItemCount(): Int = productosArrayList.size

    override fun onBindViewHolder(holder: HolderProducto, position: Int) {
        val modelo = productosArrayList[position]
        val nombre = modelo.nombre

        holder.item_nombre_p.text = nombre

        // Cargar primera imagen
        cargarPrimeraImg(modelo.id, holder)

        // Ir a detalle del producto
        holder.itemView.setOnClickListener {
            val intent = Intent(mContext, DetalleProductoActivity::class.java)
            intent.putExtra("idProducto", modelo.id)
            mContext.startActivity(intent)
        }

        // El botón de "Agregar al carrito" ya no tiene uso
        holder.agregar_carrito.visibility = View.GONE
    }

    /** Carga la primera imagen del producto desde Firebase */
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
                            Glide.with(mContext)
                                .load(imagenUrl.ifBlank { null })
                                .placeholder(R.drawable.item_img_producto)
                                .error(R.drawable.item_img_producto)
                                .into(holder.imagenP)
                        } catch (_: Exception) { }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Silenciar errores de lectura
                }
            })
    }

    /** ViewHolder */
    inner class HolderProducto(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagenP = binding.imagenP
        val item_nombre_p = binding.itemNombreP
        val agregar_carrito = binding.itemAgregarCarritoP
    }

    /** Filtro por nombre o categoría */
    override fun getFilter(): Filter {
        if (filtro == null) {
            filtro = FiltroProducto(this, filtroLista)
        }
        return filtro as FiltroProducto
    }
}
