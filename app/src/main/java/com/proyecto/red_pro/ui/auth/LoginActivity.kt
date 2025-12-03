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
import android.text.InputType
import com.proyecto.red_pro.R


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
            if (rol.isNotEmpty()) {
                goByRole(rol)
                return
            }
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
                    val firebaseUser = res.user!!
                    val uid = firebaseUser.uid
                    val emailFromAuth = firebaseUser.email ?: email

                    val usersRef = Firebase.firestore.collection("users").document(uid)

                    usersRef.get()
                        .addOnSuccessListener { doc ->

                            // Si NO existe el perfil en Firestore, lo creamos automáticamente
                            if (!doc.exists()) {
                                val newUser = hashMapOf(
                                    "id" to uid,
                                    "nombre" to "",
                                    "email" to emailFromAuth,
                                    "rol" to "cliente"
                                )

                                usersRef.set(newUser)
                                    .addOnSuccessListener {
                                        prefs.remember = b.cbRemember?.isChecked == true
                                        prefs.rol = "cliente"
                                        goByRole("cliente")
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            this,
                                            e.message ?: "No se pudo crear tu perfil",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        // Por seguridad, cerramos sesión si ni siquiera podemos crear el doc
                                        Firebase.auth.signOut()
                                    }
                                return@addOnSuccessListener
                            }

                            // 🔹 Si el documento SÍ existe, seguimos como antes
                            val rol = doc.getString("rol") ?: "cliente"
                            prefs.remember = b.cbRemember?.isChecked == true
                            prefs.rol = rol
                            goByRole(rol)
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "No se pudo leer el perfil", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, e.message ?: "Error de inicio de sesión", Toast.LENGTH_SHORT).show()
                }
        }

        //--Implementación del ojito en la contraseña
        var mostrar = false

        b.ivTogglePass.setOnClickListener {
            mostrar = !mostrar

            if (mostrar) {
                b.etPass.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                b.ivTogglePass.setImageResource(R.drawable.ic_eye)
            } else {
                b.etPass.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                b.ivTogglePass.setImageResource(R.drawable.ic_eye_off)
            }

            b.etPass.setSelection(b.etPass.text.length)
        }

        //-------



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