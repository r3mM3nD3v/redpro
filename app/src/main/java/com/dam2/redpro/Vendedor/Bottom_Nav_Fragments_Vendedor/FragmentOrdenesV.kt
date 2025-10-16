package com.dam2.redpro.Vendedor.Bottom_Nav_Fragments_Vendedor

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import com.dam2.redpro.Adaptadores.AdaptadorOrdenCompraV
import com.dam2.redpro.Modelos.ModeloOrdenCompra
import com.dam2.redpro.databinding.FragmentOrdenesVBinding
import com.google.firebase.database.*

/**
 * Lista de órdenes (Vendedor)
 * - Ver todas, buscar por ID (prefijo) y filtrar por estado.
 */
class FragmentOrdenesV : Fragment() {

    private var _binding: FragmentOrdenesVBinding? = null
    private val binding get() = _binding!!

    private lateinit var mContext: Context

    private val ordenesArrayList = ArrayList<ModeloOrdenCompra>()
    private lateinit var ordenAdaptador: AdaptadorOrdenCompraV

    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    override fun onAttach(context: Context) {
        this.mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOrdenesVBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ordenAdaptador = AdaptadorOrdenCompraV(mContext, ordenesArrayList)
        binding.ordenesRv.adapter = ordenAdaptador

        verOrdenes()

        binding.IbFiltroEstado.setOnClickListener { filtrarOrdenMenu() }

        // Búsqueda por ID con debounce de 1s
        binding.etBuscarOrdenId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(id: CharSequence?, start: Int, before: Int, count: Int) {
                searchRunnable?.let { handler.removeCallbacks(it) }
                val query = id?.toString().orEmpty()
                searchRunnable = Runnable {
                    if (query.isNotEmpty()) buscarOrdenPorId(query) else verOrdenes()
                }
                handler.postDelayed(searchRunnable!!, 1000)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchRunnable?.let { handler.removeCallbacks(it) }
        _binding = null
    }

    /** Búsqueda por prefijo de ID: orderByChild("idOrden").startAt(id).endAt(id + "\uf8ff") */
    private fun buscarOrdenPorId(idOrden: String) {
        val ref = FirebaseDatabase.getInstance()
            .getReference("Ordenes")
            .orderByChild("idOrden")
            .startAt(idOrden)
            .endAt(idOrden + "\uf8ff")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                ordenesArrayList.clear()
                if (snapshot.exists()) {
                    for (ds in snapshot.children) {
                        ds.getValue(ModeloOrdenCompra::class.java)?.let { ordenesArrayList.add(it) }
                    }
                }
                ordenAdaptador.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                // no-op
            }
        })
    }

    /** Menú de filtro por estado con IDs consistentes. */
    private fun filtrarOrdenMenu() {
        val popupMenu = PopupMenu(mContext, binding.IbFiltroEstado).apply {
            menu.add(Menu.NONE, 0, 0, "Todos")
            menu.add(Menu.NONE, 1, 1, "Solicitud recibida")
            menu.add(Menu.NONE, 2, 2, "En preparación")
            menu.add(Menu.NONE, 3, 3, "Entregado")
            menu.add(Menu.NONE, 4, 4, "Cancelado")
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    0 -> verOrdenes()
                    1 -> filtroOrden("Solicitud recibida")
                    2 -> filtroOrden("En preparación")
                    3 -> filtroOrden("Entregado")
                    4 -> filtroOrden("Cancelado")
                }
                true
            }
        }
        popupMenu.show()
    }

    /** Filtro eficiente por estado usando equalTo(estado). */
    private fun filtroOrden(estado: String) {
        val ref = FirebaseDatabase.getInstance()
            .getReference("Ordenes")
            .orderByChild("estadoOrden")
            .equalTo(estado)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                ordenesArrayList.clear()
                for (ds in snapshot.children) {
                    ds.getValue(ModeloOrdenCompra::class.java)?.let { ordenesArrayList.add(it) }
                }
                ordenAdaptador.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                // no-op
            }
        })
    }

    /** Carga todas las órdenes (listener de una sola vez). */
    private fun verOrdenes() {
        val ref = FirebaseDatabase.getInstance().getReference("Ordenes")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                ordenesArrayList.clear()
                for (ds in snapshot.children) {
                    ds.getValue(ModeloOrdenCompra::class.java)?.let { ordenesArrayList.add(it) }
                }
                ordenAdaptador.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                // no-op
            }
        })
    }
}