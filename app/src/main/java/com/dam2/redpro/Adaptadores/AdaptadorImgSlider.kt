package com.dam2.redpro.Adaptadores

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dam2.redpro.Modelos.ModeloImgSlider
import com.dam2.redpro.R
import com.dam2.redpro.databinding.ItemImagenSliderBinding
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView

class AdaptadorImgSlider(
    private val context: Context,
    private val imagenArrayList: ArrayList<ModeloImgSlider>
) : RecyclerView.Adapter<AdaptadorImgSlider.HolderImagenSlider>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderImagenSlider {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemImagenSliderBinding.inflate(inflater, parent, false)
        return HolderImagenSlider(binding)
    }

    override fun getItemCount(): Int = imagenArrayList.size

    override fun onBindViewHolder(holder: HolderImagenSlider, position: Int) {
        val item = imagenArrayList[position]
        holder.bind(item, position, imagenArrayList.size)
    }

    inner class HolderImagenSlider(
        private val binding: ItemImagenSliderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        val imagenSIV: ShapeableImageView = binding.imagenSIV
        val imagenContadorTv: TextView = binding.imagenContadorTv

        fun bind(item: ModeloImgSlider, position: Int, total: Int) {
            imagenContadorTv.text = "${position + 1}/$total"

            // Carga de imagen
            try {
                Glide.with(itemView) // evita fugas con context de Activity
                    .load(item.imagenUrl.ifBlank { null })
                    .placeholder(R.drawable.item_img_producto)
                    .error(R.drawable.item_img_producto)
                    .into(imagenSIV)
            } catch (_: Exception) {}

            // Tap para zoom
            itemView.setOnClickListener {
                // Usa bindingAdapterPosition por seguridad
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    zoomImg(imagenArrayList[pos].imagenUrl)
                }
            }
        }
    }

    /** Muestra un diálogo con zoom de la imagen usando PhotoView. */
    private fun zoomImg(imagen: String) {
        val dialog = Dialog(context).apply { setContentView(R.layout.zoom_imagen) }

        val pv: PhotoView = dialog.findViewById(R.id.zoomImg)
        val btnCerrar: MaterialButton = dialog.findViewById(R.id.cerrarZoom)

        try {
            Glide.with(context)
                .load(imagen.ifBlank { null })
                .placeholder(R.drawable.item_img_producto)
                .error(R.drawable.item_img_producto)
                .into(pv)
        } catch (_: Exception) {}

        btnCerrar.setOnClickListener { dialog.dismiss() }
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }
}