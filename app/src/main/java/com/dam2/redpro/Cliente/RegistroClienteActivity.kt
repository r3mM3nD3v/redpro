package com.dam2.redpro.Cliente

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dam2.redpro.Constantes
import com.dam2.redpro.databinding.ActivityRegistroClienteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

/**
 * Registro de Cliente con email y contraseña.
 */
class RegistroClienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroClienteBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this).apply {
            setTitle("Espere por favor")
            setCanceledOnTouchOutside(false)
        }

        binding.btnRegistrarC.setOnClickListener { validarInformacion() }
    }

    private var nombres = ""
    private var email = ""
    private var password = ""
    private var cpassword = ""

    /** Valida campos básicos del formulario. */
    private fun validarInformacion() {
        nombres = binding.etNombresC.text.toString().trim()
        email = binding.etEmail.text.toString().trim()
        password = binding.etPassword.text.toString().trim()
        cpassword = binding.etCPassword.text.toString().trim()

        when {
            nombres.isEmpty() -> {
                binding.etNombresC.error = "Ingrese nombres"
                binding.etNombresC.requestFocus()
            }
            email.isEmpty() -> {
                binding.etEmail.error = "Ingrese email"
                binding.etEmail.requestFocus()
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.etEmail.error = "Email no válido"
                binding.etEmail.requestFocus()
            }
            password.isEmpty() -> {
                binding.etPassword.error = "Ingrese password"
                binding.etPassword.requestFocus()
            }
            password.length < 6 -> {
                binding.etPassword.error = "Necesita más de 6 caracteres"
                binding.etPassword.requestFocus()
            }
            cpassword.isEmpty() -> {
                binding.etCPassword.error = "Confirme password"
                binding.etCPassword.requestFocus()
            }
            password != cpassword -> {
                binding.etCPassword.error = "No coinciden los password"
                binding.etCPassword.requestFocus()
            }
            else -> registrarCliente()
        }
    }

    /** Crea la cuenta en Firebase Auth. */
    private fun registrarCliente() {
        progressDialog.setMessage("Creando cuenta")
        progressDialog.show()

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { insertarInfoBD() }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Falló el registro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /** Inserta datos mínimos del cliente en RTDB. */
    private fun insertarInfoBD() {
        progressDialog.setMessage("Guardando información")

        val uid = firebaseAuth.uid
        if (uid.isNullOrBlank()) {
            progressDialog.dismiss()
            Toast.makeText(this, "No se pudo obtener el UID", Toast.LENGTH_SHORT).show()
            return
        }

        val tiempoRegistro = Constantes().obtenerTiempoD()
        val datosCliente = hashMapOf(
            "uid" to uid,
            "nombres" to nombres,
            "email" to email,
            "dni" to "",
            "proveedor" to "email",
            "tRegistro" to "$tiempoRegistro",
            "imagen" to "",
            "tipoUsuario" to "cliente"
        )

        FirebaseDatabase.getInstance()
            .getReference("Usuarios")
            .child(uid)
            .setValue(datosCliente)
            .addOnSuccessListener {
                progressDialog.dismiss()
                startActivity(Intent(this@RegistroClienteActivity, MainActivityCliente::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Falló el registro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}