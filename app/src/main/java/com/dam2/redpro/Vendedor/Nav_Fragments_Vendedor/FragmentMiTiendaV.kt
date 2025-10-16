package com.dam2.redpro.Vendedor.Nav_Fragments_Vendedor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dam2.redpro.Vendedor.ListaClientes.ListaClientesActivity
import com.dam2.redpro.databinding.FragmentMiTiendaVBinding
import com.google.firebase.database.*

/**
 * Panel de Mi Tienda (Vendedor)
 * - Muestra contadores por estado de órdenes.
 * - Acceso a la lista de clientes.
 */
class FragmentMiTiendaV : Fragment() {

    private lateinit var binding: FragmentMiTiendaVBinding
    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMiTiendaVBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        estadosOrden()

        binding.btnVerCliente.setOnClickListener {
            startActivity(Intent(mContext, ListaClientesActivity::class.java))
        }
    }

    /**
     * Lee las órdenes una vez y calcula contadores por estado.
     * Estados contemplados:
     *  - "Solicitud recibida"
     *  - "En preparación"   (estandarizado)
     *  - "Entregado"
     *  - "Cancelado"
     */
    private fun estadosOrden() {
        val ref = FirebaseDatabase.getInstance().getReference("Ordenes")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var solicitudes = 0
                var enPreparacion = 0
                var entregadas = 0
                var canceladas = 0

                for (ordenSn in snapshot.children) {
                    val estadoOrden = "${ordenSn.child("estadoOrden").value}"

                    when (estadoOrden) {
                        "Solicitud recibida" -> solicitudes++
                        // Normalizamos "En preparación" (si hubiera registros viejos con "En Preparación", los tratamos igual)
                        "En preparación", "En Preparación" -> enPreparacion++
                        "Entregado" -> entregadas++
                        "Cancelado" -> canceladas++
                    }
                }

                binding.tvEstado1.text = "Solicitudes recibidas: $solicitudes"
                binding.tvEstado3.text = "Órdenes en preparación: $enPreparacion"
                binding.tvEstado4.text = "Órdenes entregadas: $entregadas"
                binding.tvEstado5.text = "Órdenes canceladas: $canceladas"
            }

            override fun onCancelled(error: DatabaseError) {
                // Silenciar para no romper la UI si falla la lectura
            }
        })
    }
}