package com.fcc.clientapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.fcc.clientapp.model.LoginRequest
import com.fcc.clientapp.network.RetrofitClient
import com.fcc.clientapp.ui.LoginScreen
import com.fcc.clientapp.utils.SessionManager // <--- USAMOS EL SINGLETON
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var isLoading by mutableStateOf(false)
    private var errorMessage by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. VALIDACIÓN DE SESIÓN CENTRALIZADA
        if (SessionManager.isSessionActive(this)) {
            goToMembershipSelection()
            return
        }

        setContent {
            LoginScreen(
                onLoginClick = { email, password -> performLogin(email, password) },
                onRegisterClick = {
                    startActivity(Intent(this, RegisterActivity::class.java))
                },
                onForgotPasswordClick = {
                    startActivity(Intent(this, ForgotPasswordActivity::class.java))
                },
                isLoading = isLoading,
                errorMessage = errorMessage
            )
        }
    }

    private fun performLogin(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            errorMessage = "Completa los campos"
            return
        }

        isLoading = true
        errorMessage = null

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.login(LoginRequest(email, pass))

                if (response.isSuccessful && response.body() != null) {
                    val loginData = response.body()!!

                    // 2. GUARDADO DE SESIÓN CENTRALIZADO
                    SessionManager.saveSession(
                        context = applicationContext,
                        token = loginData.token,
                        refreshToken = loginData.refreshToken,
                        userId = loginData.id,
                        name = loginData.name,
                        image = loginData.profileImage
                    )

                    Toast.makeText(applicationContext, "¡Bienvenido!", Toast.LENGTH_SHORT).show()
                    goToMembershipSelection()
                } else {
                    errorMessage = "Error: Credenciales incorrectas (${response.code()})"
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: Verifica tu red"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    private fun goToMembershipSelection() {
        val intent = Intent(this, MembershipSelectionActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}