package com.proyecto.red_pro.ui.cliente

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.proyecto.red_pro.R
import com.proyecto.red_pro.databinding.ActivityClienteHomeBinding
import com.proyecto.red_pro.ui.profile.ProfileActivity
import com.proyecto.red_pro.ui.profile.AboutActivity
import com.proyecto.red_pro.util.Prefs
import androidx.appcompat.app.ActionBarDrawerToggle

class ClienteHomeActivity : AppCompatActivity() {

    private lateinit var b: ActivityClienteHomeBinding
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityClienteHomeBinding.inflate(layoutInflater)
        setContentView(b.root)

        // DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout)
        val toolbar = b.toolbar
        setSupportActionBar(toolbar)
        //Mantiene el color de los icon
        toolbar.navigationIcon?.setTintList(null)

        // Ocultar título
        supportActionBar?.setDisplayShowTitleEnabled(false)

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

        // Navigation View (menú)
        val navView = findViewById<NavigationView>(R.id.nav_view)
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
