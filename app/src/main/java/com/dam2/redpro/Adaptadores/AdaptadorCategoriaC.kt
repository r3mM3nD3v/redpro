package com.dam2.redpro.Adaptadores

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dam2.redpro.Cliente.ProductosC.ProductosCatCActivity
import com.dam2.redpro.Modelos.ModeloCategoria
import com.dam2.redpro.R
import com.dam2.redpro.databinding.ItemCategoriaCBinding

/**
 * Adapter de categorías (cliente).
 * - Muestra nombre e imagen de una categoría.
 * - Navega a la lista de productos de esa categoría.
 */
class AdaptadorCategoriaC(
    private val mContext: Context,
    private var categoriaArrayList: ArrayList<ModeloCategoria>
) : RecyclerView.Adapter<AdaptadorCategoriaC.HolderCategoriaC>() {

    private lateinit var binding: ItemCategoriaCBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategoriaC {
        binding = ItemCategoriaCBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return HolderCategoriaC(binding.root)
    }

    override fun getItemCount(): Int = categoriaArrayList.size

    override fun onBindViewHolder(holder: HolderCategoriaC, position: Int) {
        val modelo = categoriaArrayList[position]
        val categoria = modelo.categoria
        val imagenUrl = modelo.imagenUrl

        // Asigna nombre
        holder.itemNombre.text = categoria

        // Carga de imagen con placeholder
        Glide.with(mContext)
            .load(imagenUrl.ifBlank { null }) // evita cargar string vacío
            .placeholder(R.drawable.categorias)
            .error(R.drawable.categorias)     // fallback si falla carga
            .into(holder.itemImagen)

        // Click: abrir pantalla de productos por categoría
        holder.itemVerProductos.setOnClickListener {
            val intent = Intent(mContext, ProductosCatCActivity::class.java).apply {
                putExtra("nombreCat", categoria)
            }
            Toast.makeText(mContext, "Categoría seleccionada: $categoria", Toast.LENGTH_SHORT).show()
            mContext.startActivity(intent)
        }
    }

    inner class HolderCategoriaC(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemNombre = binding.itemNombreCC
        val itemImagen = binding.imagenCateg
        val itemVerProductos = binding.itemVerProductos
    }
}