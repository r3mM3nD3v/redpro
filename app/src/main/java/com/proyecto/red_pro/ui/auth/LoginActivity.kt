package com.proyecto.red_pro.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.proyecto.red_pro.databinding.ActivityLoginBinding
import com.proyecto.red_pro.ui.cliente.ClienteHomeActivity
import com.proyecto.red_pro.ui.profesional.ProfesionalHomeActivity
import com.proyecto.red_pro.util.Prefs


class LoginActivity : AppCompatActivity() {
    private lateinit var b: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)

        val prefs = Prefs(this)
        b.cbRemember?.isChecked = prefs.remember

        val currentUser = Firebase.auth.currentUser
        if (currentUser != null && prefs.remember) {
            val rol = prefs.rol
            if (rol.isNotEmpty()) { goByRole(rol); return }
        }

        b.btnGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        b.btnLogin.setOnClickListener {
            val email = b.etEmail.text.toString().trim()
            val pass = b.etPass.text.toString().trim()
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Completa correo y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Firebase.auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener { res ->
                    val uid = res.user!!.uid
                    Firebase.firestore.collection("users").document(uid).get()
                        .addOnSuccessListener { doc ->
                            val rol = doc.getString("rol") ?: "cliente"
                            prefs.remember = b.cbRemember?.isChecked == true
                            prefs.rol = rol
                            goByRole(rol)
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "No se pudo leer el rol", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, e.message ?: "Error de inicio de sesión", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun goByRole(rol: String) {
        val intent = if (rol == "profesional")
            Intent(this, ProfesionalHomeActivity::class.java)
        else
            Intent(this, ClienteHomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}