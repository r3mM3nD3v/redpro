package com.dam2.redpro.Vendedor.ListaClientes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dam2.redpro.Adaptadores.AdaptadorCliente
import com.dam2.redpro.Modelos.ModeloUsuario
import com.dam2.redpro.databinding.ActivityListaClientesBinding
import com.google.firebase.database.*

/**
 * Lista de clientes (Usuarios con tipoUsuario == "cliente").
 */
class ListaClientesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListaClientesBinding
    private val clientesArrayList = ArrayList<ModeloUsuario>()
    private lateinit var adaptadorCliente: AdaptadorCliente

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaClientesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Configurar adapter vacío de inicio
        adaptadorCliente = AdaptadorCliente(this, clientesArrayList)
        binding.clienteRV.adapter = adaptadorCliente

        listarClientes()
    }

    /** Carga los usuarios con tipoUsuario = "cliente" y los muestra en el RecyclerView. */
    private fun listarClientes() {
        FirebaseDatabase.getInstance()
            .getReference("Usuarios")
            .orderByChild("tipoUsuario")
            .equalTo("cliente")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    clientesArrayList.clear()
                    for (ds in snapshot.children) {
                        ds.getValue(ModeloUsuario::class.java)?.let { clientesArrayList.add(it) }
                    }
                    adaptadorCliente.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // no-op: evita crash si falla la lectura
                }
            })
    }
}