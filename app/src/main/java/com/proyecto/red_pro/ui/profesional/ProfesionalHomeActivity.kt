package com.proyecto.red_pro.ui.profesional

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.proyecto.red_pro.R
import com.proyecto.red_pro.databinding.ActivityProfesionalHomeBinding
import com.proyecto.red_pro.ui.profile.ProfileActivity
import com.proyecto.red_pro.util.Prefs

class ProfesionalHomeActivity : AppCompatActivity() {
    private lateinit var b: ActivityProfesionalHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityProfesionalHomeBinding.inflate(layoutInflater)
        setContentView(b.root)

        // Aquí sí se setea la toolbar principal
        setSupportActionBar(b.toolbar.toolbar)

        // Aquí sí se elimina el name de la app
        supportActionBar?.setDisplayShowTitleEnabled(false)


        supportFragmentManager.beginTransaction()
            .replace(R.id.container, MisServiciosFragment())
            .commit()

        b.fabAdd.setOnClickListener {
            startActivity(Intent(this, ServicioEditActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_profesional, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java));
                true }

            R.id.action_logout -> {
                Firebase.auth.signOut()
                Prefs(this).remember = false
                Prefs(this).rol = ""
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}