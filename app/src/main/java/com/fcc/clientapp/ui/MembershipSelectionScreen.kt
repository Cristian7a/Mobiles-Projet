package com.fcc.clientapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fcc.clientapp.R
import com.fcc.clientapp.model.UserMembership
import com.fcc.clientapp.ui.components.AppNavbar
import com.fcc.clientapp.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
import com.fcc.clientapp.utils.ImageUtils

@Composable
fun MembershipSelectionScreen(
    memberships: List<UserMembership>,
    isLoading: Boolean,
    errorState: String?,
    userImageUrl: String?, // <--- AGREGADO: Recibe la imagen
    onMembershipClick: (UserMembership) -> Unit,
    onMenuOptionClick: (String) -> Unit
) {
    Scaffold(
        topBar = {
            // Pasamos la imagen al Navbar
            AppNavbar(
                profileImageUrl = userImageUrl,
                onMenuOptionClick = onMenuOptionClick
            )
        },
        containerColor = BackgroundLight
    ) { paddingValues ->
        // ... (El resto del contenido sigue idéntico al anterior) ...
        // CONTENEDOR PRINCIPAL
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // CABECERA
            Column(modifier = Modifier.fillMaxWidth().widthIn(max = 900.dp)) {
                Text(
                    text = "Tus Espacios",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandTeal
                )
                Text(
                    text = "Selecciona dónde quieres trabajar hoy.",
                    fontSize = 16.sp,
                    color = TextHint,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )
            }

            // ESTADOS
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandTeal)
                }
            } else if (!errorState.isNullOrEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Warning, null, tint = ErrorColor, modifier = Modifier.size(48.dp))
                        Text(text = errorState, color = ErrorColor, modifier = Modifier.padding(top = 16.dp))
                        Button(
                            onClick = { onMenuOptionClick("Logout") },
                            modifier = Modifier.padding(top = 16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandTeal)
                        ) { Text("Salir") }
                    }
                }
            } else if (memberships.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tienes membresías activas.", color = TextHint)
                }
            } else {
                // GRILLA
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 340.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    modifier = Modifier.widthIn(max = 1200.dp).fillMaxWidth()
                ) {
                    items(memberships) { item ->
                        MembershipCard(item, onMembershipClick)
                    }
                }
            }
        }
    }
}

// ... (La función MembershipCard sigue igual) ...
@Composable
fun MembershipCard(item: UserMembership, onClick: (UserMembership) -> Unit) {
    val type = item.membership.type
    val extras = item.extras

    val title = when (type) {
        "FoodOrganizationWithDelivery", "RestaurantWaiters" -> extras?.business?.name ?: item.membership.name
        "DeliveryService" -> extras?.profile?.displayName ?: item.membership.name
        else -> item.membership.name
    }

    val description = when(type) {
        "FoodOrganizationWithDelivery" -> "Gestión de organización"
        "RestaurantWaiters" -> "Meseros y servicio"
        "DeliveryService" -> "Perfil de conductor"
        else -> "Membresía activa"
    }

    val rawLogoUrl = when (type) {
        "FoodOrganizationWithDelivery", "RestaurantWaiters" -> extras?.business?.logoUrl
        else -> null
    }

    val finalImageUrl = ImageUtils.buildFullUrl(rawLogoUrl) // <--- ÚSALO AQUÍ

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (!item.isDisabled) onClick(item) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(BackgroundLight)
            ) {
                if (finalImageUrl != null) {
                    AsyncImage(
                        model = finalImageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = rememberVectorPainter(Icons.Default.Store)
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = TextHint.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = BrandDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = item.membership.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = BrandTeal,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextHint,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACCEDER",
                        color = BrandOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = BrandOrange,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}