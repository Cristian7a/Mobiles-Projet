package com.fcc.clientapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.fcc.clientapp.ui.FoodBusinessDashboardScreen
import com.fcc.clientapp.utils.DrawerHandler
import com.fcc.clientapp.utils.MenuHandler
import com.fcc.clientapp.utils.SessionManager

class FoodBusinessDashboardActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Obtener datos de sesión (Usuario)
        val userImage = SessionManager.getUserImage(this)

        // 2. Obtener datos del Negocio (Recibidos desde MembershipSelectionActivity)
        val businessName = intent.getStringExtra("BUSINESS_NAME") ?: "Mi Negocio"
        val businessLogo = intent.getStringExtra("BUSINESS_LOGO") // <--- RECUPERAR URL DEL LOGO
        val foodOrgId = intent.getStringExtra("FOOD_ORG_ID") ?: "" // <--- NUEVO: Recibir ID

        setContent {
            // 3. Renderizar Dashboard
            FoodBusinessDashboardScreen(
                foodOrgId = foodOrgId, // <--- Pasarlo a la UI
                businessName = businessName,
                businessLogoUrl = businessLogo, // <--- PASARLO AQUI (Esto arregla el error)
                userImageUrl = userImage,

                // Conectar Navbars
                onMenuOptionClick = { option ->
                    MenuHandler.onOptionSelected(this, option)
                },
                onNavigateDrawer = { route ->
                    DrawerHandler.onNavigate(this, route, businessName)
                }
            )
        }
    }
}