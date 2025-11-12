package com.proyecto.red_pro.ui.profile

import android.app.AlertDialog
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.proyecto.red_pro.databinding.ActivityProfileBinding
import com.proyecto.red_pro.util.LocalImages
import java.io.File

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    private var pendingLocalUri: Uri? = null
    private lateinit var cameraUri: Uri

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            pendingLocalUri = uri
            binding.ivProfile.setImageURI(uri)
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingLocalUri = cameraUri
            binding.ivProfile.setImageURI(cameraUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadProfile()

        binding.btnPickImage.setOnClickListener { showImagePickerSheet() }
        binding.btnSave.setOnClickListener { saveProfile() }
        binding.btnDeleteAccount.setOnClickListener { deleteAccountFlow() }
    }

    private fun loadProfile() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { snap ->
                binding.etName.setText(snap.getString("nombre") ?: "")
                binding.etPhone.setText(snap.getString("telefono") ?: "")
                binding.etAddress.setText(snap.getString("direccion") ?: "")
                binding.etAbout.setText(snap.getString("acerca") ?: "")

                val path = snap.getString("photoPath")
                if (!path.isNullOrEmpty()) {
                    LocalImages.loadPathInto(path, binding.ivProfile)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "No se pudo cargar el perfil", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showImagePickerSheet() {
        val items = arrayOf("Galería", "Cámara")
        AlertDialog.Builder(this)
            .setTitle("Seleccionar imagen")
            .setItems(items) { _, which -> if (which == 0) requestGallery() else requestCamera() }
            .show()
    }

    private fun requestGallery() {
        if (Build.VERSION.SDK_INT >= 33) {
            pickImageLauncher.launch("image/*")
        } else {
            pickImageLauncher.launch("image/*")
        }
    }

    private fun requestCamera() {
        val dir = File(cacheDir, "camera").apply { if (!exists()) mkdirs() }
        val photo = File(dir, "profile_${System.currentTimeMillis()}.jpg")
        cameraUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photo)
        takePictureLauncher.launch(cameraUri)
    }

    private fun saveProfile() {
        val uid = auth.currentUser?.uid ?: return
        val data = hashMapOf<String, Any>(
            "nombre"    to binding.etName.text.toString().trim(),
            "telefono"  to binding.etPhone.text.toString().trim(),
            "direccion" to binding.etAddress.text.toString().trim(),
            "acerca"    to binding.etAbout.text.toString().trim(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        val picked = pendingLocalUri
        if (picked != null) {
            // Copia la imagen al almacenamiento interno estable de la app
            val destDir = File(filesDir, "images/users/$uid")
            val dest = LocalImages.copyFromContentUri(
                contentResolver, picked, destDir, "profile.jpg"
            )
            if (dest != null) {
                data["photoPath"] = dest.absolutePath      // ✅ guardamos ruta local
            }
        }

        persistUser(uid, data)
    }

    private fun persistUser(uid: String, data: Map<String, Any>) {
        db.collection("users").document(uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                pendingLocalUri = null
            }
            .addOnFailureListener {
                Toast.makeText(this, "No se pudo guardar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteAccountFlow() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar cuenta")
            .setMessage("¿Seguro que deseas eliminar tu cuenta? Esta acción es irreversible.")
            .setPositiveButton("Eliminar") { _, _ -> deleteAccount() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteAccount() {
        val user = auth.currentUser ?: return
        val uid = user.uid

        db.collection("users").document(uid).delete()
            .addOnSuccessListener {
                // Borra archivo local del perfil (best effort)
                runCatching {
                    File(filesDir, "images/users/$uid/profile.jpg").delete()
                }
                user.delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Cuenta eliminada", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Reinicia sesión para eliminar: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "No se pudo eliminar el perfil", Toast.LENGTH_SHORT).show()
            }
    }
}