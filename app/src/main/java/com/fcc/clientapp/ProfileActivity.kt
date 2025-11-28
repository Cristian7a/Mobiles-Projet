package com.fcc.clientapp

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.fcc.clientapp.model.DetailedUserResponse
import com.fcc.clientapp.network.RetrofitClient
import com.fcc.clientapp.ui.ProfileScreen
import com.fcc.clientapp.utils.MenuHandler
import com.fcc.clientapp.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class ProfileActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val token = SessionManager.getToken(this) ?: ""
        val userId = SessionManager.getUserId(this) ?: ""
        val cachedImage = SessionManager.getUserImage(this)

        setContent {
            var user by remember { mutableStateOf<DetailedUserResponse?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            var isSaving by remember { mutableStateOf(false) }
            var errorState by remember { mutableStateOf<String?>(null) }
            var currentUserImage by remember { mutableStateOf(cachedImage) }

            // Cargar Perfil
            LaunchedEffect(Unit) {
                if (token.isNotEmpty()) {
                    try {
                        val response = RetrofitClient.instance.getUserProfile("Bearer $token")
                        if (response.isSuccessful && response.body() != null) {
                            user = response.body()
                            currentUserImage = user?.profileImage

                            // Actualizar Caché
                            SessionManager.saveSession(
                                this@ProfileActivity, token, "", userId, user?.name, user?.profileImage
                            )
                        } else {
                            if (response.code() == 401) MenuHandler.onOptionSelected(this@ProfileActivity, "Logout")
                            else errorState = "Error al cargar perfil."
                        }
                    } catch (e: Exception) {
                        errorState = "Error de conexión."
                    } finally {
                        isLoading = false
                    }
                } else {
                    MenuHandler.onOptionSelected(this@ProfileActivity, "Logout")
                }
            }

            ProfileScreen(
                user = user,
                isLoading = isLoading,
                isSaving = isSaving,
                errorState = errorState,
                userImageUrl = currentUserImage,
                onBackClick = { finish() },
                onMenuOptionClick = { MenuHandler.onOptionSelected(this@ProfileActivity, it) },
                onSaveChanges = { newName, newImageUri, isImageDeleted ->
                    isSaving = true
                    // Usar lifecycleScope para que no muera si rotan la pantalla
                    // (Aunque ya bloqueamos la rotacion en manifest, es buena practica)
                    kotlinx.coroutines.GlobalScope.launch(Dispatchers.Main) {
                        saveProfileChanges(token, newName, newImageUri, isImageDeleted) { updatedUser ->
                            isSaving = false
                            if (updatedUser != null) {
                                user = updatedUser
                                currentUserImage = updatedUser.profileImage
                                // Actualizar Singleton para que el Navbar se entere en otras vistas
                                SessionManager.saveSession(
                                    this@ProfileActivity, token, "", userId, updatedUser.name, updatedUser.profileImage
                                )
                                       Toast.makeText(this@ProfileActivity, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@ProfileActivity, "No se pudo actualizar", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            )
        }
    }

    private suspend fun saveProfileChanges(
        token: String,
        name: String,
        imageUri: Uri?,
        isImageDeleted: Boolean,
        onResult: (DetailedUserResponse?) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                // 1. Nombre
                val namePart = name.toRequestBody("text/plain".toMediaTypeOrNull())

                // 2. Imagen
                var imagePart: MultipartBody.Part? = null

                if (imageUri != null) {
                    val file = uriToFile(imageUri)
                    if (file != null) {
                        // Detectar tipo MIME (importante para que el back no lo rechace)
                        val mimeType = contentResolver.getType(imageUri) ?: "image/jpeg"
                        val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
                        imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
                    }
                } else if (isImageDeleted) {
                    // Enviar string vacio para borrar
                    imagePart = MultipartBody.Part.createFormData("image", "")
                }

                // 3. Llamada
                val response = RetrofitClient.instance.updateUserProfile("Bearer $token", namePart, imagePart)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        onResult(response.body())
                    } else {
                        Log.e("PROFILE_ERROR", "Code: ${response.code()} - ${response.errorBody()?.string()}")
                        onResult(null)
                    }
                }
            } catch (e: Exception) {
                Log.e("PROFILE_ERROR", "Exception: ${e.message}")
                e.printStackTrace()
                withContext(Dispatchers.Main) { onResult(null) }
            }
        }
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val contentResolver = applicationContext.contentResolver

            // Intentar obtener extensión real
            val mime = MimeTypeMap.getSingleton()
            val type = contentResolver.getType(uri)
            val extension = mime.getExtensionFromMimeType(type) ?: "jpg"

            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("upload_", ".$extension", applicationContext.cacheDir)
            val outputStream = FileOutputStream(tempFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            tempFile
        } catch (e: Exception) {
            Log.e("FILE_ERROR", "Error creando archivo temporal: ${e.message}")
            null
        }
    }
}