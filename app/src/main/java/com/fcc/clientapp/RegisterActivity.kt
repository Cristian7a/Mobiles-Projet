package com.fcc.clientapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.fcc.clientapp.model.RegisterRequest
import com.fcc.clientapp.network.RetrofitClient
import com.fcc.clientapp.ui.RegisterScreen
import kotlinx.coroutines.launch

// CAMBIO: Heredamos de ComponentActivity (necesario para Compose)
class RegisterActivity : ComponentActivity() {

    // Estado local de la actividad
    private var isLoading by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // CAMBIO: Usamos setContent en lugar de setContentView
        setContent {
            RegisterScreen(
                onRegisterClick = { name, email, password -> performRegister(name, email, password) },
                onBackClick = { finish() }, // Volver atrás cierra la actividad
                isLoading = isLoading
            )
        }
    }

    private fun performRegister(name: String, email: String, pass: String) {
        if (name.isBlank() || email.isBlank() || pass.isBlank()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.register(
                    RegisterRequest(name, email, pass)
                )

                if (response.isSuccessful) {
                    Toast.makeText(applicationContext, "¡Cuenta creada! Inicia sesión.", Toast.LENGTH_LONG).show()
                    finish() // Cerramos registro para volver al Login
                } else {
                    Toast.makeText(applicationContext, "Error: Revisa tus datos", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(applicationContext, "Error de conexión", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
}