package com.fcc.clientapp.ui

import android.content.res.Configuration
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fcc.clientapp.R
import com.fcc.clientapp.model.*
import com.fcc.clientapp.network.RetrofitClient
import com.fcc.clientapp.ui.layout.BusinessLayout
import com.fcc.clientapp.ui.theme.*
import com.fcc.clientapp.utils.SessionManager
import com.fcc.clientapp.utils.ImageUtils
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
    val configuration = LocalConfiguration.current

    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var ordersMap by remember { mutableStateOf<Map<String, List<FoodOrder>>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = OrderStatus.values()

    // Estados Modal y Lógica
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedOrderSummary by remember { mutableStateOf<FoodOrder?>(null) }
    var selectedOrderDetail by remember { mutableStateOf<DashboardOrderDetail?>(null) }
    var isDetailLoading by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // --- LÓGICA DE DATOS ---
    suspend fun fetchDashboard(silent: Boolean = false) {
        if (!silent) isLoading = true
        try {
            val response = RetrofitClient.instance.getOrdersDashboard(foodOrgId, "Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                ordersMap = response.body()!!.orders
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (!silent) isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        fetchDashboard(silent = false)
        while(true) {
            delay(15000) // Polling cada 15s
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundLight)
        ) {
            if (isLoading && ordersMap.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = BrandTeal)
            } else {
                if (isLandscape) {
                    KanbanDashboardView(
                        ordersMap = ordersMap,
                        onOrderClick = { openOrderDetails(it) }
                    )
                } else {
                    PortraitDashboardView(
                        ordersMap = ordersMap,
                        tabs = tabs,
                        selectedTabIndex = selectedTabIndex,
                        onTabSelected = { selectedTabIndex = it },
                        onOrderClick = { openOrderDetails(it) }
                    )
                }
            }

            // --- BOTTOM SHEET DETALLE ---
            if (showBottomSheet && selectedOrderSummary != null) {
                ModalBottomSheet(
                    onDismissRequest = { showBottomSheet = false },
                    sheetState = sheetState,
                    containerColor = Color.White,
                    // Altura dinámica: en landscape cubre casi todo, en portrait deja un margen
                    modifier = Modifier.fillMaxHeight(if (isLandscape) 1f else 0.95f)
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
}

// --- VISTAS PRINCIPALES ---

@Composable
fun PortraitDashboardView(
    ordersMap: Map<String, List<FoodOrder>>,
    tabs: Array<OrderStatus>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    onOrderClick: (FoodOrder) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.White,
            contentColor = BrandDark,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = tabs[selectedTabIndex].color
                )
            }
        ) {
            tabs.forEachIndexed { index, status ->
                val count = ordersMap[status.key]?.size ?: 0
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { onTabSelected(index) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                status.label,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) status.color else TextHint
                            )
                            if (count > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Badge(containerColor = if (selectedTabIndex == index) status.color else BackgroundLight) {
                                    Text(
                                        text = count.toString(),
                                        color = if (selectedTabIndex == index) Color.White else TextHint,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }

        val currentStatus = tabs[selectedTabIndex]
        val currentOrders = ordersMap[currentStatus.key] ?: emptyList()

        if (currentOrders.isEmpty()) {
            EmptyStateMessage(currentStatus)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 300.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(currentOrders) { order ->
                    DashboardOrderCard(order, currentStatus, onOrderClick)
                }
            }
        }
    }
}

@Composable
fun KanbanDashboardView(
    ordersMap: Map<String, List<FoodOrder>>,
    onOrderClick: (FoodOrder) -> Unit
) {
    val statuses = OrderStatus.values()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        statuses.forEach { status ->
            val orders = ordersMap[status.key] ?: emptyList()

            Column(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            ) {
                // Header Columna
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(status.color, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            status.label.uppercase(),
                            color = TextHint,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    if (orders.isNotEmpty()) {
                        Surface(
                            color = status.color.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "${orders.size}",
                                color = status.color,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Divider(color = Color(0xFFE2E8F0))

                if (orders.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("-", color = TextHint.copy(alpha = 0.3f), fontSize = 24.sp)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(orders) { order ->
                            DashboardOrderCard(order, status, onOrderClick)
                        }
                    }
                }
            }
        }
    }
}

// --- COMPONENTES UI REUTILIZABLES ---

@Composable
fun EmptyStateMessage(status: OrderStatus) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                status.icon,
                null,
                tint = TextHint.copy(alpha = 0.1f),
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Sin órdenes",
                color = TextHint.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DashboardOrderCard(
    order: FoodOrder,
    status: OrderStatus,
    onClick: (FoodOrder) -> Unit
) {
    val priceFormatted = NumberFormat.getCurrencyInstance(Locale("es", "MX")).format(order.totalPrice)
    val timeFormatted = try {
        ZonedDateTime.parse(order.createdAt).format(DateTimeFormatter.ofPattern("HH:mm"))
    } catch (e: Exception) {
        "..."
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(order) }
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Barra de color lateral indicadora de estado
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(status.color)
            )

            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .weight(1f)
            ) {
                // Fila Superior: ID y Hora
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "#${order.id.take(5).uppercase()}",
                        color = BrandDark,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )

                    Text(
                        timeFormatted,
                        fontSize = 12.sp,
                        color = TextHint,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Nombre Cliente
                Text(
                    text = order.consumer?.name ?: "Cliente",
                    fontWeight = FontWeight.SemiBold,
                    color = BrandDark,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Fila Inferior: Notas (si hay) y Precio
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (!order.notes.isNullOrEmpty()) {
                        Surface(
                            color = BrandOrange.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.StickyNote2, null, tint = BrandOrange, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Nota", fontSize = 10.sp, color = BrandOrange, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Spacer(Modifier.weight(1f))
                    }

                    Text(
                        priceFormatted,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = BrandTeal
                    )
                }
            }
        }
    }
}

