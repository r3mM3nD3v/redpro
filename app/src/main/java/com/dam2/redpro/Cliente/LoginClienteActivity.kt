package com.dam2.redpro.Cliente

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.dam2.redpro.Constantes
import com.dam2.redpro.R
import com.dam2.redpro.databinding.ActivityLoginClienteBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

/**
 * Login de Cliente:
 * - Email/Password y Google Sign-In.
 */
class LoginClienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginClienteBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this).apply {
            setTitle("Espere por favor")
            setCanceledOnTouchOutside(false)
        }

        // Configuración de Google Sign-In (usar el client_id de resources)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        // Login con email/password
        binding.btnLoginC.setOnClickListener { validarInfo() }

        // Login con Google
        binding.btnLoginGoogle.setOnClickListener { googleLogin() }

        // Registro
        binding.tvRegistrarC.setOnClickListener {
            startActivity(Intent(this@LoginClienteActivity, RegistroClienteActivity::class.java))
        }
    }

    private var email = ""
    private var password = ""
    private fun validarInfo() {
        email = binding.etEmail.text.toString().trim()
        password = binding.etPassword.text.toString().trim()

        when {
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.etEmail.error = "Email inválido"
                binding.etEmail.requestFocus()
            }
            email.isEmpty() -> {
                binding.etEmail.error = "Ingrese email"
                binding.etEmail.requestFocus()
            }
            password.isEmpty() -> {
                binding.etPassword.error = "Ingrese password"
                binding.etPassword.requestFocus()
            }
            else -> loginCliente()
        }
    }

    private fun loginCliente() {
        progressDialog.setMessage("Ingresando")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                progressDialog.dismiss()
                startActivity(Intent(this, MainActivityCliente::class.java))
                finishAffinity()
                Toast.makeText(this, "Bienvenido(a)", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "No se pudo iniciar sesión: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun googleLogin() {
        val googleSignInIntent = mGoogleSignInClient.signInIntent
        googleSignInARL.launch(googleSignInIntent)
    }

    private val googleSignInARL = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        if (resultado.resultCode == RESULT_OK) {
            val data = resultado.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val cuenta = task.getResult(ApiException::class.java)
                autenticacionGoogle(cuenta?.idToken)
            } catch (e: Exception) {
                Toast.makeText(this, e.message ?: "Error al iniciar con Google", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "La operación de logeo ha sido cancelada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun autenticacionGoogle(idToken: String?) {
        if (idToken.isNullOrBlank()) {
            Toast.makeText(this, "Token de Google inválido", Toast.LENGTH_SHORT).show()
            return
        }

        progressDialog.setMessage("Autenticando con Google")
        progressDialog.show()

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { resultadoAuth ->
                if (resultadoAuth.additionalUserInfo?.isNewUser == true) {
                    // Usuario nuevo: guardar datos básicos
                    llenarInfoBD()
                } else {
                    progressDialog.dismiss()
                    startActivity(Intent(this, MainActivityCliente::class.java))
                    finishAffinity()
                }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, e.message ?: "No se pudo autenticar con Google", Toast.LENGTH_SHORT).show()
            }
    }

    /** Guarda datos minimos del cliente nuevo en RTDB (sin teléfono ni OTP). */
    private fun llenarInfoBD() {
        progressDialog.setMessage("Guardando información")

        val uid = firebaseAuth.uid ?: run {
            progressDialog.dismiss()
            Toast.makeText(this, "UID no disponible", Toast.LENGTH_SHORT).show()
            return
        }

        val nombreC = firebaseAuth.currentUser?.displayName.orEmpty()
        val emailC = firebaseAuth.currentUser?.email.orEmpty()
        val tiempoRegistro = Constantes().obtenerTiempoD()

        val datosCliente = hashMapOf(
            "uid" to uid,
            "nombres" to nombreC,
            "email" to emailC,
            "telefono" to "",             // sin phone login/OTP
            "dni" to "",
            "proveedor" to "google",
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
                startActivity(Intent(this, MainActivityCliente::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, e.message ?: "No se pudo guardar información", Toast.LENGTH_SHORT).show()
            }
    }
}