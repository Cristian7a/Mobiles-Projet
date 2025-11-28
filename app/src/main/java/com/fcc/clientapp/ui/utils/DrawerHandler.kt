package com.fcc.clientapp.utils

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.fcc.clientapp.FoodBusinessDashboardActivity

object DrawerHandler {

    fun onNavigate(activity: Activity, route: String, businessName: String) {
        val currentRoute = getCurrentRoute(activity)
        if (currentRoute == route) return

        when (route) {
            "dashboard" -> {
                if (activity !is FoodBusinessDashboardActivity) {
                    val intent = Intent(activity, FoodBusinessDashboardActivity::class.java)
                    intent.putExtra("BUSINESS_NAME", businessName)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    activity.startActivity(intent)
                }
            }
            "categories" -> {
                Toast.makeText(activity, "Categorías: Próximamente", Toast.LENGTH_SHORT).show()
                // val intent = Intent(activity, FoodCategoriesActivity::class.java)
                // ...
            }
            "items" -> {
                Toast.makeText(activity, "Productos: Próximamente", Toast.LENGTH_SHORT).show()
            }
            "profile" -> {
                Toast.makeText(activity, "Perfil del Negocio: Próximamente", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCurrentRoute(activity: Activity): String {
        return when (activity) {
            is FoodBusinessDashboardActivity -> "dashboard"
            // is FoodCategoriesActivity -> "categories"
            // is FoodItemsActivity -> "items"
            // is FoodBusinessProfileActivity -> "profile"
            else -> ""
        }
    }
}