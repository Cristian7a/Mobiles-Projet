package com.fcc.clientapp.ui.components

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fcc.clientapp.R
import com.fcc.clientapp.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu // <--- NUEVO IMPORT
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.History
import com.fcc.clientapp.BuildConfig

// URL desde Gradle
private const val SERVER_BASE_URL = BuildConfig.API_BASE_URL

@Composable
fun AppNavbar(
    profileImageUrl: String? = null,
    onNavigationClick: (() -> Unit)? = null, // <--- NUEVO PARÁMETRO (Opcional)
    onMenuOptionClick: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    // Construcción de URL
    val finalImageUrl = remember(profileImageUrl) {
        if (!profileImageUrl.isNullOrEmpty()) {
            val url = if (profileImageUrl.startsWith("http")) profileImageUrl
            else {
                val cleanPath = if (profileImageUrl.startsWith("/")) profileImageUrl.substring(1) else profileImageUrl
                "$SERVER_BASE_URL$cleanPath"
            }
            "$url?t=${System.currentTimeMillis()}"
        } else {
            null
        }
    }

    // NAVBAR
    Surface(
        shadowElevation = 2.dp,
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // --- IZQUIERDA: HAMBURGUESA + MARCA ---
            Row(verticalAlignment = Alignment.CenterVertically) {

                // LOGICA VISUAL: Solo mostrar si existe la acción de navegar
                if (onNavigationClick != null) {
                    IconButton(onClick = onNavigationClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menú Lateral",
                            tint = BrandDark
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "enjova",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = BrandDark,
                    letterSpacing = (-0.5).sp
                )
            }

            // --- DERECHA: MENÚ USUARIO ---
            Box {
                IconButton(
                    onClick = { showMenu = !showMenu },
                    modifier = Modifier.size(44.dp)
                ) {
                    if (finalImageUrl != null) {
                        AsyncImage(
                            model = finalImageUrl,
                            contentDescription = "Perfil",
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray),
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = android.R.drawable.ic_menu_report_image)
                        )
                    } else {
                        Surface(
                            shape = CircleShape,
                            color = BackgroundLight,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Menú",
                                    tint = TextHint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // --- DROPDOWN MENU ---
                MaterialTheme(
                    shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(12.dp))
                ) {
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        offset = DpOffset(0.dp, 8.dp),
                        modifier = Modifier
                            .background(Color.White)
                            .width(220.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Mi Perfil", color = BrandDark, fontWeight = FontWeight.SemiBold) },
                            onClick = { showMenu = false; onMenuOptionClick("Profile") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.AccountCircle,
                                    contentDescription = null,
                                    tint = BrandTeal
                                )
                            }
                        )

                        Divider(color = BackgroundLight, modifier = Modifier.padding(horizontal = 16.dp))

                        DropdownMenuItem(
                            text = { Text("Explorar", color = BrandDark) },
                            onClick = { showMenu = false; onMenuOptionClick("Explorar") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Public,
                                    contentDescription = null,
                                    tint = TextHint
                                )
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Historial", color = BrandDark) },
                            onClick = { showMenu = false; onMenuOptionClick("Historial") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.History,
                                    contentDescription = null,
                                    tint = TextHint
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Divider(color = BackgroundLight, modifier = Modifier.padding(horizontal = 16.dp))
                        Spacer(modifier = Modifier.height(4.dp))

                        DropdownMenuItem(
                            text = { Text("Cerrar Sesión", color = ErrorColor, fontWeight = FontWeight.Medium) },
                            onClick = { showMenu = false; onMenuOptionClick("Logout") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = null,
                                    tint = ErrorColor
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}