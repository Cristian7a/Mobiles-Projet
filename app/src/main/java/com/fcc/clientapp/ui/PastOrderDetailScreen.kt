package com.fcc.clientapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.fcc.clientapp.model.OrderDetailItem
import com.fcc.clientapp.model.PastOrderDetail
import com.fcc.clientapp.ui.components.AppNavbar
import com.fcc.clientapp.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.ReceiptLong
import java.text.NumberFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.fcc.clientapp.utils.ImageUtils


@Composable
fun PastOrderDetailScreen(
    orderId: String,
    orderDetail: PastOrderDetail?,
    isLoading: Boolean,
    errorState: String?,
    userImageUrl: String?, // Recibe imagen del Navbar
    onBackClick: () -> Unit,
    onMenuOptionClick: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            Column {
                AppNavbar(
                    profileImageUrl = userImageUrl,
                    onMenuOptionClick = onMenuOptionClick
                )

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
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Detalle del Pedido",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandDark
                            )
                            if (orderDetail != null) {
                                Text(
                                    text = "ID: ${orderDetail.id.take(8).uppercase()}",
                                    fontSize = 12.sp,
                                    color = TextHint
                                )
                            }
                        }
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
            contentAlignment = Alignment.TopCenter
        ) {
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandTeal)
                }
            } else if (!errorState.isNullOrEmpty() || orderDetail == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Store, null, tint = ErrorColor, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = errorState ?: "No se encontró el pedido", color = ErrorColor)
                        Button(
                            onClick = onBackClick,
                            modifier = Modifier.padding(top = 16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandTeal)
                        ) {
                            Text("Regresar")
                        }
                    }
                }
            } else {
                BoxWithConstraints {
                    if (maxWidth > 600.dp) {
                        LandscapeContent(orderDetail)
                    } else {
                        PortraitContent(orderDetail)
                    }
                }
            }
        }
    }
}

@Composable
fun PortraitContent(orderDetail: PastOrderDetail) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        OrderStatusHeader(orderDetail.status, orderDetail.createdAt)
        Spacer(modifier = Modifier.height(16.dp))
        BusinessHeaderCard(orderDetail)
        Spacer(modifier = Modifier.height(24.dp))
        ProductListSection(orderDetail)

        if (!orderDetail.notes.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            NotesSection(orderDetail.notes)
        }

        Spacer(modifier = Modifier.height(24.dp))
        PaymentSummaryCard(orderDetail.totalPrice)
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun LandscapeContent(orderDetail: PastOrderDetail) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(0.4f)
                .verticalScroll(rememberScrollState())
        ) {
            OrderStatusHeader(orderDetail.status, orderDetail.createdAt)
            Spacer(modifier = Modifier.height(16.dp))
            BusinessHeaderCard(orderDetail)
            Spacer(modifier = Modifier.height(16.dp))
            PaymentSummaryCard(orderDetail.totalPrice)

            if (!orderDetail.notes.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                NotesSection(orderDetail.notes)
            }
        }

        Column(
            modifier = Modifier
                .weight(0.6f)
                .verticalScroll(rememberScrollState())
        ) {
            ProductListSection(orderDetail)
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProductListSection(orderDetail: PastOrderDetail) {
    Text("Productos", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BrandDark, modifier = Modifier.padding(bottom = 12.dp, start = 4.dp))
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column {
            orderDetail.items.forEachIndexed { index, item ->
                OrderItemRow(item)
                if (index < orderDetail.items.size - 1) Divider(color = BackgroundLight, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

@Composable
fun NotesSection(notes: String) {
    Text("Notas del Pedido", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandDark, modifier = Modifier.padding(bottom = 8.dp, start = 4.dp))
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)), modifier = Modifier.fillMaxWidth()) {
        Text(text = notes, fontSize = 14.sp, color = BrandDark, modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun OrderStatusHeader(status: String, dateString: String) {
    val (bgColor, icon, label) = when (status) {
        "delivered" -> Triple(BrandTeal, Icons.Default.CheckCircle, "Entregado")
        "cancelled" -> Triple(ErrorColor, Icons.Default.Close, "Cancelado")
        "out_for_delivery" -> Triple(BrandOrange, Icons.Default.DeliveryDining, "En Camino")
        else -> Triple(TextHint, Icons.Default.Store, status.uppercase())
    }
    val formattedDate = try { ZonedDateTime.parse(dateString).format(DateTimeFormatter.ofPattern("d MMMM yyyy, h:mm a", Locale("es", "MX"))) } catch (e: Exception) { dateString }
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = bgColor), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = label, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                Text(text = formattedDate, fontSize = 13.sp, color = Color.White.copy(alpha = 0.9f))
            }
        }
    }
}

@Composable
fun BusinessHeaderCard(order: PastOrderDetail) {
    val logoPath = order.foodOrganization.logoUrl
    val fullLogoUrl = ImageUtils.buildFullUrl(logoPath) // <--- AQUÍ
}

@Composable
fun OrderItemRow(item: OrderDetailItem) {
    val imagePath = item.menuItem.imageUrl
    val fullUrl = ImageUtils.buildFullUrl(imagePath) // <--- AQUÍ
    val priceFormatted = NumberFormat.getCurrencyInstance(Locale("es", "MX")).format(item.itemPrice)
    val totalFormatted = NumberFormat.getCurrencyInstance(Locale("es", "MX")).format(item.totalPrice)
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.Top) {
        if (fullUrl != null) AsyncImage(model = fullUrl, contentDescription = null, modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop, error = painterResource(R.drawable.ic_default_food_item))
        else Image(painter = painterResource(R.drawable.ic_default_food_item), contentDescription = null, modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.menuItem.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = BrandDark, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "${item.quantity}x", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandDark)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = priceFormatted, fontSize = 13.sp, color = TextHint)
            }
        }
        Text(text = totalFormatted, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BrandDark)
    }
}

@Composable
fun PaymentSummaryCard(total: Double) {
    val totalFormatted = NumberFormat.getCurrencyInstance(Locale("es", "MX")).format(total)
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ReceiptLong, null, tint = BrandTeal)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Total Pagado", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BrandDark)
                }
                Text(text = totalFormatted, fontWeight = FontWeight.Black, fontSize = 20.sp, color = BrandTeal)
            }
        }
    }
}