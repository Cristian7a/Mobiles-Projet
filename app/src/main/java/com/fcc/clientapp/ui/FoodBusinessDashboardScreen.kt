package com.fcc.clientapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fcc.clientapp.R
import com.fcc.clientapp.model.FoodOrder
import com.fcc.clientapp.model.DashboardOrderDetail
import com.fcc.clientapp.model.OrderStatus
import com.fcc.clientapp.network.RetrofitClient
import com.fcc.clientapp.ui.layout.BusinessLayout
import com.fcc.clientapp.ui.theme.*
import com.fcc.clientapp.utils.SessionManager
import com.fcc.clientapp.utils.ImageUtils
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodBusinessDashboardScreen(
    foodOrgId: String,
    businessName: String,
    businessLogoUrl: String?,
    userImageUrl: String?,
    onMenuOptionClick: (String) -> Unit,
    onNavigateDrawer: (String) -> Unit
) {
    val context = LocalContext.current
    val token = SessionManager.getToken(context) ?: ""
    val scope = rememberCoroutineScope()

    var ordersMap by remember { mutableStateOf<Map<String, List<FoodOrder>>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorState by remember { mutableStateOf<String?>(null) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = OrderStatus.values()

    // Estados Modal
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedOrderSummary by remember { mutableStateOf<FoodOrder?>(null) }
    var selectedOrderDetail by remember { mutableStateOf<DashboardOrderDetail?>(null) }
    var isDetailLoading by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    suspend fun fetchDashboard(silent: Boolean = false) {
        if (!silent) isLoading = true
        try {
            val response = RetrofitClient.instance.getOrdersDashboard(foodOrgId, "Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                ordersMap = response.body()!!.orders
            } else {
                if (!silent) errorState = "Error cargando datos"
            }
        } catch (e: Exception) {
            if (!silent) errorState = "Error de conexión"
        } finally {
            if (!silent) isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        fetchDashboard(silent = false)
        while(true) {
            delay(10000)
            fetchDashboard(silent = true)
        }
    }

    fun changeStatus(orderId: String, targetStatus: String) {
        scope.launch {
            isDetailLoading = true
            try {
                val body = mapOf("targetStatus" to targetStatus)
                val response = RetrofitClient.instance.updateOrderStatus(foodOrgId, orderId, body, "Bearer $token")
                if (response.isSuccessful) {
                    showBottomSheet = false
                    fetchDashboard(silent = false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isDetailLoading = false
            }
        }
    }

    fun openOrderDetails(order: FoodOrder) {
        selectedOrderSummary = order
        selectedOrderDetail = null
        showBottomSheet = true
        isDetailLoading = true

        scope.launch {
            try {
                val response = RetrofitClient.instance.getDashboardOrderDetail(foodOrgId, order.id, "Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    selectedOrderDetail = response.body()!!.order
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isDetailLoading = false
            }
        }
    }

    BusinessLayout(
        businessName = businessName,
        businessLogoUrl = businessLogoUrl,
        userImageUrl = userImageUrl,
        currentRoute = "dashboard",
        onMenuOptionClick = onMenuOptionClick,
        onNavigateDrawer = onNavigateDrawer
    ) { paddingValues ->

        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).background(BackgroundLight)) {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = BrandDark,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]), color = tabs[selectedTabIndex].color)
                }
            ) {
                tabs.forEachIndexed { index, status ->
                    val count = ordersMap[status.key]?.size ?: 0
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(status.label, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal, color = if (selectedTabIndex == index) status.color else TextHint)
                                if (count > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Badge(containerColor = if (selectedTabIndex == index) status.color else BackgroundLight) {
                                        Text(text = count.toString(), color = if (selectedTabIndex == index) Color.White else TextHint, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    )
                }
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = BrandTeal) }
            } else {
                val currentStatus = tabs[selectedTabIndex]
                val currentOrders = ordersMap[currentStatus.key] ?: emptyList()

                if (currentOrders.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(currentStatus.icon, null, tint = TextHint.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
                            Text("No hay órdenes ${currentStatus.label}", color = TextHint, modifier = Modifier.padding(top=16.dp))
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 300.dp),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(currentOrders) { order ->
                            DashboardOrderCard(order, currentStatus) { openOrderDetails(order) }
                        }
                    }
                }
            }
        }

        if (showBottomSheet && selectedOrderSummary != null) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = Color.White,
                modifier = Modifier.fillMaxHeight(0.9f)
            ) {
                DashboardDetailSheet(
                    summary = selectedOrderSummary!!,
                    detail = selectedOrderDetail,
                    isLoading = isDetailLoading,
                    onChangeStatus = { id, status -> changeStatus(id, status) },
                    onClose = { showBottomSheet = false }
                )
            }
        }
    }
}

