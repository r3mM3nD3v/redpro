package com.dam2.redpro.Vendedor.Nav_Fragments_Vendedor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dam2.redpro.R
import com.dam2.redpro.Vendedor.Bottom_Nav_Fragments_Vendedor.FragmentMisProductosV
import com.dam2.redpro.Vendedor.Bottom_Nav_Fragments_Vendedor.FragmentOrdenesV
import com.dam2.redpro.Vendedor.Productos.AgregarProductoActivity
import com.dam2.redpro.databinding.FragmentInicioVBinding

/**
 * Home (Vendedor) con bottom navigation:
 * - Mis Productos
 * - Mis Órdenes
 * + FAB para agregar producto.
 */
class FragmentInicioV : Fragment() {

    private lateinit var binding: FragmentInicioVBinding
    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentInicioVBinding.inflate(inflater, container, false)

        // Selector de pestañas
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.op_mis_productos_v -> replaceFragment(FragmentMisProductosV())
                R.id.op_mis_ordenes_v   -> replaceFragment(FragmentOrdenesV())
            }
            true
        }

        // Pestaña por defecto
        binding.bottomNavigation.selectedItemId = R.id.op_mis_productos_v
        replaceFragment(FragmentMisProductosV())

        // FAB: abrir alta/edición de producto (modo alta)
        binding.addFab.setOnClickListener {
            // Debounce simple evitando múltiples lanzamientos en taps rápidos
            binding.addFab.isEnabled = false
            startActivity(Intent(mContext, AgregarProductoActivity::class.java).apply {
                putExtra("Edicion", false)
            })
            // Rehabilitar tras lanzar (si vuelves, el view se recrea de nuevo)
            binding.addFab.postDelayed({ binding.addFab.isEnabled = true }, 600)
        }

        return binding.root
    }

    /** Reemplaza el fragmento hijo del contenedor inferior. */
    private fun replaceFragment(fragment: Fragment) {
        if (!isAdded) return
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.bottomFragment, fragment)
            .commit()
    }
}