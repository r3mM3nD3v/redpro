package com.proyecto.red_pro.ui.profesional

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.proyecto.red_pro.data.model.Servicio
import com.proyecto.red_pro.data.repo.FirestoreRepository
import com.proyecto.red_pro.databinding.FragmentMisServiciosBinding
import com.proyecto.red_pro.ui.widgets.ServiciosAdapter
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest

class MisServiciosFragment : Fragment() {
    private var _b: FragmentMisServiciosBinding? = null
    private val b get() = _b!!
    private val repo by lazy { FirestoreRepository(Firebase.firestore) }
    private val adapter by lazy { ServiciosAdapter { openEdit(it) } }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        FragmentMisServiciosBinding.inflate(inflater, container, false).also { _b = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        b.rvMisServicios.layoutManager = LinearLayoutManager(requireContext())
        b.rvMisServicios.adapter = adapter

        b.progress.visibility = View.VISIBLE
        b.empty.visibility = View.GONE

        val uid = Firebase.auth.currentUser?.uid ?: return

        // ✅ Recolecta SOLO cuando el Fragment esté STARTED/RESUMED y se reanuda al volver
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repo.listenMisServicios(uid).collectLatest { list ->
                    b.progress.visibility = View.GONE
                    b.empty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                    // ✅ Fuerza una nueva instancia de lista (evita que DiffUtil crea que es la misma referencia)
                    adapter.submitList(list.toList())
                }
            }
        }
    }

    private fun openEdit(s: Servicio) {
        startActivity(
            Intent(requireContext(), ServicioEditActivity::class.java).apply {
                putExtra("id", s.id)
                putExtra("titulo", s.titulo)
                putExtra("descripcion", s.descripcion)
                putExtra("categoria", s.categoria)
                putExtra("precio", s.precio)
                putExtra("ubicacion", s.ubicacion)
            }
        )
    }

    override fun onDestroyView() { _b = null; super.onDestroyView() }
}