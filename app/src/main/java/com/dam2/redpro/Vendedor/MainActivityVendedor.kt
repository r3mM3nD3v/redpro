package com.dam2.redpro.Vendedor

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.dam2.redpro.R
import com.dam2.redpro.SeleccionarTipoActivity
import com.dam2.redpro.Vendedor.Bottom_Nav_Fragments_Vendedor.FragmentMisProductosV
import com.dam2.redpro.Vendedor.Bottom_Nav_Fragments_Vendedor.FragmentOrdenesV
import com.dam2.redpro.Vendedor.Nav_Fragments_Vendedor.FragmentCategoriasV
import com.dam2.redpro.Vendedor.Nav_Fragments_Vendedor.FragmentInicioV
import com.dam2.redpro.Vendedor.Nav_Fragments_Vendedor.FragmentMiTiendaV
import com.dam2.redpro.Vendedor.Nav_Fragments_Vendedor.FragmentProductosV
import com.dam2.redpro.databinding.ActivityMainVendedorBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

/**
 * Home del vendedor:
 * - Drawer con navegación a Inicio, Mi Tienda, Categorías, Productos, Mis Productos y Mis Órdenes.
 */
class MainActivityVendedor : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainVendedorBinding
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private var dobleClick = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainVendedorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        comprobarSesion()

        binding.navigationView.setNavigationItemSelectedListener(this)

        // Back: si el drawer está abierto, ciérralo; si no, doble tap para salir
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    return
                }
                if (dobleClick) {
                    finish()
                    return
                }
                dobleClick = true
                Toast.makeText(this@MainActivityVendedor, "Presione nuevamente para salir", Toast.LENGTH_SHORT).show()
                handler.postDelayed({ dobleClick = false }, 2000)
            }
        })

        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, toolbar,
            R.string.open_drawer, R.string.close_drawer
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        replaceFragment(FragmentInicioV())
        binding.navigationView.setCheckedItem(R.id.op_inicio_v)
    }

    /** Si no hay usuario autenticado, volver a SeleccionarTipo y limpiar back stack. */
    private fun comprobarSesion() {
        if (firebaseAuth.currentUser == null) {
            startActivity(Intent(this, SeleccionarTipoActivity::class.java))
            finishAffinity()
        } else {
            Toast.makeText(this, "Usuario en línea", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cerrarSesion() {
        firebaseAuth.signOut()
        startActivity(Intent(this, SeleccionarTipoActivity::class.java))
        finishAffinity()
        Toast.makeText(this, "Has cerrado sesión", Toast.LENGTH_SHORT).show()
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.navFragment, fragment)
            .commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.op_inicio_v -> replaceFragment(FragmentInicioV())
            R.id.op_mi_tienda_v -> replaceFragment(FragmentMiTiendaV())
            R.id.op_categorias_v -> replaceFragment(FragmentCategoriasV())
            R.id.op_productos_v -> replaceFragment(FragmentProductosV())
            R.id.op_mis_productos_v -> replaceFragment(FragmentMisProductosV())
            R.id.op_mis_ordenes_v -> replaceFragment(FragmentOrdenesV())
            R.id.op_cerrar_sesion_v -> cerrarSesion()
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
