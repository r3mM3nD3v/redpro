package com.proyecto.red_pro.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.proyecto.red_pro.ui.auth.LoginActivity
import com.proyecto.red_pro.ui.cliente.ClienteHomeActivity
import com.proyecto.red_pro.ui.profesional.ProfesionalHomeActivity
import com.proyecto.red_pro.util.Prefs

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = Prefs(this)
        val user = Firebase.auth.currentUser
        val go = when {
            user != null && prefs.remember && prefs.rol == "cliente" -> ClienteHomeActivity::class.java
            user != null && prefs.remember && prefs.rol == "profesional" -> ProfesionalHomeActivity::class.java
            else -> LoginActivity::class.java
        }
        startActivity(Intent(this, go))
        finish()
    }
}