package com.dam2.redpro.Vendedor

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dam2.redpro.databinding.ActivityLoginVendedorBinding
import com.google.firebase.auth.FirebaseAuth

/**
 * Login de Vendedor
 */
class LoginVendedorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginVendedorBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginVendedorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this).apply {
            setTitle("Espere por favor")
            setCanceledOnTouchOutside(false)
        }

        // Acción: Iniciar sesión
        binding.btnLoginV.setOnClickListener { validarInfo() }

        // Navegar a registro de vendedor
        binding.tvRegistrarV.setOnClickListener {
            startActivity(Intent(applicationContext, RegistroVendedorActivity::class.java))
        }
    }

    private fun validarInfo() {
        val emailTxt = binding.etEmail.text.toString().trim()
        val passTxt  = binding.etPassword.text.toString().trim()

        when {
            emailTxt.isEmpty() -> {
                binding.etEmail.error = "Ingrese email"
                binding.etEmail.requestFocus()
            }
            !Patterns.EMAIL_ADDRESS.matcher(emailTxt).matches() -> {
                binding.etEmail.error = "Email no válido"
                binding.etEmail.requestFocus()
            }
            passTxt.isEmpty() -> {
                binding.etPassword.error = "Ingrese password"
                binding.etPassword.requestFocus()
            }
            else -> loginVendedor(emailTxt, passTxt)
        }
    }

    private fun loginVendedor(email: String, password: String) {
        progressDialog.setMessage("Ingresando")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                progressDialog.dismiss()
                startActivity(Intent(this, MainActivityVendedor::class.java))
                finishAffinity()
                Toast.makeText(this, "Bienvenido(a)", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "No se pudo iniciar sesión debido a ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}