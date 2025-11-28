package com.fcc.clientapp.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.fcc.clientapp.model.DetailedUserResponse
import com.fcc.clientapp.ui.components.AppNavbar
import com.fcc.clientapp.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import com.fcc.clientapp.BuildConfig
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.fcc.clientapp.utils.ImageUtils

// ⚠️ Asegúrate de que esta sea tu IP correcta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: DetailedUserResponse?,
    isLoading: Boolean,
    isSaving: Boolean,
    errorState: String?,
    userImageUrl: String?,
    onBackClick: () -> Unit,
    onMenuOptionClick: (String) -> Unit,
    onSaveChanges: (String, Uri?, Boolean) -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // --- ESTADOS ---
    // Guardamos el nombre original para poder cancelar
    var originalName by remember(user) { mutableStateOf(user?.name ?: "") }
    var editedName by remember(user) { mutableStateOf(user?.name ?: "") }
    var isEditingName by remember { mutableStateOf(false) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isImageDeleted by remember { mutableStateOf(false) }

    // Detección de cambios
    val nameChanged = user != null && editedName.trim() != user.name
    val imageChanged = selectedImageUri != null || (isImageDeleted && !user?.profileImage.isNullOrEmpty())
    val hasChanges = nameChanged || imageChanged

    // Picker
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            isImageDeleted = false
        }
    }

    // --- LÓGICA DE IMAGEN (SOLUCIÓN CACHÉ) ---
    val remoteImageUrl = user?.profileImage
    val fullRemoteUrl = remember(remoteImageUrl) {
        val url = ImageUtils.buildFullUrl(remoteImageUrl) // <--- ÚSALO AQUÍ
        if (url != null) "$url?t=${System.currentTimeMillis()}" else null
    }

    // Modelo final para Coil
    val imageModel: Any? = if (selectedImageUri != null) selectedImageUri else if (!isImageDeleted) fullRemoteUrl else null

    Scaffold(
        topBar = {
            Column {
                AppNavbar(profileImageUrl = userImageUrl, onMenuOptionClick = onMenuOptionClick)
                Surface(color = Color.White, shadowElevation = 1.dp) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = BrandDark)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mi Perfil", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BrandDark)
                    }
                }
            }
        },
        containerColor = BackgroundLight,
        floatingActionButton = {
            if (hasChanges && !isLoading && !isSaving) {
                ExtendedFloatingActionButton(
                    onClick = {
                        // Al guardar, actualizamos el "original" para que la UI sepa que ya se guardó
                        originalName = editedName
                        onSaveChanges(editedName, selectedImageUri, isImageDeleted)
                    },
                    containerColor = BrandTeal,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Save, null) },
                    text = { Text("GUARDAR CAMBIOS") }
                )
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            if (isLoading && user == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandTeal)
                }
            } else if (!errorState.isNullOrEmpty() && user == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = errorState ?: "Error desconocido", color = ErrorColor)
                }
            } else if (user != null) {

                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .padding(24.dp)
                        .widthIn(max = 600.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // --- FOTO DE PERFIL ---
                    Box(contentAlignment = Alignment.BottomEnd) {
                        // El Box con key fuerza recomposición si cambia el modelo
                        key(imageModel) {
                            Box(
                                modifier = Modifier
                                    .size(130.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F5F9))
                                    .border(4.dp, Color.White, CircleShape)
                                    .clickable { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (imageModel != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(imageModel)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(Icons.Default.Person, null, tint = TextHint, modifier = Modifier.size(64.dp))
                                }
                            }
                        }

                        // Botones de Acción (Cámara / Basura)
                        Row {
                            if (imageModel != null) {
                                SmallFloatingActionButton(
                                    onClick = { isImageDeleted = true; selectedImageUri = null },
                                    containerColor = ErrorColor,
                                    contentColor = Color.White,
                                    modifier = Modifier.size(40.dp)
                                ) { Icon(Icons.Default.Delete, null, Modifier.size(20.dp)) }
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            SmallFloatingActionButton(
                                onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                containerColor = BrandTeal,
                                contentColor = Color.White,
                                modifier = Modifier.size(40.dp)
                            ) { Icon(Icons.Default.CameraAlt, null, Modifier.size(20.dp)) }
                        }
                    }

                    if (isSaving) {
                        Spacer(modifier = Modifier.height(24.dp))
                        LinearProgressIndicator(color = BrandTeal, modifier = Modifier.width(150.dp))
                        Text("Actualizando...", fontSize = 12.sp, color = BrandTeal, modifier = Modifier.padding(top=8.dp))
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // --- TARJETA DE DATOS ---
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {

                            // NOMBRE (UX: Input Editable)
                            Text("Nombre Completo", fontSize = 12.sp, color = TextHint)

                            if (isEditingName) {
                                // MODO EDICIÓN
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = editedName,
                                        onValueChange = { editedName = it },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = BrandTeal,
                                            cursorColor = BrandTeal
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    // CANCELAR (X)
                                    IconButton(onClick = {
                                        editedName = originalName // Revertir cambios
                                        isEditingName = false
                                    }) {
                                        Icon(Icons.Default.Close, "Cancelar", tint = ErrorColor)
                                    }
                                    // ACEPTAR (Check)
                                    IconButton(onClick = {
                                        // Confirmar edición local (aún no se envía al back)
                                        isEditingName = false
                                    }) {
                                        Icon(Icons.Default.Check, "Listo", tint = BrandTeal)
                                    }
                                }
                            } else {
                                // MODO LECTURA
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                ) {
                                    Text(
                                        text = editedName,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = BrandDark,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = { isEditingName = true }) {
                                        Icon(Icons.Default.Edit, "Editar", tint = BrandTeal)
                                    }
                                }
                            }

                            Divider(color = BackgroundLight, modifier = Modifier.padding(vertical = 16.dp))

                            // EMAIL (Read Only)
                            InfoRow(Icons.Default.Email, "Correo Electrónico", user.email)

                            if (!user.phone.isNullOrEmpty()) {
                                Divider(color = BackgroundLight, modifier = Modifier.padding(vertical = 16.dp))
                                InfoRow(Icons.Default.Phone, "Teléfono", user.phone)
                            }

                            Divider(color = BackgroundLight, modifier = Modifier.padding(vertical = 16.dp))

                            // FECHA REGISTRO
                            val dateStr = try {
                                val zdt = ZonedDateTime.parse(user.createdAt)
                                zdt.format(DateTimeFormatter.ofPattern("d 'de' MMMM, yyyy", Locale("es", "MX")))
                            } catch (e: Exception) { user.createdAt ?: "-" }

                            InfoRow(Icons.Default.CalendarMonth, "Miembro Desde", dateStr)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = TextHint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, fontSize = 11.sp, color = TextHint)
            Text(text = value, fontSize = 15.sp, color = BrandDark)
        }
    }
}