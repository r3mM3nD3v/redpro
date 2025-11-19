package com.proyecto.red_pro.data.repo

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.proyecto.red_pro.data.model.Servicio
import com.proyecto.red_pro.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class FirestoreRepository(private val db: FirebaseFirestore) {

    private val users = db.collection("users")
    private val services = db.collection("services")

    // =========================
    // USERS
    // =========================

    /** Crea/actualiza el usuario fusionando campos  */
    fun upsertUserMerge(u: User): Task<Void> {
        require(u.uid.isNotEmpty()) { "uid requerido" }
        return users.document(u.uid).set(u, SetOptions.merge())
    }

    /** Crea el usuario pisando el doc  */
    fun createUserOverwrite(u: User): Task<Void> {
        require(u.uid.isNotEmpty()) { "uid requerido" }
        return users.document(u.uid).set(u)
    }

    /** Obtiene el doc de usuario (Task clásico, sin await). */
    fun getUser(uid: String): Task<DocumentSnapshot> =
        users.document(uid).get()

    /** Actualiza SOLO los campos enviados (merge). */
    fun updateUserFields(uid: String, data: Map<String, Any>): Task<Void> =
        users.document(uid).set(data, SetOptions.merge())

    // =========================
    // SERVICES
    // =========================

    /**
     * Crea / actualiza un servicio.
     * - Si s.id está vacío, genera un ID y lo pone en el doc.
     * - Si s.id no está vacío, usa ese ID (útil para mantener consistencia con fotos locales).
     */
    fun upsertServicio(s: Servicio): Task<Void> {
        val ref = if (s.id.isEmpty()) services.document() else services.document(s.id)
        val fixed = if (s.id.isEmpty()) s.copy(id = ref.id) else s
        return ref.set(fixed)
    }

    /** Borra un servicio por id. */
    fun deleteServicio(id: String) = services.document(id).delete()

    /** Obtiene un servicio puntual (por id). */
    fun getServicio(id: String): Task<DocumentSnapshot> = services.document(id).get()

    /**
     * Escucha SOLO mis servicios.
     * Se filtra por uidProfesional y se ordena en memoria por timestamp desc
     * para evitar necesitar un índice compuesto en Firestore.
     */
    fun listenMisServicios(uid: String) = callbackFlow<List<Servicio>> {
        val q = services
            .whereEqualTo("uidProfesional", uid)

        val reg = q.addSnapshotListener(MetadataChanges.INCLUDE) { snap, _ ->
            val list = snap?.toObjects(Servicio::class.java) ?: emptyList()
            // Ordenamos en memoria de más reciente a más antiguo
            trySend(list.sortedByDescending { it.timestamp })
        }
        awaitClose { reg.remove() }
    }

    /** Escucha todos los servicios (lectura pública) ordenados por timestamp desc. */
    fun listenServicios() = callbackFlow<List<Servicio>> {
        val q = services.orderBy("timestamp", Query.Direction.DESCENDING)
        val reg = q.addSnapshotListener(MetadataChanges.INCLUDE) { snap, _ ->
            val list = snap?.toObjects(Servicio::class.java) ?: emptyList()
            trySend(list)
        }
        awaitClose { reg.remove() }
    }

    /**
     * (Opcional) Añadir o reemplazar las rutas locales de fotos en un servicio existente.
     * Útil si quieres actualizar solo el arreglo photoPaths sin tocar otros campos.
     */
    fun updateServicioPhotoPaths(id: String, paths: List<String>): Task<Void> =
        services.document(id).set(mapOf("photoPaths" to paths), SetOptions.merge())
}