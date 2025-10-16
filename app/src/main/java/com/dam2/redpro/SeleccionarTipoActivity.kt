package com.dam2.redpro

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dam2.redpro.Cliente.LoginClienteActivity
import com.dam2.redpro.Vendedor.LoginVendedorActivity
import com.dam2.redpro.databinding.ActivitySeleccionarTipoBinding

/**
 * Activity principal que permite seleccionar el tipo de usuario
 * al momento de iniciar la aplicación.
 *
 * Desde aquí, el usuario puede elegir si desea acceder como:
 *  - Vendedor
 *  - Cliente
 *
 * Esta pantalla actúa como punto de entrada común antes de los
 * procesos de autenticación.
 */
class SeleccionarTipoActivity : AppCompatActivity() {

    // ViewBinding para acceder a los elementos del layout sin usar findViewById
    private lateinit var binding: ActivitySeleccionarTipoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Infla el layout asociado a esta Activity
        binding = ActivitySeleccionarTipoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /**
         * Botón de selección para iniciar sesión como Vendedor.
         * Redirige al Login específico para vendedores.
         */
        binding.tipoVendedor.setOnClickListener {
            startActivity(Intent(this@SeleccionarTipoActivity, LoginVendedorActivity::class.java))
            // Opcional: finish() para evitar volver a esta pantalla con "atrás"
        }

        /**
         * Botón de selección para iniciar sesión como Cliente.
         * Redirige al Login específico para clientes.
         */
        binding.tipoCliente.setOnClickListener {
            startActivity(Intent(this@SeleccionarTipoActivity, LoginClienteActivity::class.java))
            // Opcional: finish() para evitar volver a esta pantalla con "atrás"
        }
    }
}