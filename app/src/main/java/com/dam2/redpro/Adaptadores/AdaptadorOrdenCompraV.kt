package com.dam2.redpro.Adaptadores

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dam2.redpro.Constantes
import com.dam2.redpro.Modelos.ModeloOrdenCompra
import com.dam2.redpro.R
import com.dam2.redpro.Vendedor.Orden.DetalleOrdenVActivity
import com.dam2.redpro.databinding.ItemOrdenCompraBinding

/**
 * Adapter de órdenes del Vendedor completamente limpio:
 * - Muestra ID, fecha y estado (color visual informativo).
 */
class AdaptadorOrdenCompraV(
    private val mContext: Context,
    var ordenesArrayList: ArrayList<ModeloOrdenCompra>
) : RecyclerView.Adapter<AdaptadorOrdenCompraV.HolderOrdenCompra>() {

    private lateinit var binding: ItemOrdenCompraBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderOrdenCompra {
        binding = ItemOrdenCompraBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return HolderOrdenCompra(binding.root)
    }

    override fun getItemCount(): Int = ordenesArrayList.size

    override fun onBindViewHolder(holder: HolderOrdenCompra, position: Int) {
        val orden = ordenesArrayList[position]

        // Datos base
        val idOrden = orden.idOrden
        val tiempoOrden = orden.tiempoOrden
        val estadoOriginal = orden.estadoOrden

        // Normalizar estado: eliminar palabra "Pago"
        val estadoMostrado = when (estadoOriginal) {
            "Pago Pendiente" -> "Pendiente"
            else -> estadoOriginal
        }

        // Mostrar datos
        holder.idOrdenItem.text = idOrden
        holder.estadoOrdenItem.text = estadoMostrado

        // Asignar color por estado
        val colorEstado = when (estadoMostrado) {
            "Solicitud recibida" -> R.color.azul_marino_oscuro
            "Pendiente" -> R.color.morado
            "En Preparación" -> R.color.naranja
            "Entregado" -> R.color.verde_oscuro2
            "Cancelado" -> R.color.rojo
            else -> R.color.negro
        }
        holder.estadoOrdenItem.setTextColor(ContextCompat.getColor(mContext, colorEstado))

        // Fecha legible
        holder.fechaOrdenItem.text = Constantes().obtenerFecha(tiempoOrden.toLong())

        // Ver detalle de orden
        holder.ibSiguiente.setOnClickListener {
            val intent = Intent(mContext, DetalleOrdenVActivity::class.java).apply {
                putExtra("idOrden", idOrden)
            }
            mContext.startActivity(intent)
        }
    }

    /** ViewHolder para los elementos de la lista de órdenes */
    inner class HolderOrdenCompra(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val idOrdenItem = binding.idOrdenItem
        val fechaOrdenItem = binding.fechaOrdenItem
        val estadoOrdenItem = binding.estadoOrdenItem
        val ibSiguiente = binding.ibSiguiente
    }
}