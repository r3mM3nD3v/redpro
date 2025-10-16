package com.dam2.redpro.Adaptadores

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dam2.redpro.Modelos.ModeloCategoria
import com.dam2.redpro.R
import com.dam2.redpro.Vendedor.Productos.ProductosCatVActivity
import com.dam2.redpro.databinding.ItemCategoriaVBinding
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

/**
 * Adapter de categorías para Vendedor.
 * - Muestra nombre e imagen.
 * - Permite ver productos por categoría, renombrar y eliminar (incluida la imagen en Storage).
 */
class AdaptadorCategoriaV(
    private val mContext: Context,
    private val categoriaArrayList: ArrayList<ModeloCategoria>
) : RecyclerView.Adapter<AdaptadorCategoriaV.HolderCategoriaV>() {

    private lateinit var binding: ItemCategoriaVBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategoriaV {
        binding = ItemCategoriaVBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return HolderCategoriaV(binding.root)
    }

    override fun getItemCount(): Int = categoriaArrayList.size

    override fun onBindViewHolder(holder: HolderCategoriaV, position: Int) {
        val modelo = categoriaArrayList[position]
        val id = modelo.id
        val nombreCategoria = modelo.categoria
        val imagenUrl = modelo.imagenUrl

        // Nombre
        holder.itemNombre.text = nombreCategoria

        // Imagen con placeholder y fallback
        Glide.with(mContext)
            .load(imagenUrl.ifBlank { null })
            .placeholder(R.drawable.categorias)
            .error(R.drawable.categorias)
            .into(holder.itemImagen)

        // Renombrar categoría
        holder.btnActualizarNombre.setOnClickListener { mostrarDialogRenombrar(id) }

        // Eliminar categoría (con confirmación)
        holder.btnEliminar.setOnClickListener {
            val builder = AlertDialog.Builder(mContext)
                .setTitle("Eliminar categoría")
                .setMessage("¿Estás seguro(a) de eliminar esta categoría?")
                .setPositiveButton("Confirmar") { dialog, _ ->
                    eliminarCategoria(modelo)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            builder.show()
        }

        // Ver productos de la categoría
        holder.btnVerProductos.setOnClickListener {
            val intent = Intent(mContext, ProductosCatVActivity::class.java).apply {
                putExtra("nombreCat", nombreCategoria)
            }
            Toast.makeText(mContext, "Categoría seleccionada: $nombreCategoria", Toast.LENGTH_SHORT).show()
            mContext.startActivity(intent)
        }
    }

    /** Elimina categoría en RTDB y luego su imagen en Storage (si existe). */
    private fun eliminarCategoria(modelo: ModeloCategoria) {
        val idCat = modelo.id
        val ref = FirebaseDatabase.getInstance().getReference("Categorias")
        ref.child(idCat).removeValue()
            .addOnSuccessListener {
                Toast.makeText(mContext, "Categoría eliminada", Toast.LENGTH_SHORT).show()
                eliminarImagenCategoria(idCat)
            }
            .addOnFailureListener { e ->
                Toast.makeText(mContext, "No se eliminó la categoría: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /** Elimina la imagen asociada en Firebase Storage: /Categorias/{idCat} */
    private fun eliminarImagenCategoria(idCat: String) {
        val ruta = "Categorias/$idCat"
        val storageRef = FirebaseStorage.getInstance().getReference(ruta)
        storageRef.delete()
            .addOnSuccessListener {
                Toast.makeText(mContext, "Imagen de la categoría eliminada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Puede fallar si no existe archivo; lo manejamos de forma no bloqueante.
                Toast.makeText(mContext, e.message ?: "No se pudo eliminar la imagen", Toast.LENGTH_SHORT).show()
            }
    }

    /** Muestra diálogo para renombrar categoría y persiste el cambio en RTDB. */
    private fun mostrarDialogRenombrar(idCat: String) {
        val dialog = Dialog(mContext).apply { setContentView(R.layout.dialog_act_nom_cat) }

        val etNuevoNomCat: EditText = dialog.findViewById(R.id.etNuevoNomCat)
        val btnActualizar: MaterialButton = dialog.findViewById(R.id.btnActualizarNomCat)
        val btnCerrar: ImageButton = dialog.findViewById(R.id.ibCerrar)

        btnActualizar.setOnClickListener {
            val nuevoNombre = etNuevoNomCat.text.toString().trim()
            if (nuevoNombre.isNotEmpty()) {
                actualizarNombreCategoria(idCat, nuevoNombre)
                dialog.dismiss()
            } else {
                Toast.makeText(mContext, "Ingrese un nombre", Toast.LENGTH_SHORT).show()
            }
        }

        btnCerrar.setOnClickListener { dialog.dismiss() }

        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    /** Actualiza el nombre de la categoría en RTDB. */
    private fun actualizarNombreCategoria(idCat: String, nuevoNombre: String) {
        val cambios = hashMapOf<String, Any>("categoria" to nuevoNombre)
        FirebaseDatabase.getInstance().getReference("Categorias")
            .child(idCat)
            .updateChildren(cambios)
            .addOnSuccessListener { Toast.makeText(mContext, "Nombre actualizado", Toast.LENGTH_SHORT).show() }
            .addOnFailureListener { e ->
                Toast.makeText(mContext, "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    inner class HolderCategoriaV(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemNombre = binding.itemNombreCV
        val btnActualizarNombre = binding.itemActualizarCat
        val btnEliminar = binding.itemEliminarC
        val itemImagen = binding.imagenCategCV
        val btnVerProductos = binding.itemVerProductos
    }
}