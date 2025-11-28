package com.fcc.clientapp.ui.components

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fcc.clientapp.R
import com.fcc.clientapp.ui.theme.*
import com.fcc.clientapp.utils.ImageUtils
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.outlined.*

@Composable
fun BusinessDrawerContent(
    businessName: String,
    businessLogoUrl: String?, // <--- NUEVO PARÁMETRO
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onCloseDrawer: () -> Unit
) {
    // Procesar URL de imagen
    val finalLogoUrl = remember(businessLogoUrl) {
        ImageUtils.buildFullUrl(businessLogoUrl)
    }

    ModalDrawerSheet(
        drawerContainerColor = Color.White,
        drawerTonalElevation = 0.dp,
        modifier = Modifier.width(320.dp)
    ) {
        // --- CABECERA ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(BrandTeal, BrandTeal.copy(alpha = 0.8f))
                    )
                )
                .padding(24.dp)
                .padding(top = 16.dp)
        ) {
            Column {
                // LOGO DEL NEGOCIO
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (finalLogoUrl != null) {
                            AsyncImage(
                                model = finalLogoUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                                error = painterResource(R.drawable.ic_default_business)
                            )
                        } else {
                            // Icono por defecto si no hay imagen
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = null,
                                tint = BrandTeal,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = businessName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Panel de Administración",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        // --- OPCIONES ---
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = "GESTIÓN",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextHint,
                modifier = Modifier.padding(start = 12.dp, top = 16.dp, bottom = 8.dp)
            )

            DrawerItem(
                label = "Dashboard",
                icon = if (currentRoute == "dashboard") Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                isSelected = currentRoute == "dashboard",
                onClick = { onNavigate("dashboard"); onCloseDrawer() }
            )

            DrawerItem(
                label = "Categorías",
                icon = if (currentRoute == "categories") Icons.Filled.Category else Icons.Outlined.Category,
                isSelected = currentRoute == "categories",
                onClick = { onNavigate("categories"); onCloseDrawer() }
            )

            DrawerItem(
                label = "Productos",
                icon = if (currentRoute == "items") Icons.Filled.Inventory2 else Icons.Outlined.Inventory2,
                isSelected = currentRoute == "items",
                onClick = { onNavigate("items"); onCloseDrawer() }
            )

            Divider(modifier = Modifier.padding(vertical = 16.dp), color = BackgroundLight)

            Text(
                text = "CONFIGURACIÓN",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextHint,
                modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
            )

            DrawerItem(
                label = "Perfil del Negocio",
                icon = if (currentRoute == "profile") Icons.Filled.Store else Icons.Outlined.Store,
                isSelected = currentRoute == "profile",
                onClick = { onNavigate("profile"); onCloseDrawer() }
            )
        }
    }
}

@Composable
private fun DrawerItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
        icon = { Icon(icon, null) },
        selected = isSelected,
        onClick = onClick,
        shape = RoundedCornerShape(100),
        colors = NavigationDrawerItemDefaults.colors(
            // Colores seleccionados
            selectedContainerColor = BrandTeal.copy(alpha = 0.12f),
            selectedIconColor = BrandTeal,
            selectedTextColor = BrandTeal,

            // Colores NO seleccionados (CORRECCIÓN AQUÍ)
            unselectedContainerColor = Color.Transparent, // <--- ESTO QUITA LO ROSA
            unselectedIconColor = TextHint,
            unselectedTextColor = BrandDark
        ),
        modifier = Modifier.padding(vertical = 4.dp)
    )
}