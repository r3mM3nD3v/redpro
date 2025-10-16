package com.dam2.redpro.Vendedor.Productos

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.dam2.redpro.Adaptadores.AdaptadorImagenSeleccionada
import com.dam2.redpro.Constantes
import com.dam2.redpro.Modelos.ModeloCategoria
import com.dam2.redpro.Modelos.ModeloImagenSeleccionada
import com.dam2.redpro.Vendedor.MainActivityVendedor
import com.dam2.redpro.databinding.ActivityAgregarProductoBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

/**
 * Alta/Edición de Productos (Vendedor)
 * - Guarda: nombre, descripción, categoría e imágenes.
 */
class AgregarProductoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAgregarProductoBinding
    private var imagenUri: Uri? = null

    private lateinit var imagenSelecArrayList: ArrayList<ModeloImagenSeleccionada>
    private lateinit var adaptadorImagenSel: AdaptadorImagenSeleccionada

    private lateinit var categoriasArrayList: ArrayList<ModeloCategoria>
    private lateinit var progressDialog: ProgressDialog

    private var Edicion = false
    private var idProducto = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgregarProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cargarCategorias()

        progressDialog = ProgressDialog(this).apply {
            setTitle("Espere por favor")
            setCanceledOnTouchOutside(false)
        }

        Edicion = intent.getBooleanExtra("Edicion", false)
        if (Edicion) {
            idProducto = intent.getStringExtra("idProducto") ?: ""
            binding.txtAgregarProductos.text = "Editar producto"
            cargarInfo()
        } else {
            binding.txtAgregarProductos.text = "Agregar un producto"
        }

        imagenSelecArrayList = ArrayList()

        binding.imgAgregarProducto.setOnClickListener { seleccionarImg() }
        binding.Categoria.setOnClickListener { selecCategorias() }
        binding.btnAgregarProducto.setOnClickListener { validarInfo() }

        cargarImagenes()
    }

    /** Carga datos del producto cuando estamos en modo edición. (sin precio/desc) */
    private fun cargarInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Productos")
        ref.child(idProducto).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nombre = "${snapshot.child("nombre").value}"
                val descripcion = "${snapshot.child("descripcion").value}"
                val categoria = "${snapshot.child("categoria").value}"

                binding.etNombresP.setText(nombre)
                binding.etDescripcionP.setText(descripcion)
                binding.Categoria.setText(categoria)

                // Cargar imágenes existentes (marcadas como desde Internet)
                snapshot.child("Imagenes").ref
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapImgs: DataSnapshot) {
                            imagenSelecArrayList.clear()
                            for (ds in snapImgs.children) {
                                val id = "${ds.child("id").value}"
                                val imagenUrl = "${ds.child("imagenUrl").value}"
                                imagenSelecArrayList.add(
                                    ModeloImagenSeleccionada(
                                        id = id,
                                        imagenUri = null,
                                        imagenUrl = imagenUrl,
                                        deInternet = true
                                    )
                                )
                            }
                            cargarImagenes()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // No romper UI si hay error
                        }
                    })
            }

            override fun onCancelled(error: DatabaseError) {
                // No romper UI si hay error
            }
        })
    }

    private var nombreP = ""
    private var descripcionP = ""
    private var categoriaP = ""

    /** Valida campos mínimos */
    private fun validarInfo() {
        nombreP = binding.etNombresP.text.toString().trim()
        descripcionP = binding.etDescripcionP.text.toString().trim()
        categoriaP = binding.Categoria.text.toString().trim()

        when {
            nombreP.isEmpty() -> {
                binding.etNombresP.error = "Ingrese nombre"
                binding.etNombresP.requestFocus()
            }
            descripcionP.isEmpty() -> {
                binding.etDescripcionP.error = "Ingrese descripción"
                binding.etDescripcionP.requestFocus()
            }
            categoriaP.isEmpty() -> {
                binding.Categoria.error = "Seleccione una categoría"
                binding.Categoria.requestFocus()
            }
            else -> {
                if (Edicion) {
                    actualizarInfo()
                } else {
                    if (imagenSelecArrayList.isEmpty()) {
                        Toast.makeText(this, "Agregue al menos una imagen", Toast.LENGTH_SHORT).show()
                    } else {
                        agregarProducto()
                    }
                }
            }
        }
    }

    /** Actualiza nombre/desc/categoría; luego asegura subir nuevas imágenes (si las hay). */
    private fun actualizarInfo() {
        progressDialog.setMessage("Actualizando producto")
        progressDialog.show()

        val cambios = hashMapOf<String, Any>(
            "nombre" to nombreP,
            "descripcion" to descripcionP,
            "categoria" to categoriaP
        )

        FirebaseDatabase.getInstance()
            .getReference("Productos")
            .child(idProducto)
            .updateChildren(cambios)
            .addOnSuccessListener {
                progressDialog.dismiss()
                subirImgsStorage(idProducto)
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Falló la actualización: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /** Crea el producto y luego sube imágenes. */
    private fun agregarProducto() {
        progressDialog.setMessage("Agregando producto")
        progressDialog.show()

        val ref = FirebaseDatabase.getInstance().getReference("Productos")
        val keyId = ref.push().key ?: run {
            progressDialog.dismiss()
            Toast.makeText(this, "No se pudo generar ID", Toast.LENGTH_SHORT).show()
            return
        }

        val datos = hashMapOf<String, Any>(
            "id" to keyId,
            "nombre" to nombreP,
            "descripcion" to descripcionP,
            "categoria" to categoriaP
        )

        ref.child(keyId)
            .setValue(datos)
            .addOnSuccessListener { subirImgsStorage(keyId) }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, e.message ?: "Error al agregar producto", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Sube SOLO las imágenes nuevas (las que no vienen de Internet).
     * Las que ya existían (deInternet=true) se mantienen sin cambios.
     */
    private fun subirImgsStorage(keyId: String) {
        if (imagenSelecArrayList.none { !it.deInternet }) {
            // No hay imágenes nuevas; finalizar flujo según edición/creación
            if (Edicion) {
                startActivity(Intent(this@AgregarProductoActivity, MainActivityVendedor::class.java))
                Toast.makeText(this, "Se actualizó la información del producto", Toast.LENGTH_SHORT).show()
                finishAffinity()
            } else {
                progressDialog.dismiss()
                Toast.makeText(this, "Se agregó el producto", Toast.LENGTH_SHORT).show()
                limpiarCampos()
            }
            return
        }

        for (modelo in imagenSelecArrayList) {
            if (modelo.deInternet) continue

            val nombreImagen = modelo.id
            val rutaImagen = "Productos/$nombreImagen"

            val storageRef = FirebaseStorage.getInstance().getReference(rutaImagen)
            val uri = modelo.imagenUri ?: continue

            storageRef.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl
                        .addOnSuccessListener { url ->
                            val imgMap = hashMapOf<String, Any>(
                                "id" to nombreImagen,
                                "imagenUrl" to url.toString()
                            )
                            FirebaseDatabase.getInstance()
                                .getReference("Productos")
                                .child(keyId)
                                .child("Imagenes")
                                .child(nombreImagen)
                                .updateChildren(imgMap)
                                .addOnCompleteListener {
                                    // Cuando acabe el último upload, cerramos/limpiamos.
                                    // (no contamos; mantén UX simple)
                                    progressDialog.dismiss()
                                    if (Edicion) {
                                        startActivity(Intent(this@AgregarProductoActivity, MainActivityVendedor::class.java))
                                        Toast.makeText(this, "Se actualizó la información del producto", Toast.LENGTH_SHORT).show()
                                        finishAffinity()
                                    } else {
                                        Toast.makeText(this, "Se agregó el producto", Toast.LENGTH_SHORT).show()
                                        limpiarCampos()
                                    }
                                }
                        }
                        .addOnFailureListener { e ->
                            progressDialog.dismiss()
                            Toast.makeText(this, e.message ?: "No se pudo obtener URL de imagen", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this, e.message ?: "No se pudo subir la imagen", Toast.LENGTH_SHORT).show()
                }
        }
    }

    /** Limpia formulario. */
    private fun limpiarCampos() {
        imagenSelecArrayList.clear()
        if (::adaptadorImagenSel.isInitialized) adaptadorImagenSel.notifyDataSetChanged()
        binding.etNombresP.setText("")
        binding.etDescripcionP.setText("")
        binding.Categoria.setText("")
        // Campos de precio/desc ya NO se usan
    }

    /** Carga categorías para el selector. */
    private fun cargarCategorias() {
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
                }
                override fun onCancelled(error: DatabaseError) {
                    // No romper UI si hay error
                }
            })
    }

    private var idCat = ""
    private var tituloCat = ""

    /** Muestra diálogo para seleccionar categoría. */
    private fun selecCategorias() {
        if (categoriasArrayList.isEmpty()) return

        val categoriasArray = arrayOfNulls<String>(categoriasArrayList.size)
        for (i in categoriasArray.indices) {
            categoriasArray[i] = categoriasArrayList[i].categoria
        }

        AlertDialog.Builder(this)
            .setTitle("Seleccione una categoría")
            .setItems(categoriasArray) { _, which ->
                idCat = categoriasArrayList[which].id
                tituloCat = categoriasArrayList[which].categoria
                binding.Categoria.text = tituloCat
            }
            .show()
    }

    /** Setea/recarga el RecyclerView de imágenes seleccionadas. */
    private fun cargarImagenes() {
        adaptadorImagenSel = AdaptadorImagenSeleccionada(this, imagenSelecArrayList, idProducto)
        binding.RVImagenesProducto.adapter = adaptadorImagenSel
    }

    /** Lanzador del ImagePicker. */
    private fun seleccionarImg() {
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .createIntent { intent -> resultadoImg.launch(intent) }
    }

    private val resultadoImg =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
            if (resultado.resultCode == Activity.RESULT_OK) {
                imagenUri = resultado.data?.data
                val tiempo = "${Constantes().obtenerTiempoD()}"
                imagenUri?.let {
                    val modeloImgSel = ModeloImagenSeleccionada(tiempo, it, null, false)
                    imagenSelecArrayList.add(modeloImgSel)
                    cargarImagenes()
                }
            } else {
                Toast.makeText(this, "Acción cancelada", Toast.LENGTH_SHORT).show()
            }
        }
}