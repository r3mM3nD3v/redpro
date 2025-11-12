package com.proyecto.red_pro.ui.cliente

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.proyecto.red_pro.R
import com.proyecto.red_pro.databinding.ActivityClienteHomeBinding
import com.proyecto.red_pro.ui.profile.ProfileActivity
import com.proyecto.red_pro.util.Prefs

class ClienteHomeActivity : AppCompatActivity() {
    private lateinit var b: ActivityClienteHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityClienteHomeBinding.inflate(layoutInflater)
        setContentView(b.root)

        setSupportActionBar(b.toolbar)

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, ExplorarFragment())
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_cliente, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
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