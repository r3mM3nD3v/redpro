package com.proyecto.red_pro.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.proyecto.red_pro.data.model.User
import com.proyecto.red_pro.databinding.ActivityRegisterBinding
import com.proyecto.red_pro.ui.cliente.ClienteHomeActivity
import com.proyecto.red_pro.ui.profesional.ProfesionalHomeActivity
import com.proyecto.red_pro.util.Prefs

class RegisterActivity : AppCompatActivity() {
    private lateinit var b: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.btnRegister.setOnClickListener {
            val nombre = b.etNombre.text.toString().trim()
            val email  = b.etEmail.text.toString().trim()
            val pass   = b.etPass.text.toString().trim()

            val selectedId = b.rgRol.checkedRadioButtonId
            val rol = findViewById<RadioButton>(selectedId)?.text?.toString()?.lowercase() ?: "cliente"

            if (nombre.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Firebase.auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener { res ->
                    val uid = res.user!!.uid
                    val u = User(uid, nombre, email, rol)
                    Firebase.firestore.collection("users").document(uid).set(u)
                        .addOnSuccessListener {
                            val prefs = Prefs(this)
                            prefs.rol = rol
                            prefs.remember = true

                            if (rol == "profesional")
                                startActivity(Intent(this, ProfesionalHomeActivity::class.java))
                            else
                                startActivity(Intent(this, ClienteHomeActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, e.message ?: "No se pudo guardar el usuario", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, e.message ?: "Error al registrar", Toast.LENGTH_SHORT).show()
                }
        }
    }
}