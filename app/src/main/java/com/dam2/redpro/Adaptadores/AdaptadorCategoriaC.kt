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

class AdaptadorCategoriaC : RecyclerView.Adapter<AdaptadorCategoriaC.HolderCategoriaC> {

    private lateinit var binding : ItemCategoriaCBinding

    private var mContext : Context
    private var categoriaArrayList : ArrayList<ModeloCategoria>

    constructor(mContext: Context, categoriaArrayList: ArrayList<ModeloCategoria>) : super() {
        this.mContext = mContext
        this.categoriaArrayList = categoriaArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategoriaC {
        binding = ItemCategoriaCBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return HolderCategoriaC(binding.root)
    }

    override fun getItemCount(): Int {
        return categoriaArrayList.size
    }

    override fun onBindViewHolder(holder: HolderCategoriaC, position: Int) {
        val modelo = categoriaArrayList[position]

        val categoria = modelo.categoria
        val imagen = modelo.imagenUrl

        holder.item_nombre_c_c.text = categoria

        Glide.with(mContext)
            .load(imagen)
            .placeholder(R.drawable.categorias)
            .into(holder.item_img_cat)

        //Evento para ver productos de una categoria
        holder.item_ver_productos.setOnClickListener {
            val intent = Intent(mContext, ProductosCatCActivity::class.java)
            intent.putExtra("nombreCat", categoria)
            Toast.makeText(mContext, "Categoria seleccionada ${categoria}", Toast.LENGTH_SHORT).show()
            mContext.startActivity(intent)
        }
    }

    inner class HolderCategoriaC (itemView : View) : RecyclerView.ViewHolder(itemView){
        var item_nombre_c_c = binding.itemNombreCC
        var item_img_cat = binding.imagenCateg
        var item_ver_productos = binding.itemVerProductos
    }
}