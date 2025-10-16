package com.dam2.redpro.Vendedor.Nav_Fragments_Vendedor

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.dam2.redpro.Adaptadores.AdaptadorCategoriaV
import com.dam2.redpro.Modelos.ModeloCategoria
import com.dam2.redpro.R
import com.dam2.redpro.databinding.FragmentCategoriasVBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

/**
 * Gestión de categorías (Vendedor)
 * - Agregar categoría (nombre + imagen)
 * - Listar / editar / eliminar (a través del adaptador)
 */
class FragmentCategoriasV : Fragment() {

    private lateinit var binding: FragmentCategoriasVBinding
    private lateinit var mContext: Context
    private lateinit var progressDialog: ProgressDialog
    private var imageUri: Uri? = null

    private lateinit var categoriasArrayList: ArrayList<ModeloCategoria>
    private lateinit var adaptadorCategoriaV: AdaptadorCategoriaV

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCategoriasVBinding.inflate(inflater, container, false)

        progressDialog = ProgressDialog(context).apply {
            setTitle("Espere por favor")
            setCanceledOnTouchOutside(false)
        }

        binding.imgCategorias.setOnClickListener { seleccionarImg() }
        binding.btnAgregarCat.setOnClickListener { validarInfo() }

        listarCategorias()
        return binding.root
    }

    /** Lista categorías ordenadas por nombre. */
    private fun listarCategorias() {
        categoriasArrayList = ArrayList()
        FirebaseDatabase.getInstance()
            .getReference("Categorias")
            .orderByChild("categoria")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    categoriasArrayList.clear()
                    for (ds in snapshot.children) {
                        ds.getValue(ModeloCategoria::class.java)?.let { categoriasArrayList.add(it) }
                    }
                    adaptadorCategoriaV = AdaptadorCategoriaV(mContext, categoriasArrayList)
                    binding.rvCategorias.adapter = adaptadorCategoriaV
                }

                override fun onCancelled(error: DatabaseError) {
                    // Evitar crash si falla la lectura
                }
            })
    }

    /** Lanza el selector de imagen. */
    private fun seleccionarImg() {
        ImagePicker.with(this) // usar el Fragment como owner
            .crop()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .createIntent { intent -> resultadoImg.launch(intent) }
    }

    private val resultadoImg =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
            if (resultado.resultCode == Activity.RESULT_OK) {
                imageUri = resultado.data?.data
                binding.imgCategorias.setImageURI(imageUri)
            } else {
                Toast.makeText(mContext, "Acción cancelada", Toast.LENGTH_SHORT).show()
            }
        }

    private var categoria = ""

    /** Valida que exista nombre e imagen antes de guardar. */
    private fun validarInfo() {
        categoria = binding.etCategoria.text.toString().trim()
        when {
            categoria.isEmpty() -> Toast.makeText(context, "Ingrese una categoría", Toast.LENGTH_SHORT).show()
            imageUri == null    -> Toast.makeText(context, "Seleccione una imagen", Toast.LENGTH_SHORT).show()
            else                -> agregarCatBD()
        }
    }

    /** Crea el nodo de categoría y luego sube la imagen a Storage. */
    private fun agregarCatBD() {
        progressDialog.setMessage("Agregando categoría")
        progressDialog.show()

        val ref = FirebaseDatabase.getInstance().getReference("Categorias")
        val keyId = ref.push().key ?: run {
            progressDialog.dismiss()
            Toast.makeText(context, "No se pudo generar ID", Toast.LENGTH_SHORT).show()
            return
        }

        val datos = hashMapOf<String, Any>(
            "id" to keyId,
            "categoria" to categoria
        )

        ref.child(keyId)
            .setValue(datos)
            .addOnSuccessListener { subirImgStorage(keyId) }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(context, e.message ?: "Error al agregar categoría", Toast.LENGTH_SHORT).show()
            }
    }

    /** Sube la imagen a /Categorias/{id} y actualiza la URL en RTDB. */
    private fun subirImgStorage(keyId: String) {
        val uri = imageUri ?: run {
            progressDialog.dismiss()
            Toast.makeText(context, "Imagen no disponible", Toast.LENGTH_SHORT).show()
            return
        }

        progressDialog.setMessage("Subiendo imagen")

        val storageReference = FirebaseStorage.getInstance().getReference("Categorias/$keyId")
        storageReference.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl
                    .addOnSuccessListener { url ->
                        val imgMap = hashMapOf<String, Any>("imagenUrl" to url.toString())
                        FirebaseDatabase.getInstance()
                            .getReference("Categorias")
                            .child(keyId)
                            .updateChildren(imgMap)
                            .addOnSuccessListener {
                                progressDialog.dismiss()
                                Toast.makeText(mContext, "Se agregó la categoría con éxito", Toast.LENGTH_SHORT).show()
                                // Reset UI
                                binding.etCategoria.setText("")
                                imageUri = null
                                binding.imgCategorias.setImageResource(R.drawable.categorias)
                            }
                            .addOnFailureListener { e ->
                                progressDialog.dismiss()
                                Toast.makeText(context, e.message ?: "No se guardó la URL de imagen", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        progressDialog.dismiss()
                        Toast.makeText(context, e.message ?: "No se pudo obtener la URL de imagen", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(context, e.message ?: "No se pudo subir la imagen", Toast.LENGTH_SHORT).show()
            }
    }
}