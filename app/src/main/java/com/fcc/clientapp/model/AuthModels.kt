package com.fcc.clientapp.model

// Lo que envías al hacer Login (según LoginSchema)
data class LoginRequest(
    val email: String,
    val password: String
)

// Lo que recibes del backend (según generateUserTokens en authController.ts)
data class LoginResponse(
    val token: String,
    val refreshToken: String,
    val id: String,
    val name: String?,
    val profileImage: String? // <--- NUEVO
)

// Para manejar errores (opcional pero recomendado dado tu ProblemError)
data class ErrorResponse(
    val status: Int?,
    val title: String?,
    val detail: String?
)

// ... (Tus clases anteriores LoginRequest, LoginResponse)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    // Agrega 'phone' si tu backend lo requiere obligatoriamente
    // val phone: String
)

data class ForgotPasswordRequest(
    val email: String
)

// Respuesta genérica (muchos endpoints devuelven solo un mensaje o status)
data class GenericResponse(
    val message: String?,
    val status: String?
)