package com.dam2.redpro.Vendedor

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dam2.redpro.Constantes
import com.dam2.redpro.databinding.ActivityRegistroVendedorBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

/**
 * Registro de Vendedor
 * - Solo email + password
 */
class RegistroVendedorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroVendedorBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroVendedorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this).apply {
            setTitle("Espere por favor")
            setCanceledOnTouchOutside(false)
        }

        binding.btnRegistrarV.setOnClickListener { validarInformacion() }
    }

    private fun validarInformacion() {
        val nombresTxt = binding.etNombresV.text.toString().trim()
        val emailTxt   = binding.etEmail.text.toString().trim()
        val passTxt    = binding.etPassword.text.toString().trim()
        val cpassTxt   = binding.etCPassword.text.toString().trim()

        when {
            nombresTxt.isEmpty() -> {
                binding.etNombresV.error = "Ingrese sus nombres"; binding.etNombresV.requestFocus()
            }
            emailTxt.isEmpty() -> {
                binding.etEmail.error = "Ingrese email"; binding.etEmail.requestFocus()
            }
            !Patterns.EMAIL_ADDRESS.matcher(emailTxt).matches() -> {
                binding.etEmail.error = "Email no válido"; binding.etEmail.requestFocus()
            }
            passTxt.isEmpty() -> {
                binding.etPassword.error = "Ingrese password"; binding.etPassword.requestFocus()
            }
            passTxt.length < 6 -> {
                binding.etPassword.error = "Necesita 6 o más car."; binding.etPassword.requestFocus()
            }
            cpassTxt.isEmpty() -> {
                binding.etCPassword.error = "Confirme password"; binding.etCPassword.requestFocus()
            }
            passTxt != cpassTxt -> {
                binding.etCPassword.error = "No coincide"; binding.etCPassword.requestFocus()
            }
            else -> registrarVendedor(emailTxt, passTxt, nombresTxt)
        }
    }

    private fun registrarVendedor(email: String, password: String, nombres: String) {
        progressDialog.setMessage("Creando cuenta")
        progressDialog.show()

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                insertarInfoBD(nombres, email)
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Falló el registro debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /** Inserta el documento del vendedor en /Usuarios con claves consistentes. */
    private fun insertarInfoBD(nombres: String, email: String) {
        progressDialog.setMessage("Guardando información...")

        val uid = firebaseAuth.uid ?: return
        val tiempoRegistro = Constantes().obtenerTiempoD()

        val datosVendedor = hashMapOf<String, Any>(
            "uid"          to uid,
            "nombres"      to nombres,
            "email"        to email,
            "tipoUsuario"  to "vendedor",
            // Usa la MISMA clave que el resto de la app (cliente): tRegistro
            "tRegistro"    to tiempoRegistro,
            // Uniformidad con pantallas que leen 'proveedor'
            "proveedor"    to "email",
            // Campos opcionales que otras vistas podrían leer
            "imagen"       to "",
            "dni"          to "",
            "direccion"    to ""
        )

        FirebaseDatabase.getInstance()
            .getReference("Usuarios")
            .child(uid)
            .setValue(datosVendedor)
            .addOnSuccessListener {
                progressDialog.dismiss()
                startActivity(Intent(this, MainActivityVendedor::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Falló el registro en BD debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}