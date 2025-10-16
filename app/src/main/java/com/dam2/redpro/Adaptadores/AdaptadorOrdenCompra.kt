package com.dam2.redpro.Adaptadores

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dam2.redpro.Cliente.Orden.DetalleOrdenCActivity
import com.dam2.redpro.Constantes
import com.dam2.redpro.Modelos.ModeloOrdenCompra
import com.dam2.redpro.R
import com.dam2.redpro.databinding.ItemOrdenCompraBinding

/**
 * Adapter de órdenes:
 * - Oculta costo y cualquier referencia a pago.
 * - Normaliza "Pago Pendiente" -> "Pendiente" solo para mostrar en UI.
 * - Muestra id, fecha y estado (con color).
 */
class AdaptadorOrdenCompra(
    private val mContext: Context,
    var ordenesArrayList: ArrayList<ModeloOrdenCompra>
) : RecyclerView.Adapter<AdaptadorOrdenCompra.HolderOrdenCompra>() {

    private lateinit var binding: ItemOrdenCompraBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderOrdenCompra {
        binding = ItemOrdenCompraBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return HolderOrdenCompra(binding.root)
    }

    override fun getItemCount(): Int = ordenesArrayList.size

    override fun onBindViewHolder(holder: HolderOrdenCompra, position: Int) {
        val ordenCompra = ordenesArrayList[position]

        val idOrden = ordenCompra.idOrden
        val tiempoOrden = ordenCompra.tiempoOrden
        val estadoOriginal = ordenCompra.estadoOrden

        // 1) Normalizar estado para la UI (eliminar referencia a "Pago")
        val estadoMostrado = when (estadoOriginal) {
            "Pago Pendiente" -> "Pendiente"
            else -> estadoOriginal
        }

        // 2) Bind básico
        holder.idOrdenItem.text = idOrden
        holder.estadoOrdenItem.text = estadoMostrado

        // 3) Color del estado
        val color = when (estadoMostrado) {
            "Solicitud recibida" -> R.color.azul_marino_oscuro
            "Pendiente"          -> R.color.morado      // antes: "Pago Pendiente"
            "En Preparación"     -> R.color.naranja
            "Entregado"          -> R.color.verde_oscuro2
            "Cancelado"          -> R.color.rojo
            else                 -> R.color.negro // fallback por si llegara otro estado
        }
        holder.estadoOrdenItem.setTextColor(ContextCompat.getColor(mContext, color))

        // 4) Fecha legible
        val fecha = Constantes().obtenerFecha(tiempoOrden.toLong())
        holder.fechaOrdenItem.text = fecha

        // 6) Navegar al detalle de la orden
        holder.ibSiguiente.setOnClickListener {
            val intent = Intent(mContext, DetalleOrdenCActivity::class.java)
            intent.putExtra("idOrden", idOrden)
            mContext.startActivity(intent)
        }
    }

    inner class HolderOrdenCompra(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val idOrdenItem = binding.idOrdenItem
        val fechaOrdenItem = binding.fechaOrdenItem
        val estadoOrdenItem = binding.estadoOrdenItem
        val ibSiguiente = binding.ibSiguiente
    }
}