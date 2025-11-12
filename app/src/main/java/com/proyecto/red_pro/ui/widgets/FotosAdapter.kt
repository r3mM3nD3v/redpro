package com.proyecto.red_pro.ui.widgets

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.proyecto.red_pro.R
import com.proyecto.red_pro.util.LocalImages

class FotosAdapter(
    private val paths: MutableList<String>,
    private val onRemove: (position: Int, removedPath: String) -> Unit
) : RecyclerView.Adapter<FotosAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val ivThumb: ImageView = v.findViewById(R.id.ivThumb)
        val btnEliminar: ImageButton = v.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_foto_thumb, parent, false)
        return VH(view)
    }

    override fun getItemCount() = paths.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val path = paths[position]
        LocalImages.loadPathInto(path, holder.ivThumb)

        holder.btnEliminar.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                val removed = paths.removeAt(pos)
                notifyItemRemoved(pos)
                onRemove(pos, removed)
            }
        }
    }

    fun addAll(newPaths: List<String>) {
        val start = paths.size
        paths.addAll(newPaths)
        notifyItemRangeInserted(start, newPaths.size)
    }

    fun getPaths(): List<String> = paths.toList()
}