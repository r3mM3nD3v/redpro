package com.proyecto.red_pro.ui.profesional

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.proyecto.red_pro.R
import com.proyecto.red_pro.databinding.ActivityProfesionalHomeBinding
import com.proyecto.red_pro.ui.profile.ProfileActivity
import com.proyecto.red_pro.util.Prefs
import androidx.appcompat.app.ActionBarDrawerToggle

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
        navView.setNavigationItemSelectedListener { item ->
            when(item.itemId) {
                R.id.action_profile -> startActivity(Intent(this, ProfileActivity::class.java))
                R.id.action_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    Prefs(this).remember = false
                    Prefs(this).rol = ""
                    finish()
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
