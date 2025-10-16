package com.dam2.redpro

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import com.dam2.redpro.R
import com.dam2.redpro.Cliente.MainActivityCliente
import com.dam2.redpro.SeleccionarTipoActivity
import com.dam2.redpro.Vendedor.MainActivityVendedor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * Pantalla de bienvenida (Splash Screen)
 *
 * Esta Activity se muestra al abrir la aplicación.
 * Tiene tres propósitos principales:
 *  1. Mostrar la animación o imagen inicial (logo o presentación).
 *  2. Esperar unos segundos antes de ingresar a la app.
 *  3. Verificar si el usuario ya ha iniciado sesión
 *     y dirigirlo según su tipo de cuenta (cliente o vendedor).
 */
class SplashScreenActivity : AppCompatActivity() {

    // Autenticador de Firebase para comprobar la sesión del usuario
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Carga el diseño de la pantalla splash (activity_splash_screen.xml)
        setContentView(R.layout.activity_splash_screen)

        // Inicializa Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        // Inicia la cuenta regresiva de presentación
        verBienvenida()
    }

    /**
     * Muestra la pantalla de bienvenida durante 3 segundos (3000 ms).
     * Luego, llama al método que comprueba el tipo de usuario autenticado.
     */
    private fun verBienvenida() {
        object : CountDownTimer(3000, 1000) { // Cada tick es 1 segundo
            override fun onTick(millisUntilFinished: Long) {
                // Aquí podría añadirse una animación o contador visual si se desea
            }

            override fun onFinish() {
                // Al finalizar el tiempo, verificamos la sesión
                comprobarTipoUsuario()
            }
        }.start()
    }

    /**
     * Comprueba si hay un usuario autenticado en Firebase.
     *  - Si no hay sesión, redirige al selector de tipo de usuario.
     *  - Si hay sesión, consulta en la BD qué tipo de usuario es
     *    (vendedor o cliente) y abre la pantalla correspondiente.
     */
    private fun comprobarTipoUsuario() {
        val firebaseUser = firebaseAuth.currentUser

        // Si no hay usuario logueado, vuelve al selector inicial
        if (firebaseUser == null) {
            startActivity(Intent(this, SeleccionarTipoActivity::class.java))
        } else {
            // Si el usuario está logueado, verificamos su tipo en la base de datos
            val reference = FirebaseDatabase.getInstance().getReference("Usuarios")
            reference.child(firebaseUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {

                    // Se ejecuta cuando se recuperan los datos del usuario
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val tipoU = snapshot.child("tipoUsuario").value

                        // Según el tipo de usuario, se abre la Activity correspondiente
                        if (tipoU == "vendedor") {
                            startActivity(Intent(this@SplashScreenActivity, MainActivityVendedor::class.java))
                            finishAffinity() // Evita volver al splash con "atrás"
                        } else if (tipoU == "cliente") {
                            startActivity(Intent(this@SplashScreenActivity, MainActivityCliente::class.java))
                            finishAffinity()
                        }
                    }

                    // Se ejecuta si ocurre un error al leer la base de datos
                    override fun onCancelled(error: DatabaseError) {
                        // Aquí puede manejarse el error con logs o mensajes al usuario
                    }
                })
        }
    }
}