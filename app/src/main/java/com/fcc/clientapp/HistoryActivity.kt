package com.fcc.clientapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.fcc.clientapp.model.PastOrder
import com.fcc.clientapp.network.RetrofitClient
import com.fcc.clientapp.ui.HistoryScreen
import com.fcc.clientapp.utils.MenuHandler
import com.fcc.clientapp.utils.SessionManager // <--- Singleton
import kotlinx.coroutines.launch

class HistoryActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Obtener datos desde SessionManager
        val token = SessionManager.getToken(this) ?: ""
        val userImage = SessionManager.getUserImage(this)

        setContent {
            var orders by remember { mutableStateOf<List<PastOrder>>(emptyList()) }
            var isLoading by remember { mutableStateOf(true) }
            var errorState by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(Unit) {
                if (token.isNotEmpty()) {
                    try {
                        val response = RetrofitClient.instance.getPastOrders("Bearer $token")
                        if (response.isSuccessful && response.body() != null) {
                            orders = response.body()!!.foodOrders
                        } else {
                            if (response.code() == 401) {
                                // Logout centralizado
                                MenuHandler.onOptionSelected(this@HistoryActivity, "Logout")
                            } else {
                                errorState = "Error al cargar historial."
                            }
                        }
                    } catch (e: Exception) {
                        errorState = "Error de conexión."
                    } finally {
                        isLoading = false
                    }
                } else {
                    MenuHandler.onOptionSelected(this@HistoryActivity, "Logout")
                }
            }

            HistoryScreen(
                orders = orders,
                isLoading = isLoading,
                errorState = errorState,
                // Pasamos la imagen recuperada al Screen (para el Navbar)
                userImageUrl = userImage, // <--- Asegúrate de agregar este parámetro a HistoryScreen también
                onBackClick = { finish() },
                onMenuOptionClick = { option ->
                    MenuHandler.onOptionSelected(this@HistoryActivity, option)
                },
                onOrderClick = { orderId ->
                    val intent = Intent(this@HistoryActivity, PastOrderDetailActivity::class.java)
                    intent.putExtra("ORDER_ID", orderId)
                    startActivity(intent)
                }
            )
        }
    }
}