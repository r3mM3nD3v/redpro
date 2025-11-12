package com.proyecto.red_pro.ui.cliente

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.proyecto.red_pro.R
import com.proyecto.red_pro.data.model.Servicio
import com.proyecto.red_pro.data.repo.FirestoreRepository
import com.proyecto.red_pro.databinding.FragmentExplorarBinding
import com.proyecto.red_pro.ui.widgets.ServiciosAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ExplorarFragment : Fragment() {
    private var _b: FragmentExplorarBinding? = null
    private val b get() = _b!!
    private val repo by lazy { FirestoreRepository(Firebase.firestore) }
    private val adapter by lazy { ServiciosAdapter(onClick = { showServicioDetalle(it) }) }

    private val categorias = listOf("Todas","Educación","Tecnología","Salud","Oficios","Otros")
    private var fullList: List<Servicio> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        FragmentExplorarBinding.inflate(inflater, container, false).also { _b = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        b.rvServicios.layoutManager = LinearLayoutManager(requireContext())
        b.rvServicios.adapter = adapter
        b.spCategoria.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categorias)

        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { applyFilters() }
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
        }
        b.etBuscar.addTextChangedListener(watcher)
        b.etUbicacion.addTextChangedListener(watcher)
        b.etPrecioMax.addTextChangedListener(watcher)
        b.spCategoria.setOnItemSelectedListener { applyFilters() }

        b.progress.visibility = View.VISIBLE
        b.empty.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repo.listenServicios().collectLatest { list ->
                    fullList = list
                    b.progress.visibility = View.GONE
                    applyFilters()
                }
            }
        }
    }

    private fun android.widget.Spinner.setOnItemSelectedListener(onSelect: () -> Unit) {
        this.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: android.widget.AdapterView<*>, v: View?, pos: Int, id: Long) { onSelect() }
            override fun onNothingSelected(p0: android.widget.AdapterView<*>) {}
        }
    }

    private fun applyFilters() {
        val q = b.etBuscar.text.toString().trim().lowercase()
        val cat = categorias[b.spCategoria.selectedItemPosition]
        val ub = b.etUbicacion.text.toString().trim().lowercase()
        val max = b.etPrecioMax.text.toString().toDoubleOrNull()

        val filtered = fullList.filter { s ->
            (q.isEmpty() || s.titulo.lowercase().contains(q) || s.descripcion.lowercase().contains(q)) &&
                    (cat == "Todas" || s.categoria == cat) &&
                    (ub.isEmpty() || s.ubicacion.lowercase().contains(ub)) &&
                    (max == null || s.precio <= max)
        }
        b.empty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        adapter.submitList(filtered.toList()) // ✅ nueva instancia de lista
    }

    private fun showServicioDetalle(s: Servicio) {
        val msg = "Categoría: ${s.categoria}\n" +
                "Precio: USD ${"%.2f".format(s.precio)}\n" +
                "Ubicación: ${s.ubicacion}\n\n${s.descripcion}"
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(s.titulo)
            .setMessage(msg)
            .setPositiveButton("Cerrar", null)
            .show()
    }

    override fun onDestroyView() { _b = null; super.onDestroyView() }
}