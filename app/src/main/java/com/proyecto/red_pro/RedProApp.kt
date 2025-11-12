package com.proyecto.red_pro

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class RedProApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // (Opcional) Ajustes globales de Firestore
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings
    }
}
