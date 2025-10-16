package com.dam2.redpro.Adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.dam2.redpro.Modelos.ModeloImagenSeleccionada
import com.dam2.redpro.R
import com.dam2.redpro.databinding.ItemImagenesSeleccionadasBinding
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

/**
 * Adapter para imágenes seleccionadas de un producto.
 * - Muestra imágenes locales (Uri) o remotas (URL) con Glide.
 * - Permite eliminar imágenes:
 *   - Si son remotas (deInternet = true): borra de RTDB y luego de Storage.
 *   - Si son locales (deInternet = false): solo las remueve de la lista.
 */
class AdaptadorImagenSeleccionada(
    private val context: Context,
    private val imagenesSelecArrayList: ArrayList<ModeloImagenSeleccionada>,
    private val idProducto: String
) : Adapter<AdaptadorImagenSeleccionada.HolderImagenSeleccionada>() {

    private lateinit var binding: ItemImagenesSeleccionadasBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderImagenSeleccionada {
        binding = ItemImagenesSeleccionadasBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderImagenSeleccionada(binding.root)
    }

    override fun getItemCount(): Int = imagenesSelecArrayList.size

    override fun onBindViewHolder(holder: HolderImagenSeleccionada, position: Int) {
        val modelo = imagenesSelecArrayList[position]

        // Carga segura de imagen: remota o local
        if (modelo.deInternet) {
            Glide.with(context)
                .load(modelo.imagenUrl?.ifBlank { null })
                .placeholder(R.drawable.item_imagen)
                .error(R.drawable.item_imagen)
                .into(holder.imagenItem)
        } else {
            Glide.with(context)
                .load(modelo.imagenUri)
                .placeholder(R.drawable.item_imagen)
                .error(R.drawable.item_imagen)
                .into(holder.imagenItem)
        }

        // Eliminar imagen (remota: borra en RTDB y Storage; local: solo lista)
        holder.btn_borrar.setOnClickListener {
            if (modelo.deInternet) {
                eliminarImagenFirebase(modelo, position)
            } else {
                imagenesSelecArrayList.removeAt(position)
                notifyItemRemoved(position)
                // Opcional: notifyItemRangeChanged(position, itemCount - position)
            }
        }
    }

    /**
     * Elimina la referencia de la imagen en RTDB y luego borra el archivo de Storage.
     * Solo aplica para imágenes remotas (deInternet = true).
     */
    private fun eliminarImagenFirebase(
        modelo: ModeloImagenSeleccionada,
        position: Int
    ) {
        val idImagen = modelo.id
        val ref = FirebaseDatabase.getInstance()
            .getReference("Productos")
            .child(idProducto)
            .child("Imagenes")
            .child(idImagen)

        ref.removeValue()
            .addOnSuccessListener {
                eliminarImagenStorage(modelo) {
                    // Actualiza UI tras eliminar en RTDB + Storage
                    val idx = imagenesSelecArrayList.indexOfFirst { it.id == modelo.id }
                    if (idx != -1) {
                        imagenesSelecArrayList.removeAt(idx)
                        notifyItemRemoved(idx)
                    }
                    Toast.makeText(context, "Imagen eliminada", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, e.message ?: "Error al eliminar en la base de datos", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Elimina el archivo de Storage en /Productos/{idImagen}.
     * Llama a onComplete (éxito o fallo) para no bloquear la UI.
     */
    private fun eliminarImagenStorage(
        modelo: ModeloImagenSeleccionada,
        onComplete: () -> Unit
    ) {
        val rutaImagen = "Productos/${modelo.id}"
        val ref = FirebaseStorage.getInstance().getReference(rutaImagen)
        ref.delete()
            .addOnSuccessListener { onComplete() }
            .addOnFailureListener { e ->
                // Si falla, informamos pero igual continuamos con la UI.
                Toast.makeText(context, e.message ?: "No se pudo eliminar la imagen", Toast.LENGTH_SHORT).show()
                onComplete()
            }
    }

    inner class HolderImagenSeleccionada(itemView: View) : ViewHolder(itemView) {
        val imagenItem = binding.imagenItem
        val btn_borrar = binding.borrarItem
    }
}