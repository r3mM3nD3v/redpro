package com.dam2.redpro.Vendedor.Nav_Fragments_Vendedor

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.dam2.redpro.Adaptadores.AdaptadorProducto
import com.dam2.redpro.Modelos.ModeloProducto
import com.dam2.redpro.databinding.FragmentProductosVBinding
import com.google.firebase.database.*

/**
 * Listado de productos (Vendedor) con paginación simple y búsqueda por nombre.
 */
class FragmentProductosV : Fragment() {

    private var _binding: FragmentProductosVBinding? = null
    private val binding get() = _binding!!

    private lateinit var mContext: Context

    private val productoArrayList: ArrayList<ModeloProducto> = ArrayList()
    private lateinit var adaptadorProductos: AdaptadorProducto

    // Paginación por clave
    private val cantidadProductos = 4
    private var ultimoProductoVisible: DataSnapshot? = null
    private var primerProductoVisible: DataSnapshot? = null
    private var cargandoDatos = false
    private var primeraPagina = true

    // Debounce búsqueda
    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProductosVBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recycler
        adaptadorProductos = AdaptadorProducto(mContext, productoArrayList)
        binding.productosRV.apply {
            adapter = adaptadorProductos
            layoutManager = GridLayoutManager(mContext, 2)
        }

        // Búsqueda con debounce (1s)
        binding.etBuscarProducto.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(nombreP: CharSequence?, start: Int, before: Int, count: Int) {
                searchRunnable?.let { handler.removeCallbacks(it) }
                val query = nombreP?.toString().orEmpty()

                searchRunnable = Runnable {
                    if (query.isNotEmpty()) {
                        buscarProducto(query)
                    } else {
                        // reset paginación y recargar primera página
                        ultimoProductoVisible = null
                        primerProductoVisible = null
                        primeraPagina = true
                        listarProductos()
                    }
                }
                handler.postDelayed(searchRunnable!!, 1000)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Paginación
        binding.btnAnterior.isEnabled = false
        binding.btnAnterior.setOnClickListener {
            if (!cargandoDatos && !primeraPagina) cargarPaginaAnterior()
        }
        binding.btnSiguiente.setOnClickListener {
            if (!cargandoDatos) cargarPaginaSiguiente()
        }

        // Primera carga (una sola vez)
        listarProductos()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Evita fugas del Handler cuando se destruye la vista
        searchRunnable?.let { handler.removeCallbacks(it) }
        _binding = null
    }

    /** Página siguiente (por clave) */
    private fun cargarPaginaSiguiente() {
        listarProductos()
        primeraPagina = false
        binding.btnAnterior.isEnabled = true
    }

    /** Página anterior (por clave) */
    private fun cargarPaginaAnterior() {
        val first = primerProductoVisible ?: return
        cargandoDatos = true

        val ref = FirebaseDatabase.getInstance().getReference("Productos")
        val query = ref.orderByKey()
            .endBefore(first.key)
            .limitToLast(cantidadProductos)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productoArrayList.clear()
                if (snapshot.hasChildren()) {
                    primerProductoVisible = snapshot.children.first()
                    ultimoProductoVisible = snapshot.children.last()
                    for (ds in snapshot.children) {
                        ds.getValue(ModeloProducto::class.java)?.let { productoArrayList.add(it) }
                    }
                    adaptadorProductos.notifyDataSetChanged()
                    comprobarPrimeraPagina()
                }
                cargandoDatos = false
            }
            override fun onCancelled(error: DatabaseError) {
                cargandoDatos = false
            }
        })
    }

    /** Revisa si estamos en la primera página para deshabilitar "Anterior". */
    private fun comprobarPrimeraPagina() {
        FirebaseDatabase.getInstance().getReference("Productos")
            .orderByKey()
            .limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val primerProdBD = snapshot.children.first()
                        primeraPagina = primerProductoVisible?.key == primerProdBD.key
                        binding.btnAnterior.isEnabled = !primeraPagina
                        binding.btnSiguiente.isEnabled = true
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    // no-op
                }
            })
    }

    /** Búsqueda por nombre (prefijo). */
    private fun buscarProducto(nombreProducto: String) {
        val ref = FirebaseDatabase.getInstance()
            .getReference("Productos")
            .orderByChild("nombre")
            .startAt(nombreProducto)
            .endAt(nombreProducto + "\uf8ff")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productoArrayList.clear()
                if (snapshot.exists()) {
                    for (ds in snapshot.children) {
                        ds.getValue(ModeloProducto::class.java)?.let { productoArrayList.add(it) }
                    }
                }
                adaptadorProductos.notifyDataSetChanged()

                // En modo búsqueda, desactiva paginación para evitar confusión
                binding.btnAnterior.isEnabled = false
                binding.btnSiguiente.isEnabled = false
            }
            override fun onCancelled(error: DatabaseError) {
                // no-op
            }
        })
    }

    /** Lista productos con paginación por clave. */
    private fun listarProductos() {
        cargandoDatos = true

        val ref = FirebaseDatabase.getInstance().getReference("Productos")
        val query: Query = if (ultimoProductoVisible != null) {
            // Siguientes
            primeraPagina = false
            ref.orderByKey()
                .startAfter(ultimoProductoVisible!!.key)
                .limitToFirst(cantidadProductos)
        } else {
            // Primera página
            primeraPagina = true
            ref.orderByKey().limitToFirst(cantidadProductos)
        }

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productoArrayList.clear()
                if (snapshot.hasChildren()) {
                    primerProductoVisible = snapshot.children.first()
                    ultimoProductoVisible = snapshot.children.last()

                    for (ds in snapshot.children) {
                        ds.getValue(ModeloProducto::class.java)?.let { productoArrayList.add(it) }
                    }
                    adaptadorProductos.notifyDataSetChanged()

                    binding.btnAnterior.isEnabled = !primeraPagina
                    binding.btnSiguiente.isEnabled = snapshot.childrenCount.toInt() == cantidadProductos
                } else {
                    // No hay más productos
                    binding.btnSiguiente.isEnabled = false
                }
                cargandoDatos = false
            }
            override fun onCancelled(error: DatabaseError) {
                cargandoDatos = false
            }
        })
    }
}