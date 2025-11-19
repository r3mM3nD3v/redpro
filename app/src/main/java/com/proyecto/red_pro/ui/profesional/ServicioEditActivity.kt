package com.proyecto.red_pro.ui.profesional

import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.proyecto.red_pro.data.model.Servicio
import com.proyecto.red_pro.data.repo.FirestoreRepository
import com.proyecto.red_pro.databinding.ActivityServicioEditBinding
import com.proyecto.red_pro.ui.widgets.FotosAdapter
import com.proyecto.red_pro.util.LocalImages
import java.io.File
import android.view.View

class ServicioEditActivity : AppCompatActivity() {

    private lateinit var b: ActivityServicioEditBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val repo by lazy { FirestoreRepository(FirebaseFirestore.getInstance()) }

    private var currentId: String? = null
    private val categorias = listOf("Educación", "Tecnología", "Salud", "Oficios", "Otros")

    // Mantendremos los paths locales acá:
    private val fotoPaths = mutableListOf<String>()
    private lateinit var fotosAdapter: FotosAdapter

    // Abrir galería (múltiple)
    private val pickMultiple = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNullOrEmpty()) return@registerForActivityResult
        val sid = currentId ?: pendingGeneratedId()
        val destDir = File(filesDir, "images/services/$sid")
        val added = mutableListOf<String>()
        uris.forEachIndexed { idx, uri ->
            val name = "foto_${System.currentTimeMillis()}_${idx}.jpg"
            val file = LocalImages.copyFromContentUri(contentResolver, uri, destDir, name)
            if (file != null) added.add(file.absolutePath)
        }
        if (added.isNotEmpty()) {
            fotosAdapter.addAll(added)
            Toast.makeText(this, "Se agregaron ${added.size} foto(s)", Toast.LENGTH_SHORT).show()
        }
    }

    // Para casos "nuevo servicio" sin id, generamos uno temporal estable
    private var _tempId: String? = null
    private fun pendingGeneratedId(): String {
        if (_tempId == null) {
            _tempId = FirebaseFirestore.getInstance().collection("services").document().id
        }
        return _tempId!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityServicioEditBinding.inflate(layoutInflater)
        setContentView(b.root)

        // Spinner
        b.spCategoria.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categorias
        )

        // Recycler horizontal para fotos
        b.rvFotos.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        fotosAdapter = FotosAdapter(fotoPaths) { _, removedPath ->
            // Opcional: borrar archivo local cuando el usuario lo quita de la lista
            runCatching { File(removedPath).delete() }
        }
        b.rvFotos.adapter = fotosAdapter

        // Si venimos a editar, cargamos datos + fotos
        currentId = intent.getStringExtra("id")

        // Si no hay id (servicio nuevo), ocultamos el botón Eliminar
        if (currentId == null) {
            b.btnEliminar.visibility = View.GONE
        } else {
            b.btnEliminar.visibility = View.VISIBLE
        }

        b.etTitulo.setText(intent.getStringExtra("titulo") ?: "")
        b.etDescripcion.setText(intent.getStringExtra("descripcion") ?: "")
        b.etUbicacion.setText(intent.getStringExtra("ubicacion") ?: "")
        val precio = intent.getDoubleExtra("precio", 0.0)
        if (precio != 0.0) b.etPrecio.setText(precio.toString())
        val cat = intent.getStringExtra("categoria")
        cat?.let {
            b.spCategoria.setSelection(
                categorias.indexOf(it).coerceAtLeast(0)
            )
        }

        // Si hay id, intenta cargar photoPaths desde Firestore
        currentId?.let { sid ->
            FirebaseFirestore.getInstance()
                .collection("services")
                .document(sid)
                .get()
                .addOnSuccessListener { snap ->
                    val paths = (snap.get("photoPaths") as? List<*>)?.filterIsInstance<String>()
                        ?: emptyList()
                    if (paths.isNotEmpty()) {
                        fotoPaths.addAll(paths)
                        fotosAdapter.notifyItemRangeInserted(0, paths.size)
                    }
                }
        }

        b.btnAgregarFotos.setOnClickListener { pickMultiple.launch("image/*") }
        b.btnGuardar.setOnClickListener { save() }
        b.btnEliminar.setOnClickListener { delete() }
    }

    private fun save() {
        val uid = auth.currentUser?.uid ?: return
        val titulo = b.etTitulo.text.toString().trim()
        val descripcion = b.etDescripcion.text.toString().trim()
        val precio = b.etPrecio.text.toString().toDoubleOrNull() ?: 0.0
        val ubicacion = b.etUbicacion.text.toString().trim()
        val categoria = categorias[b.spCategoria.selectedItemPosition]

        if (titulo.isEmpty()) {
            Toast.makeText(this, "Título requerido", Toast.LENGTH_SHORT).show(); return
        }
        if (fotosAdapter.getPaths().isEmpty()) {
            Toast.makeText(this, "Agrega al menos una imagen del servicio", Toast.LENGTH_SHORT).show(); return
        }

        // Si no hay id (nuevo), usa el temporal generado
        val id = currentId ?: pendingGeneratedId()

        // Construye el modelo
        val s = Servicio(
            id = id,
            uidProfesional = uid,
            titulo = titulo,
            descripcion = descripcion,
            categoria = categoria,
            precio = precio,
            ubicacion = ubicacion,
            timestamp = System.currentTimeMillis(),
            photoPaths = fotosAdapter.getPaths()
        )

        repo.upsertServicio(s)
            .addOnSuccessListener {
                currentId = id // por si recién se creó
                Toast.makeText(this, "Servicio guardado", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, it.message ?: "Error al guardar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun delete() {
        val id = currentId ?: return
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Eliminar servicio")
            .setMessage("¿Seguro que deseas eliminarlo?")
            .setPositiveButton("Eliminar") { _, _ ->
                repo.deleteServicio(id)
                    .addOnSuccessListener {
                        // Limpia fotos locales del servicio
                        runCatching {
                            File(filesDir, "images/services/$id").deleteRecursively()
                        }
                        Toast.makeText(this, "Servicio eliminado", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, e.message ?: "Error al eliminar", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}