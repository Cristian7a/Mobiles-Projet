package com.fcc.clientapp.utils

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.fcc.clientapp.HistoryActivity
import com.fcc.clientapp.MainActivity
import com.fcc.clientapp.ProfileActivity

object MenuHandler {

    fun onOptionSelected(activity: Activity, option: String) {
        when (option) {
            "Profile" -> {
                if (activity !is ProfileActivity) {
                    val intent = Intent(activity, ProfileActivity::class.java)
                    activity.startActivity(intent)
                }
            }
            "Explorar" -> {
                Toast.makeText(activity, "Explorar: Próximamente", Toast.LENGTH_SHORT).show()
            }
            "Historial" -> {
                if (activity !is HistoryActivity) {
                    val intent = Intent(activity, HistoryActivity::class.java)
                    activity.startActivity(intent)
                }
            }
            "Logout" -> {
                performLogout(activity)
            }
        }
    }

    private fun performLogout(activity: Activity) {
        // 1. LIMPIEZA DE SESIÓN CENTRALIZADA
        SessionManager.clearSession(activity)

        // 2. Navegación al Login
        val intent = Intent(activity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity.startActivity(intent)
        activity.finish()
    }
}