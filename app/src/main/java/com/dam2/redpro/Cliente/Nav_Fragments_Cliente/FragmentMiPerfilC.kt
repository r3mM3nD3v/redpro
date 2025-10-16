package com.dam2.redpro.Cliente.Nav_Fragments_Cliente

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.dam2.redpro.Constantes
import com.dam2.redpro.Mapas.SeleccionarUbicacionActivity
import com.dam2.redpro.R
import com.dam2.redpro.databinding.FragmentMiPerfilCBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

/**
 * Mi Perfil (Cliente)
 * - Edita nombre/email/dni y dirección (con selector de mapa).
 * - Actualiza foto de perfil en Storage y URL en RTDB.
 * - Oculta proveedor de autenticación (email/google) por política.
 */
class FragmentMiPerfilC : Fragment() {

    private lateinit var binding: FragmentMiPerfilCBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mContext: Context
    private var imagenUri: Uri? = null

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        leerInformacion()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMiPerfilCBinding.inflate(layoutInflater, container, false)


        // ❌ Ocultar proveedor de autenticación (email/google)
        binding.proveedorCPerfil.visibility = View.GONE

        // Cambiar foto
        binding.imgCPerfil.setOnClickListener { seleccionarImg() }

        // Seleccionar ubicación
        binding.ubicacion.setOnClickListener {
            val intent = Intent(mContext, SeleccionarUbicacionActivity::class.java)
            obtenerUbicacion_ARL.launch(intent)
        }

        // Guardar datos básicos (nombres/email/dni/dirección)
        binding.btnGuardarInfoC.setOnClickListener { actualizarInfo() }

        return binding.root
    }

    // Datos a actualizar
    private var latitud = 0.0
    private var longitud = 0.0
    private var direccion = ""

    /** Actualiza información básica del usuario en RTDB (sin password). */
    private fun actualizarInfo() {
        val uid = firebaseAuth.uid ?: return

        val nombres = binding.nombresCPerfil.text.toString().trim()
        val email = binding.emailCPerfil.text.toString().trim()
        val dni = binding.dniCPerfil.text.toString().trim()
        direccion = binding.ubicacion.text.toString().trim()

        val cambios = hashMapOf<String, Any>(
            "nombres" to nombres,
            "email" to email,
            "dni" to dni,
            "direccion" to direccion,
            "latitud" to "$latitud",
            "longitud" to "$longitud"
        )

        FirebaseDatabase.getInstance()
            .getReference("Usuarios")
            .child(uid)
            .updateChildren(cambios)
            .addOnSuccessListener {
                Toast.makeText(mContext, "Se actualizó la información", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(mContext, e.message ?: "Error desconocido", Toast.LENGTH_SHORT).show()
            }
    }

    /** Lee datos del perfil desde RTDB y los pinta en la UI. */
    private fun leerInformacion() {
        val uid = firebaseAuth.uid ?: return
        FirebaseDatabase.getInstance()
            .getReference("Usuarios")
            .child(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombres = "${snapshot.child("nombres").value}"
                    val email = "${snapshot.child("email").value}"
                    val dni = "${snapshot.child("dni").value}"
                    val imagen = "${snapshot.child("imagen").value}"
                    val fechaRegistro = "${snapshot.child("tRegistro").value}"
                    val direccionDb = "${snapshot.child("direccion").value}"

                    val fecha = runCatching { Constantes().obtenerFecha(fechaRegistro.toLong()) }
                        .getOrDefault("")

                    binding.nombresCPerfil.setText(nombres)
                    binding.emailCPerfil.setText(email)
                    binding.dniCPerfil.setText(dni)
                    binding.fechaRegistroCPerfil.text = "Se unió el $fecha"
                    binding.ubicacion.setText(direccionDb)

                    try {
                        Glide.with(mContext)
                            .load(if (imagen.isBlank()) null else imagen)
                            .placeholder(R.drawable.img_perfil)
                            .error(R.drawable.img_perfil)
                            .into(binding.imgCPerfil)
                    } catch (_: Exception) {}
                }

                override fun onCancelled(error: DatabaseError) {
                    // No crashear la app si se cancela la lectura
                }
            })
    }

    /** Lanzador del picker de imágenes. */
    private fun seleccionarImg() {
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .createIntent { intent -> resultadoImg.launch(intent) }
    }

    private val resultadoImg =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
            if (resultado.resultCode == Activity.RESULT_OK) {
                imagenUri = resultado.data?.data
                subirImagenStorage(imagenUri)
            } else {
                Toast.makeText(mContext, "Acción cancelada", Toast.LENGTH_SHORT).show()
            }
        }

    /** Sube la imagen a Storage en /imagenesPerfil/{uid} y actualiza la URL en RTDB. */
    private fun subirImagenStorage(imagenUri: Uri?) {
        val uid = firebaseAuth.uid ?: return
        if (imagenUri == null) return

        val rutaImagen = "imagenesPerfil/$uid"
        val ref = FirebaseStorage.getInstance().getReference(rutaImagen)
        ref.putFile(imagenUri)
            .addOnSuccessListener { taskSnapShot ->
                taskSnapShot.storage.downloadUrl
                    .addOnSuccessListener { url ->
                        actualizarImagenBD(url.toString())
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(mContext, e.message ?: "No se pudo obtener URL", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(mContext, e.message ?: "No se pudo subir la imagen", Toast.LENGTH_SHORT).show()
            }
    }

    /** Guarda la URL de la imagen en RTDB. */
    private fun actualizarImagenBD(urlImagenCargada: String) {
        val uid = firebaseAuth.uid ?: return
        val cambios = hashMapOf<String, Any>("imagen" to urlImagenCargada)

        FirebaseDatabase.getInstance()
            .getReference("Usuarios")
            .child(uid)
            .updateChildren(cambios)
            .addOnSuccessListener {
                Toast.makeText(mContext, "Su imagen de perfil se ha actualizado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(mContext, e.message ?: "No se pudo actualizar la imagen", Toast.LENGTH_SHORT).show()
            }
    }

    /** Recibe lat/long/dirección desde SeleccionarUbicacionActivity. */
    private val obtenerUbicacion_ARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
            if (resultado.resultCode == Activity.RESULT_OK) {
                resultado.data?.let { data ->
                    latitud = data.getDoubleExtra("latitud", 0.0)
                    longitud = data.getDoubleExtra("longitud", 0.0)
                    direccion = data.getStringExtra("direccion") ?: ""
                    binding.ubicacion.setText(direccion)
                }
            }
        }
}