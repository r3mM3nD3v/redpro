package com.proyecto.red_pro.ui.cliente

import android.content.Intent
import android.os.Bundle
import android.widget.TextView // Importado para el encabezado
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore // Importado para el encabezado
import com.proyecto.red_pro.R
import com.proyecto.red_pro.databinding.ActivityClienteHomeBinding
import com.proyecto.red_pro.ui.profile.ProfileActivity
import com.proyecto.red_pro.ui.profile.AboutActivity
import com.proyecto.red_pro.util.Prefs
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatDelegate

class ClienteHomeActivity : AppCompatActivity() {

    private lateinit var b: ActivityClienteHomeBinding
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityClienteHomeBinding.inflate(layoutInflater)
        setContentView(b.root)

        // ... [Configuración de DrawerLayout y Toolbar] ...
        drawerLayout = findViewById(R.id.drawer_layout)
        val toolbar = b.toolbar
        setSupportActionBar(toolbar)
        toolbar.navigationIcon?.setTintList(null)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.open, R.string.close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Navigation View (menú)
        val navView = findViewById<NavigationView>(R.id.nav_view)


        // Lógica para el encabezado de clientes el nombre
        // ----------------------------------------------------------------
        val headerView = navView.getHeaderView(0)
        val tvHeaderTitle = headerView.findViewById<TextView>(R.id.header_title)

        val uid = Firebase.auth.currentUser?.uid
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    // Usa el nombre del usuario, si no existe, usa "Cliente"
                    val nombre = doc.getString("nombre") ?: "Cliente"
                    tvHeaderTitle.text = "Hola, $nombre" // Saludo personalizado
                }
                .addOnFailureListener {
                    tvHeaderTitle.text = "¡A Explorar!" // Mensaje de fallback
                }
        }
        // ----------------------------------------------------------------


        // 1. Cargar el estado inicial del modo oscuro
        val settings = getSharedPreferences("app_settings", MODE_PRIVATE)
        val isDark = settings.getBoolean("dark_mode", false)
        navView.menu.findItem(R.id.action_dark_mode)?.isChecked = isDark

        // Aplicar el modo oscuro al inicio (para que se cargue correctamente)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        // 2. Mover la lógica de clic al NavigationItemSelectedListener
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                }

                R.id.action_logout -> {
                    Firebase.auth.signOut()
                    Prefs(this).remember = false
                    Prefs(this).rol = ""
                    finish()
                }

                R.id.nav_about -> {
                    startActivity(Intent(this, AboutActivity::class.java))
                }

                R.id.action_dark_mode -> {
                    // LÓGICA DEL MODO OSCURO (ya implementada)
                    val currentIsDark = item.isChecked
                    val newIsDark = !currentIsDark

                    item.isChecked = newIsDark
                    settings.edit().putBoolean("dark_mode", newIsDark).apply()

                    AppCompatDelegate.setDefaultNightMode(
                        if (newIsDark) AppCompatDelegate.MODE_NIGHT_YES
                        else AppCompatDelegate.MODE_NIGHT_NO
                    )
                }
            }

            drawerLayout.closeDrawers()
            true
        }


        // Fragment principal
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, ExplorarFragment())
            .commit()

    }


}