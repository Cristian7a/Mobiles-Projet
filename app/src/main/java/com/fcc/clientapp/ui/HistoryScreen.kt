package com.fcc.clientapp.ui

// --- IMPORTS NECESARIOS PARA EVITAR ERRORES ---
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.fcc.clientapp.model.PastOrder
import com.fcc.clientapp.ui.components.AppNavbar
import com.fcc.clientapp.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
import java.text.NumberFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.fcc.clientapp.utils.ImageUtils

// Ajusta tu IP local

@Composable
fun HistoryScreen(
    orders: List<PastOrder>,
    isLoading: Boolean,
    errorState: String?,
    userImageUrl: String?, // Para el Navbar
    onBackClick: () -> Unit,
    onMenuOptionClick: (String) -> Unit,
    onOrderClick: (String) -> Unit
) {
    Scaffold(
        topBar = {
            Column {
                // 1. NAVBAR GLOBAL
                AppNavbar(
                    profileImageUrl = userImageUrl,
                    onMenuOptionClick = onMenuOptionClick
                )

                // 2. SUB-HEADER DE NAVEGACIÓN
                Surface(color = Color.White, shadowElevation = 1.dp) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        IconButton(onClick = onBackClick, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = BrandDark)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Historial de Pedidos",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandDark
                        )
                    }
                }
                Divider(color = Color.LightGray.copy(alpha = 0.5f))
            }
        },
        containerColor = BackgroundLight
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = BrandTeal)
            } else if (!errorState.isNullOrEmpty()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Warning, null, tint = ErrorColor, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorState, color = ErrorColor)
                    Button(
                        onClick = onBackClick,
                        modifier = Modifier.padding(top = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandTeal)
                    ) {
                        Text("Volver")
                    }
                }
            } else if (orders.isEmpty()) {
                // ESTADO VACÍO
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = TextHint.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Aún no tienes historial", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BrandDark)
                    Text("Tus órdenes pasadas aparecerán aquí.", fontSize = 16.sp, color = TextHint)
                }
            } else {
                // LISTA RESPONSIVA
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 350.dp), // Se adapta a landscape
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(orders) { order ->
                        PastOrderCard(order, onOrderClick)
                    }
                }
            }
        }
    }
}

@Composable
fun PastOrderCard(order: PastOrder, onClick: (String) -> Unit) {
    // 1. Datos
    val foodOrgName = order.foodOrganization.name
    val rawLogoUrl = order.foodOrganization.logoUrl

    // Buscar item destacado con imagen
    val featuredItem = order.items.firstOrNull { !it.menuItem.imageUrl.isNullOrEmpty() }
        ?: order.items.firstOrNull()

    val itemName = featuredItem?.menuItem?.name ?: "Sin items"
    val rawItemImage = featuredItem?.menuItem?.imageUrl
    val quantity = featuredItem?.quantity ?: 0
    val extraItems = (order.items.size - 1).coerceAtLeast(0)

    // Helper URLs

    val logoUrl = ImageUtils.buildFullUrl(rawLogoUrl) // <--- AQUÍ
    val itemImageUrl = ImageUtils.buildFullUrl(rawItemImage) // <--- Y AQUÍ

    val formattedDate = try {
        val zdt = ZonedDateTime.parse(order.createdAt)
        zdt.format(DateTimeFormatter.ofPattern("d MMM yyyy, hh:mm a", Locale("es", "MX")))
    } catch (e: Exception) { order.createdAt }

    val formattedPrice = NumberFormat.getCurrencyInstance(Locale("es", "MX")).format(order.totalPrice)

    val (statusColor, statusLabel) = when (order.status) {
        "delivered" -> BrandTeal to "Entregado"
        "cancelled" -> ErrorColor to "Cancelado"
        "out_for_delivery" -> BrandOrange to "En camino"
        "preparing" -> BrandOrange to "Preparando"
        else -> TextHint to order.status.uppercase()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(order.id) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Cabecera
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    if (logoUrl != null) {
                        AsyncImage(
                            model = logoUrl, contentDescription = null,
                            modifier = Modifier.size(24.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            error = painterResource(R.drawable.ic_default_business)
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.ic_default_business),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = foodOrgName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = BrandDark, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Surface(color = statusColor.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                    Text(text = statusLabel, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }

            Divider(thickness = 1.dp, color = Color(0xFFE2E8F0))

            // Cuerpo
            Row(modifier = Modifier.padding(12.dp)) {
                if (itemImageUrl != null) {
                    AsyncImage(
                        model = itemImageUrl, contentDescription = null,
                        modifier = Modifier.size(64.dp).clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Crop,
                        error = painterResource(R.drawable.ic_default_food_item)
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.ic_default_food_item),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp).clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = itemName, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = BrandDark, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                        Text(text = " x$quantity", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BrandDark)
                        if (extraItems > 0) {
                            Text(text = " (+$extraItems más)", fontSize = 11.sp, color = TextHint, modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        Text(text = formattedDate, fontSize = 11.sp, color = TextHint)
                        Text(text = formattedPrice, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandDark)
                    }
                }

                // Icono flecha
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = TextHint.copy(alpha=0.5f), modifier = Modifier.align(Alignment.CenterVertically).size(16.dp))
            }
        }
    }
}