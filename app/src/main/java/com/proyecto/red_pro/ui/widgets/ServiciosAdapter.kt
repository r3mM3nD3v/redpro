package com.proyecto.red_pro.ui.widgets

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.proyecto.red_pro.R
import com.proyecto.red_pro.data.model.Servicio
import com.proyecto.red_pro.util.LocalImages

class ServiciosAdapter(
    private val onClick: (Servicio) -> Unit
) : ListAdapter<Servicio, ServiciosAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Servicio>() {
            override fun areItemsTheSame(oldItem: Servicio, newItem: Servicio) =
                oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Servicio, newItem: Servicio) =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_servicio, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.bind(item, onClick)

        val firstPath = item.photoPaths?.firstOrNull()
        if (firstPath.isNullOrEmpty()) {
            holder.ivImagen.setImageResource(R.drawable.placeholder)
        } else {
            LocalImages.loadPathInto(firstPath, holder.ivImagen)
        }
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val ivImagen: ImageView = view.findViewById(R.id.ivImagen)
        private val tvTitulo: TextView = view.findViewById(R.id.tvTitulo)
        private val tvCategoria: TextView = view.findViewById(R.id.tvCategoria)
        private val tvPrecio: TextView = view.findViewById(R.id.tvPrecio)
        private val tvUbicacion: TextView = view.findViewById(R.id.tvUbicacion)

        fun bind(s: Servicio, onClick: (Servicio) -> Unit) {
            tvTitulo.text = s.titulo
            tvCategoria.text = s.categoria
            tvPrecio.text = "USD ${"%.2f".format(s.precio)}"
            tvUbicacion.text = s.ubicacion
            itemView.setOnClickListener { onClick(s) }
        }
    }
}