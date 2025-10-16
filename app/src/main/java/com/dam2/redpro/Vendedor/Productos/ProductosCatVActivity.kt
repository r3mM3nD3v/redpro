package com.dam2.redpro.Vendedor.Productos

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dam2.redpro.Adaptadores.AdaptadorProducto
import com.dam2.redpro.Modelos.ModeloProducto
import com.dam2.redpro.databinding.ActivityProductosCatVactivityBinding
import com.google.firebase.database.*

/**
 * Lista productos de una categoría (Vendedor)
 */
class ProductosCatVActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductosCatVactivityBinding
    private var nombreCat: String = ""

    private val productoArrayList = ArrayList<ModeloProducto>()
    private lateinit var adaptadorProductos: AdaptadorProducto

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductosCatVactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Categoría recibida desde el intent
        nombreCat = intent.getStringExtra("nombreCat").orEmpty()
        binding.txtProductoCat.text = "Categoria - $nombreCat"

        // Configurar adapter vacío de inicio
        adaptadorProductos = AdaptadorProducto(this, productoArrayList)
        binding.productosRV.adapter = adaptadorProductos

        listarProductos(nombreCat)
    }

    /** Carga productos de la categoría dada y los muestra en el RecyclerView. */
    private fun listarProductos(nombreCat: String) {
        FirebaseDatabase.getInstance()
            .getReference("Productos")
            .orderByChild("categoria")
            .equalTo(nombreCat)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    productoArrayList.clear()
                    for (ds in snapshot.children) {
                        ds.getValue(ModeloProducto::class.java)?.let { productoArrayList.add(it) }
                    }
                    adaptadorProductos.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // no-op para evitar crash si falla la lectura
                }
            })
    }
}