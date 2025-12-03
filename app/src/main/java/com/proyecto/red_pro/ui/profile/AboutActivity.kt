package com.proyecto.red_pro.ui.profile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.proyecto.red_pro.R

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        // Vincular el toolbar del layout como ActionBar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Activar botón de retroceso
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
        // Cambiar color de la flecha
        toolbar.navigationIcon?.setTint(getColor(R.color.white))
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
