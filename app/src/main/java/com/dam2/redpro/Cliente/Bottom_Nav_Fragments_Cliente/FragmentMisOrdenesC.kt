package com.dam2.redpro.Cliente.Bottom_Nav_Fragments_Cliente

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.dam2.redpro.Adaptadores.AdaptadorOrdenCompra
import com.dam2.redpro.Modelos.ModeloOrdenCompra
import com.dam2.redpro.databinding.FragmentMisOrdenesCBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

/**
 * Fragment que muestra las órdenes del cliente actual.
 */
class FragmentMisOrdenesC : Fragment() {

    private lateinit var binding: FragmentMisOrdenesCBinding
    private lateinit var mContext: Context
    private lateinit var ordenesArrayList: ArrayList<ModeloOrdenCompra>
    private lateinit var ordenAdaptador: AdaptadorOrdenCompra

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMisOrdenesCBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        verOrdenes()
    }

    /**
     * Carga las órdenes del usuario autenticado desde Firebase.
     */
    private fun verOrdenes() {
        ordenesArrayList = ArrayList()

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid.isNullOrBlank()) {
            Toast.makeText(mContext, "Sesión no válida", Toast.LENGTH_SHORT).show()
            return
        }

        val ref = FirebaseDatabase.getInstance().getReference("Ordenes")
        ref.orderByChild("ordenadoPor").equalTo(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    ordenesArrayList.clear()
                    for (ds in snapshot.children) {
                        ds.getValue(ModeloOrdenCompra::class.java)?.let { ordenesArrayList.add(it) }
                    }

                    if (ordenesArrayList.isEmpty()) {
                        Toast.makeText(mContext, "No tienes órdenes registradas", Toast.LENGTH_SHORT).show()
                    }

                    ordenAdaptador = AdaptadorOrdenCompra(mContext, ordenesArrayList)
                    binding.ordenesRv.adapter = ordenAdaptador
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(mContext, "Error al cargar órdenes", Toast.LENGTH_SHORT).show()
                }
            })
    }
}