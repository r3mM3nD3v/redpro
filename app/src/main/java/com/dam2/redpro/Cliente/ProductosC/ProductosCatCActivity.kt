package com.dam2.redpro.Cliente.ProductosC

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dam2.redpro.Adaptadores.AdaptadorProductoC
import com.dam2.redpro.Modelos.ModeloProducto
import com.dam2.redpro.databinding.ActivityProductosCatCactivityBinding
import com.google.firebase.database.*

/**
 * Pantalla de productos por categoría (cliente).
 * - Lista los productos de la categoría seleccionada.
 * - Permite buscar por nombre.
 */
class ProductosCatCActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductosCatCactivityBinding
    private lateinit var productoArrayList: ArrayList<ModeloProducto>
    private lateinit var adaptadorProductos: AdaptadorProductoC

    private var nombreCat: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductosCatCactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener nombre de categoría desde el intent
        nombreCat = intent.getStringExtra("nombreCat") ?: "Sin categoría"
        binding.txtProductoCat.text = "Categoría - $nombreCat"

        // Cargar productos filtrados por categoría
        listarProductos(nombreCat)

        // Botón regresar
        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Filtro en tiempo real
        binding.etBuscarProducto.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(filtro: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    val consulta = filtro.toString()
                    adaptadorProductos.filter.filter(consulta)
                } catch (_: Exception) {
                }
            }
        })

        // Botón limpiar búsqueda
        binding.IbLimpiarCampo.setOnClickListener {
            val consulta = binding.etBuscarProducto.text.toString().trim()
            if (consulta.isNotEmpty()) {
                binding.etBuscarProducto.setText("")
                Toast.makeText(this, "Campo limpiado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No se ha ingresado una consulta", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** Carga desde Firebase los productos pertenecientes a una categoría */
    private fun listarProductos(nombreCat: String) {
        productoArrayList = ArrayList()

        FirebaseDatabase.getInstance()
            .getReference("Productos")
            .orderByChild("categoria").equalTo(nombreCat)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    productoArrayList.clear()
                    for (ds in snapshot.children) {
                        ds.getValue(ModeloProducto::class.java)?.let {
                            productoArrayList.add(it)
                        }
                    }

                    // Orden alfabético por nombre
                    productoArrayList.sortBy { it.nombre }

                    // Asignar adaptador limpio (sin precios ni pagos)
                    adaptadorProductos = AdaptadorProductoC(this@ProductosCatCActivity, productoArrayList)
                    binding.productosRV.adapter = adaptadorProductos
                }

                override fun onCancelled(error: DatabaseError) {
                    // Silenciar errores de Firebase
                }
            })
    }
}