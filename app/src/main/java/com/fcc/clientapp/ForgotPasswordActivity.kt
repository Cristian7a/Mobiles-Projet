package com.fcc.clientapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.fcc.clientapp.model.ForgotPasswordRequest
import com.fcc.clientapp.network.RetrofitClient
import com.fcc.clientapp.ui.ForgotPasswordScreen
import kotlinx.coroutines.launch

class ForgotPasswordActivity : ComponentActivity() {

    private var isLoading by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ForgotPasswordScreen(
                onSendClick = { email -> performReset(email) },
                onBackClick = { finish() },
                isLoading = isLoading
            )
        }
    }

    private fun performReset(email: String) {
        if (email.isBlank()) {
            Toast.makeText(this, "Ingresa un correo válido", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.forgotPassword(
                    ForgotPasswordRequest(email)
                )

                if (response.isSuccessful) {
                    Toast.makeText(applicationContext, "Revisa tu bandeja de entrada", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(applicationContext, "No se pudo procesar la solicitud", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(applicationContext, "Error de conexión", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }
}