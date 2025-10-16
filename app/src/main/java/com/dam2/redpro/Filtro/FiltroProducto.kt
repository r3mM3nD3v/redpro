package com.dam2.redpro.Filtro

import android.widget.Filter
import com.dam2.redpro.Adaptadores.AdaptadorProductoC
import com.dam2.redpro.Modelos.ModeloProducto
import java.util.Locale

/**
 * Filtro para búsqueda de productos por nombre.
 */
class FiltroProducto(
    private val adaptador: AdaptadorProductoC,
    private val listaOriginal: ArrayList<ModeloProducto>
) : Filter() {

    override fun performFiltering(query: CharSequence?): FilterResults {
        val resultados = FilterResults()
        val textoFiltro = query?.toString()?.trim()?.uppercase(Locale.getDefault()).orEmpty()

        val listaFiltrada = if (textoFiltro.isNotEmpty()) {
            listaOriginal.filter { producto ->
                producto.nombre.uppercase(Locale.getDefault()).contains(textoFiltro)
            }
        } else {
            listaOriginal
        }

        resultados.values = ArrayList(listaFiltrada)
        resultados.count = listaFiltrada.size
        return resultados
    }

    @Suppress("UNCHECKED_CAST")
    override fun publishResults(query: CharSequence?, resultados: FilterResults) {
        val listaActualizada = resultados.values as? ArrayList<ModeloProducto> ?: arrayListOf()
        adaptador.productosArrayList = listaActualizada
        adaptador.notifyDataSetChanged()
    }
}