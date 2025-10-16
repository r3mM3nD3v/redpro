package com.dam2.redpro.Cliente.Bottom_Nav_Fragments_Cliente

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dam2.redpro.Adaptadores.AdaptadorCategoriaC
import com.dam2.redpro.Modelos.ModeloCategoria
import com.dam2.redpro.databinding.FragmentTiendaCBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

/**
 * Tienda (Cliente)
 * - Muestra saludo y dirección del usuario.
 * - Lista categorías disponibles.
 */
class FragmentTiendaC : Fragment() {

    private lateinit var binding: FragmentTiendaCBinding
    private lateinit var mContext: Context
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var categoriaArrayList: ArrayList<ModeloCategoria>
    private lateinit var adaptadorCategoria: AdaptadorCategoriaC

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTiendaCBinding.inflate(LayoutInflater.from(mContext), container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()

        leerInfoCliente()
        listarCategorias()
    }

    /** Lee nombre y dirección del usuario autenticado para mostrar saludo. */
    private fun leerInfoCliente() {
        val uid = firebaseAuth.uid ?: return
        FirebaseDatabase.getInstance()
            .getReference("Usuarios")
            .child(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombres = "${snapshot.child("nombres").value}"
                    val direccion = "${snapshot.child("direccion").value}"
                    binding.bienvenidaTXT.text = "Bienvenido(a): $nombres"
                    binding.direccionTXT.text = direccion
                }

                override fun onCancelled(error: DatabaseError) {
                    // Silenciar para no romper UI si falla la lectura
                }
            })
    }

    /** Carga y muestra las categorías, ordenadas por nombre. */
    private fun listarCategorias() {
        categoriaArrayList = ArrayList()

        FirebaseDatabase.getInstance()
            .getReference("Categorias")
            .orderByChild("categoria")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    categoriaArrayList.clear()
                    for (ds in snapshot.children) {
                        ds.getValue(ModeloCategoria::class.java)?.let { categoriaArrayList.add(it) }
                    }
                    adaptadorCategoria = AdaptadorCategoriaC(mContext, categoriaArrayList)
                    binding.categoriasRV.adapter = adaptadorCategoria
                }

                override fun onCancelled(error: DatabaseError) {
                    // Silenciar para no romper UI si falla la lectura
                }
            })
    }
}