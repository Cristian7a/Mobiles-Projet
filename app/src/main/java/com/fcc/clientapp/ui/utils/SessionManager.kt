package com.fcc.clientapp.utils

import android.content.Context
import android.content.SharedPreferences
import com.fcc.clientapp.model.UserSessionData // Asegúrate de importar la data class que creamos arriba
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object SessionManager {
    private const val PREF_NAME = "AppSession"
    private const val KEY_AUTH_TOKEN = "auth_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_USER_ID = "USER_ID"
    private const val KEY_USER_NAME = "USER_NAME"
    private const val KEY_USER_IMAGE = "USER_IMAGE"

    // --- PARTE REACTIVA (ANGULAR STYLE) ---
    // El estado inicial es null
    private val _sessionState = MutableStateFlow<UserSessionData?>(null)
    // Esta es la variable pública que escucharán tus vistas
    val sessionState = _sessionState.asStateFlow()

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // Cargar datos iniciales (Llamar esto al iniciar la app o una actividad principal)
    // Esto sirve para cuando abres la app y el Flow está vacío pero las SharedPreferences tienen datos
    fun loadFromPreferences(context: Context) {
        val prefs = getPreferences(context)
        val token = prefs.getString(KEY_AUTH_TOKEN, null)
        val userId = prefs.getString(KEY_USER_ID, null)

        if (token != null && userId != null) {
            _sessionState.value = UserSessionData(
                userId = userId,
                name = prefs.getString(KEY_USER_NAME, ""),
                imageUrl = prefs.getString(KEY_USER_IMAGE, null),
                token = token
            )
        }
    }

    fun saveSession(
        context: Context,
        token: String,
        refreshToken: String,
        userId: String,
        name: String? = null,
        image: String? = null
    ) {
        // 1. Guardar en disco (Persistencia)
        with(getPreferences(context).edit()) {
            putString(KEY_AUTH_TOKEN, token)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_IMAGE, image)
            apply()
        }

        // 2. EMITIR EL CAMBIO (Reactividad)
        // Todas las pantallas suscritas se actualizarán instantáneamente
        _sessionState.value = UserSessionData(userId, name, image, token)
    }

    fun clearSession(context: Context) {
        with(getPreferences(context).edit()) {
            clear()
            apply()
        }
        // Avisar que ya no hay sesión
        _sessionState.value = null
    }

    // Getters rápidos (puedes seguir usándolos si no necesitas reactividad)
    fun getToken(context: Context): String? = getPreferences(context).getString(KEY_AUTH_TOKEN, null)
    fun getUserId(context: Context): String? = getPreferences(context).getString(KEY_USER_ID, null)
    fun getUserImage(context: Context): String? = getPreferences(context).getString(KEY_USER_IMAGE, null)

    fun isSessionActive(context: Context): Boolean {
        return !getToken(context).isNullOrEmpty()
    }
}