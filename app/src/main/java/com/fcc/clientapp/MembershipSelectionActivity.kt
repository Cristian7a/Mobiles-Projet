package com.fcc.clientapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.fcc.clientapp.model.UserMembership
import com.fcc.clientapp.network.RetrofitClient
import com.fcc.clientapp.ui.MembershipSelectionScreen
import com.fcc.clientapp.utils.MenuHandler
import com.fcc.clientapp.utils.SessionManager
import kotlinx.coroutines.launch
import android.content.Intent // <--- ESTE ERA EL QUE FALTABA

class MembershipSelectionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Asegurarnos de cargar los datos de disco a memoria al entrar
        SessionManager.loadFromPreferences(this)

        setContent {
            // 2. SUSCRIPCIÓN REACTIVA
            // 'session' se actualizará automáticamente si ProfileActivity cambia algo
            val session by SessionManager.sessionState.collectAsState()

            // Si por alguna razón el flow está vacío pero tenemos token, usamos el de disco
            val token = session?.token ?: SessionManager.getToken(this) ?: ""

            // Esta imagen cambiará en tiempo real
            val userImage = session?.imageUrl

            var memberships by remember { mutableStateOf<List<UserMembership>>(emptyList()) }
            var isLoading by remember { mutableStateOf(true) }
            var errorState by remember { mutableStateOf<String?>(null) }

            // Carga de datos inicial
            LaunchedEffect(Unit) {
                if (token.isNotEmpty()) {
                    try {
                        val response = RetrofitClient.instance.getUserMemberships("Bearer $token")
                        if (response.isSuccessful) {
                            memberships = response.body()?.memberships ?: emptyList()
                        } else {
                            if (response.code() == 401) MenuHandler.onOptionSelected(this@MembershipSelectionActivity, "Logout")
                            else errorState = "Error cargando datos."
                        }
                    } catch (e: Exception) {
                        errorState = "Error de conexión."
                    } finally {
                        isLoading = false
                    }
                } else {
                    MenuHandler.onOptionSelected(this@MembershipSelectionActivity, "Logout")
                }
            }

            MembershipSelectionScreen(
                memberships = memberships,
                isLoading = isLoading,
                errorState = errorState,
                // AQUÍ PASAS LA VARIABLE REACTIVA
                userImageUrl = userImage,
                onMembershipClick = { selected ->
                    // Lógica simple de nombre
                    val name = selected.extras?.business?.name
                        ?: selected.extras?.profile?.displayName
                        ?: selected.membership.name
                    val logoUrl = selected.extras?.business?.logoUrl // <--- OBTENER URL

                    // OBTENER EL ID CORRECTO SEGÚN EL TIPO
                    val foodOrgId = selected.extras?.foodOrganizationId

                    val intent = Intent(this@MembershipSelectionActivity, FoodBusinessDashboardActivity::class.java)
                    intent.putExtra("BUSINESS_NAME", name)
                    intent.putExtra("BUSINESS_LOGO", logoUrl)
                    intent.putExtra("FOOD_ORG_ID", foodOrgId) // <--- NUEVO: Enviar ID
                    startActivity(intent)

                },
                onMenuOptionClick = { option ->
                    MenuHandler.onOptionSelected(this@MembershipSelectionActivity, option)
                }
            )
        }
    }

    // Tip Pro: Si quieres que se recargue la info de usuario al volver a esta pantalla
    // (por si acaso se borró de memoria), puedes forzarlo en onResume.
    override fun onResume() {
        super.onResume()
        SessionManager.loadFromPreferences(this)
    }
}