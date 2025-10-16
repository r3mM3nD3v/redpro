package com.dam2.redpro.DetalleProducto

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dam2.redpro.Adaptadores.AdaptadorImgSlider
import com.dam2.redpro.Modelos.ModeloImgSlider
import com.dam2.redpro.Modelos.ModeloProducto
import com.dam2.redpro.databinding.ActivityDetalleProductoBinding
import com.google.firebase.database.*

/**
 * Pantalla de detalle de producto.
 * Muestra nombre, descripción e imágenes (slider).
 */
class DetalleProductoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleProductoBinding
    private var idProducto: String = ""
    private lateinit var imagenSlider: ArrayList<ModeloImgSlider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener idProducto desde el Intent de forma segura
        idProducto = intent.getStringExtra("idProducto") ?: run {
            // Si no viene, no podemos continuar en esta pantalla
            finish()
            return
        }

        // Cargar datos
        cargarImagenesProd()
        cargarInfoProducto()

        // Navegación atrás
        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    /** Lee nombre y descripción del producto y los muestra. */
    private fun cargarInfoProducto() {
        FirebaseDatabase.getInstance()
            .getReference("Productos")
            .child(idProducto)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val modeloProducto = snapshot.getValue(ModeloProducto::class.java)
                    binding.nombrePD.text = modeloProducto?.nombre.orEmpty()
                    binding.descripcionPD.text = modeloProducto?.descripcion.orEmpty()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Evitar crash si Firebase cancela la lectura
                }
            })
    }

    /** Carga todas las imágenes del producto para el slider. */
    private fun cargarImagenesProd() {
        imagenSlider = ArrayList()
        FirebaseDatabase.getInstance()
            .getReference("Productos")
            .child(idProducto)
            .child("Imagenes")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    imagenSlider.clear()
                    for (ds in snapshot.children) {
                        ds.getValue(ModeloImgSlider::class.java)?.let { imagenSlider.add(it) }
                    }
                    binding.imagenVP.adapter = AdaptadorImgSlider(this@DetalleProductoActivity, imagenSlider)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Silenciar para no romper la UI si hay error de lectura
                }
            })
    }
}