package com.dam2.redpro.Adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dam2.redpro.Modelos.ModeloProductoOrden
import com.dam2.redpro.databinding.ItemProductoOrdenBinding

/**
 * Adapter para listar productos dentro de una orden.
 * - Muestra solo nombre y cantidad.
 */
class AdaptadorProductoOrden(
    private val mContext: Context,
    private var productosArrayList: ArrayList<ModeloProductoOrden>
) : RecyclerView.Adapter<AdaptadorProductoOrden.HolderProductoOrden>() {

    private lateinit var binding: ItemProductoOrdenBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderProductoOrden {
        binding = ItemProductoOrdenBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return HolderProductoOrden(binding.root)
    }

    override fun getItemCount(): Int = productosArrayList.size

    override fun onBindViewHolder(holder: HolderProductoOrden, position: Int) {
        val item = productosArrayList[position]
        holder.itemNombreP.text = item.nombre
        holder.itemCantidadP.text = "Cantidad: ${item.cantidad}"
    }

    inner class HolderProductoOrden(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemNombreP = binding.itemNombreP
        val itemCantidadP = binding.itemCantidadP
    }
}