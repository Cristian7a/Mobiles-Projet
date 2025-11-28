package com.fcc.clientapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.fcc.clientapp.model.PastOrderDetail
import com.fcc.clientapp.network.RetrofitClient
import com.fcc.clientapp.ui.PastOrderDetailScreen
import com.fcc.clientapp.utils.MenuHandler
import com.fcc.clientapp.utils.SessionManager // <--- Singleton
import kotlinx.coroutines.launch

class PastOrderDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val orderId = intent.getStringExtra("ORDER_ID") ?: ""
        // 1. Obtener datos desde SessionManager
        val token = SessionManager.getToken(this) ?: ""
        val userImage = SessionManager.getUserImage(this)

        setContent {
            var orderDetail by remember { mutableStateOf<PastOrderDetail?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            var errorState by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(Unit) {
                if (token.isNotEmpty() && orderId.isNotEmpty()) {
                    try {
                        val response = RetrofitClient.instance.getPastOrderDetail(orderId, "Bearer $token")

                        if (response.isSuccessful && response.body() != null) {
                            orderDetail = response.body()!!.foodOrder
                        } else {
                            if (response.code() == 401) {
                                MenuHandler.onOptionSelected(this@PastOrderDetailActivity, "Logout")
                            } else {
                                errorState = "Error al cargar el pedido."
                            }
                        }
                    } catch (e: Exception) {
                        errorState = "Error de conexión."
                    } finally {
                        isLoading = false
                    }
                } else {
                    errorState = "Pedido no encontrado."
                    isLoading = false
                }
            }

            PastOrderDetailScreen(
                orderId = orderId,
                orderDetail = orderDetail,
                isLoading = isLoading,
                errorState = errorState,
                // Pasamos la imagen
                userImageUrl = userImage, // <--- Agregar este parámetro a PastOrderDetailScreen
                onBackClick = { finish() },
                onMenuOptionClick = { option ->
                    if (option == "Historial") finish()
                    else MenuHandler.onOptionSelected(this@PastOrderDetailActivity, option)
                }
            )
        }
    }
}