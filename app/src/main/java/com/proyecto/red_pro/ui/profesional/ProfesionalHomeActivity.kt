package com.proyecto.red_pro.ui.profesional

import android.content.Intent
import android.os.Bundle
// Importado para el encabezado
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
// Importado para el encabezado
import com.google.firebase.firestore.FirebaseFirestore
import com.proyecto.red_pro.R
import com.proyecto.red_pro.databinding.ActivityProfesionalHomeBinding
import com.proyecto.red_pro.ui.profile.ProfileActivity
import com.proyecto.red_pro.util.Prefs
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatDelegate
import android.content.SharedPreferences

class ProfesionalHomeActivity : AppCompatActivity() {

    private lateinit var b: ActivityProfesionalHomeBinding
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityProfesionalHomeBinding.inflate(layoutInflater)
        setContentView(b.root)

        // Toolbar
        val toolbar = b.toolbar.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // DrawerLayout
        drawerLayout = b.drawerLayout

        // Botón hamburguesa
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.open,
            R.string.close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // NavigationView (menú)
        val navView = b.navView

        // Lógica para el encabezado
        // ----------------------------------------------------------------
        val headerView = navView.getHeaderView(0)
        val tvHeaderTitle = headerView.findViewById<TextView>(R.id.header_title)

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    val nombre = doc.getString("nombre") ?: "Profesional"
                    tvHeaderTitle.text = "Hola, $nombre" // Saludo personalizado
                }
                .addOnFailureListener {
                    tvHeaderTitle.text = "Panel de Gestión" // Mensaje de fallback
                }
        } else {
            tvHeaderTitle.text = "Bienvenidos a Red-Pro"
        }
        // ----------------------------------------------------------------

        // La lógica del modo oscuro
        val settings = getSharedPreferences("app_settings", MODE_PRIVATE)
        val isDark = settings.getBoolean("dark_mode", false)
        navView.menu.findItem(R.id.action_dark_mode)?.isChecked = isDark

        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
        // ----------------------------------------------------------------


        // 3. Configurar el Listener
        navView.setNavigationItemSelectedListener { item ->
            when(item.itemId) {
                R.id.action_profile -> startActivity(Intent(this, ProfileActivity::class.java))

                R.id.action_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    Prefs(this).remember = false
                    Prefs(this).rol = ""
                    finish()
                }

                R.id.action_dark_mode -> {
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
            .replace(R.id.container, MisServiciosFragment())
            .commit()

        // Botón flotante
        b.fabAdd.setOnClickListener {
            startActivity(Intent(this, ServicioEditActivity::class.java))
        }
    }
}