// --- SECCIONES REUTILIZABLES DEL DETALLE ---

@Composable
fun ClientInfoSection(detail: DashboardOrderDetail) {
    Column {
        Text("CLIENTE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextHint)
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = Color(0xFFF1F5F9), modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = TextHint, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(detail.consumer?.name ?: "Cliente Anónimo", fontWeight = FontWeight.Bold, color = BrandDark, fontSize = 16.sp)
                Text("ID Cliente: ${detail.consumer?.id?.take(5) ?: "---"}", fontSize = 12.sp, color = TextHint)
            }
        }
    }
}

@Composable
fun NotesSection(summary: FoodOrder) {
    if (!summary.notes.isNullOrEmpty()) {
        Column {
            Text("NOTAS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandOrange)
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = Color(0xFFFFF7ED),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFFED7AA)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.StickyNote2, null, tint = BrandOrange, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Nota del cliente:", fontWeight = FontWeight.Bold, color = BrandOrange, fontSize = 13.sp)
                        Text(summary.notes, color = BrandDark, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ProductListSection(detail: DashboardOrderDetail) {
    Column {
        Text("PRODUCTOS (${detail.items.sumOf { it.quantity }})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextHint)
        Spacer(modifier = Modifier.height(12.dp))

        detail.items.forEach { item ->
            OrderItemRow(item)
        }
    }
}

// --- DETALLE MODAL (Lógica de Adaptación) ---

@Composable
fun DashboardDetailSheet(
    summary: FoodOrder,
    detail: DashboardOrderDetail?,
    isLoading: Boolean,
    onChangeStatus: (String, String) -> Unit,
    onClose: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Contenido Scrolleable (Header + Info)
        Column(
            modifier = Modifier
                .weight(1f) // Ocupa todo el espacio menos los botones
                .verticalScroll(rememberScrollState())
        ) {
            // HEADER GRANDE
            val currentStatus = OrderStatus.fromKey(summary.status)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(currentStatus.color.copy(alpha = 0.1f))
                    .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 32.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = Color.White, shape = CircleShape, modifier = Modifier.size(32.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(currentStatus.icon, null, tint = currentStatus.color, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(currentStatus.label.uppercase(), color = currentStatus.color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Orden #${summary.id.take(5).uppercase()}", fontSize = 32.sp, fontWeight = FontWeight.Black, color = BrandDark)
                    Text("Total: ${NumberFormat.getCurrencyInstance(Locale("es", "MX")).format(summary.totalPrice)}", fontSize = 20.sp, color = BrandTeal, fontWeight = FontWeight.Bold)
                }
            }

            // CONTENIDO PRINCIPAL
            if (isLoading || detail == null) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandTeal)
                }
            } else {
                Column(Modifier.padding(24.dp)) {
                    // 1. TIMELINE DE ESTADOS
                    Text("SEGUIMIENTO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextHint)
                    Spacer(modifier = Modifier.height(12.dp))
                    StatusTimelineCard(summary, detail)

                    Spacer(modifier = Modifier.height(32.dp))

                    // 2. ADAPTACIÓN LANDSCAPE/PORTRAIT
                    if (isLandscape) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                            // Columna Izquierda: Cliente y Notas
                            Column(Modifier.weight(1f)) {
                                ClientInfoSection(detail)
                                if (!summary.notes.isNullOrEmpty()) {
                                    Spacer(modifier = Modifier.height(32.dp))
                                    NotesSection(summary)
                                }
                            }
                            // Columna Derecha: Lista de Productos
                            Column(Modifier.weight(1f)) {
                                ProductListSection(detail)
                            }
                        }
                    } else {
                        // Modo Portrait: Apilado normal
                        ClientInfoSection(detail)
                        Spacer(modifier = Modifier.height(32.dp))
                        NotesSection(summary)
                        Spacer(modifier = Modifier.height(32.dp))
                        ProductListSection(detail)
                    }

                    // Espacio de seguridad
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }

        // BOTONES DE ACCIÓN (Sticky Bottom)
        Surface(
            shadowElevation = 16.dp,
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                ActionButtons(summary, onChangeStatus, onClose)
            }
        }
    }
}

@Composable
fun StatusTimelineCard(summary: FoodOrder, detail: DashboardOrderDetail) {
    val foodStatus = OrderStatus.fromKey(summary.status)

    val hasDriver = detail.deliveryWorkOrder?.assignedTo != null
    val rawStatus = detail.deliveryWorkOrder?.status

    val (deliveryStatusLabel, deliveryColor) = when {
        rawStatus == "DELIVERED" -> "Entregado" to Color(0xFF64748B)
        rawStatus == "CANCELLED" -> "Cancelado" to Color(0xFFEF4444)
        hasDriver -> "Repartidor Asignado" to BrandTeal
        rawStatus == "IN_PROGRESS" -> "En Progreso" to BrandTeal
        else -> "Buscando Repartidor" to TextHint
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            // Fila 1: Restaurante
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = foodStatus.color.copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.size(32.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Restaurant, null, tint = foodStatus.color, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Restaurante", fontSize = 11.sp, color = TextHint, fontWeight = FontWeight.Bold)
                    Text(foodStatus.label, color = foodStatus.color, fontWeight = FontWeight.Bold)
                }
            }

            // Conector Visual
            Box(modifier = Modifier.padding(start = 15.dp).height(24.dp).width(2.dp).background(Color(0xFFE2E8F0)))

            // Fila 2: Logística
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = deliveryColor.copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.size(32.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        val icon = if (hasDriver) Icons.Outlined.DeliveryDining else Icons.Default.Moped
                        Icon(icon, null, tint = deliveryColor, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Logística", fontSize = 11.sp, color = TextHint, fontWeight = FontWeight.Bold)
                    Text(deliveryStatusLabel, color = deliveryColor, fontWeight = FontWeight.Bold)

                    if (hasDriver) {
                        Text(
                            detail.deliveryWorkOrder!!.assignedTo!!.name,
                            fontSize = 13.sp,
                            color = BrandDark,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderItemRow(item: DashboardOrderItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val imgUrl = ImageUtils.buildFullUrl(item.menuItem.imageUrl)

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF1F5F9),
            modifier = Modifier.size(56.dp)
        ) {
            if (imgUrl != null) {
                AsyncImage(
                    model = imgUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Fastfood, null, tint = TextHint, modifier = Modifier.size(24.dp))
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(item.menuItem.name, fontWeight = FontWeight.Bold, color = BrandDark, fontSize = 15.sp)
            Text("${item.quantity} x ${NumberFormat.getCurrencyInstance(Locale("es", "MX")).format(item.itemPrice)}", fontSize = 13.sp, color = TextHint)
        }

        Text(
            NumberFormat.getCurrencyInstance(Locale("es", "MX")).format(item.totalPrice),
            fontWeight = FontWeight.Bold,
            color = BrandDark,
            fontSize = 15.sp
        )
    }
    Divider(color = Color(0xFFF1F5F9))
}

@Composable
fun ActionButtons(
    summary: FoodOrder,
    onChangeStatus: (String, String) -> Unit,
    onClose: () -> Unit
) {
    val currentStatus = OrderStatus.fromKey(summary.status)

    val (btnText, nextStatus, btnColor) = when (currentStatus) {
        OrderStatus.QUEUED -> Triple("ACEPTAR ORDEN", OrderStatus.PREPARING.key, BrandTeal)
        OrderStatus.PREPARING -> Triple("MARCAR LISTO", OrderStatus.READY.key, BrandOrange)
        OrderStatus.READY -> Triple("ENTREGAR REPARTIDOR", OrderStatus.OUT.key, Color(0xFF0EA5E9))
        else -> Triple(null, null, Color.Gray)
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(
            onClick = onClose,
            modifier = Modifier.weight(1f).height(50.dp),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
        ) {
            Text("Cerrar", color = TextHint)
        }

        if (btnText != null && nextStatus != null) {
            Button(
                onClick = { onChangeStatus(summary.id, nextStatus) },
                modifier = Modifier.weight(2f).height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = btnColor)
            ) {
                Text(btnText, fontWeight = FontWeight.Bold)
            }
        }
    }
}