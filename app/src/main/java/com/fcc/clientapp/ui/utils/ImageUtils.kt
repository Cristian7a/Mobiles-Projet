package com.fcc.clientapp.utils

import com.fcc.clientapp.BuildConfig

object ImageUtils {
    // Usamos la variable centralizada
    private const val BASE_URL = BuildConfig.API_BASE_URL

    fun buildFullUrl(path: String?): String? {
        if (path.isNullOrEmpty()) return null

        return if (path.startsWith("http")) {
            path
        } else {
            // Limpiamos el slash inicial si existe
            val cleanPath = path.removePrefix("/")
            "$BASE_URL$cleanPath"
        }
    }
}