// --- TARJETA LISTA (RESUMEN) ---
@Composable
fun DashboardOrderCard(order: FoodOrder, status: OrderStatus, onClick: () -> Unit) {
    val priceFormatted = NumberFormat.getCurrencyInstance(Locale("es", "MX")).format(order.totalPrice)
    val timeFormatted = try { ZonedDateTime.parse(order.createdAt).format(DateTimeFormatter.ofPattern("HH:mm")) } catch (e: Exception) { "" }
    val clientName = order.consumer?.name ?: "Cliente"

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Surface(color = status.color.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                    Text("#${order.id.take(5).uppercase()}", color = status.color, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
                Text(timeFormatted, fontSize = 12.sp, color = TextHint)
            }
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, tint = BrandTeal, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(clientName, fontWeight = FontWeight.Bold, color = BrandDark, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nota de Items (Ya que el back no manda la lista en el resumen)
            Text("Ver detalles de productos...", fontSize = 13.sp, color = BrandTeal, fontWeight = FontWeight.Medium)

            if (!order.notes.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Edit, null, tint = BrandOrange, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hay notas del cliente", fontSize = 12.sp, color = BrandDark, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = BackgroundLight, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(priceFormatted, fontWeight = FontWeight.Black, fontSize = 16.sp, color = BrandDark, modifier = Modifier.align(Alignment.End))
        }
    }
}

// --- DETALLE MODAL (FUSIÓN DE DATOS) ---
@Composable
fun DashboardDetailSheet(
    summary: FoodOrder,
    detail: DashboardOrderDetail?,
    isLoading: Boolean,
    onChangeStatus: (String, String) -> Unit,
    onClose: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 100.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // HEADER (Datos del Resumen)
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("Orden #${summary.id.take(5).uppercase()}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = BrandDark)
                    val dateStr = try { ZonedDateTime.parse(summary.createdAt).format(DateTimeFormatter.ofPattern("HH:mm a")) } catch (e: Exception) { "" }
                    Text(dateStr, fontSize = 14.sp, color = TextHint)
                }
                Text(
                    text = NumberFormat.getCurrencyInstance(Locale("es", "MX")).format(summary.totalPrice),
                    fontSize = 24.sp, fontWeight = FontWeight.Black, color = BrandTeal
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // CLIENTE (Datos del Resumen o Detalle)
            val clientName = detail?.consumer?.name ?: summary.consumer?.name ?: "Cliente Anónimo"
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, tint = BrandTeal)
                Spacer(modifier = Modifier.width(8.dp))
                Text(clientName, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = BrandDark)
            }
            Divider(modifier = Modifier.padding(vertical = 16.dp), color = BackgroundLight)

            if (isLoading || detail == null) {
                Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandTeal)
                }
            } else {
                // IMAGEN DESTACADA (Primer producto con foto)
                val featuredItem = detail.items.firstOrNull { !it.menuItem.imageUrl.isNullOrEmpty() }
                val featuredImgUrl = ImageUtils.buildFullUrl(featuredItem?.menuItem?.imageUrl)

                if (featuredImgUrl != null) {
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(12.dp))) {
                        AsyncImage(
                            model = featuredImgUrl, contentDescription = null,
                            modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                        )
                        // Gradiente para texto
                        Box(Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.6f)))))
                        Text(
                            text = featuredItem!!.menuItem.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text("Productos (${detail.items.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextHint)
                Spacer(modifier = Modifier.height(8.dp))

                detail.items.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.Top) {
                        val imgUrl = ImageUtils.buildFullUrl(item.menuItem.imageUrl)
                        if (imgUrl != null) {
                            AsyncImage(model = imgUrl, contentDescription = null, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                        } else {
                            Image(painter = painterResource(R.drawable.ic_default_food_item), contentDescription = null, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.menuItem.name, fontWeight = FontWeight.Medium, color = BrandDark)
                            Text("x${item.quantity}", fontSize = 13.sp, color = TextHint)
                        }
                        Text(NumberFormat.getCurrencyInstance(Locale("es", "MX")).format(item.totalPrice), fontWeight = FontWeight.Bold, color = BrandDark)
                    }
                    Divider(color = BackgroundLight)
                }

                // NOTAS (Del Resumen, que sabemos que las tiene)
                if (!summary.notes.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Notas del Cliente", fontWeight = FontWeight.Bold, color = BrandOrange)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(color = Color(0xFFFFF7ED), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(12.dp)) {
                            Icon(Icons.Default.Edit, null, tint = BrandOrange, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = summary.notes, fontSize = 14.sp, color = BrandDark, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        }
                    }
                }

                // DELIVERY
                if (detail.deliveryWorkOrder != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(color = Color(0xFFF0F9FF), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DeliveryDining, null, tint = Color(0xFF0EA5E9))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Repartidor", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0EA5E9))
                                Text(detail.deliveryWorkOrder.assignedTo?.name ?: "Buscando...", fontSize = 14.sp, color = BrandDark)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // BOTONES
        if (!isLoading) {
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter),
                shadowElevation = 16.dp,
                color = Color.White
            ) {
                Box(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                    val currentStatus = OrderStatus.fromKey(summary.status)
                    val nextAction = when(currentStatus) {
                        OrderStatus.QUEUED -> "ACEPTAR Y PREPARAR" to OrderStatus.PREPARING.key
                        OrderStatus.PREPARING -> "MARCAR LISTO" to OrderStatus.READY.key
                        OrderStatus.READY -> "ENTREGAR A REPARTIDOR" to OrderStatus.OUT.key
                        else -> null
                    }

                    if (nextAction != null) {
                        Button(
                            onClick = { onChangeStatus(summary.id, nextAction.second) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandTeal)
                        ) {
                            Text(nextAction.first, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (currentStatus == OrderStatus.CANCELLED) {
                        Button(
                            onClick = onClose,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
                        ) { Text("CERRAR (CANCELADO)") }
                    } else {
                        Button(
                            onClick = onClose,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TextHint)
                        ) { Text("CERRAR") }
                    }
                }
            }
        }
    }
}