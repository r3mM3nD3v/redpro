package com.proyecto.red_pro.util

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("prefs_redpro", Context.MODE_PRIVATE)

    var rol: String
        get() = prefs.getString("rol", "") ?: ""
        set(value) = prefs.edit().putString("rol", value).apply()

    var remember: Boolean
        get() = prefs.getBoolean("remember", false)
        set(value) = prefs.edit().putBoolean("remember", value).apply()
}