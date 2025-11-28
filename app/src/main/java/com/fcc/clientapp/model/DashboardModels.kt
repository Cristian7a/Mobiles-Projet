package com.fcc.clientapp.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*

// Enum exacto según tu backend (food-delivery-orders.ts)
enum class OrderStatus(
    val key: String,
    val label: String,
    val color: Color,
    val icon: ImageVector
) {
    QUEUED("queued", "Nueva", Color(0xFF0EA5E9), Icons.Default.NotificationsActive), // Sky-500
    PREPARING("preparing", "Cocina", Color(0xFFF59E0B), Icons.Default.SoupKitchen), // Amber-500
    READY("ready_for_pickup", "Listo", Color(0xFF10B981), Icons.Default.ShoppingBag), // Emerald-500
    OUT("out_for_delivery", "En Ruta", Color(0xFF6366F1), Icons.Default.DeliveryDining), // Indigo-500
    DELIVERED("delivered", "Entregado", Color(0xFF64748B), Icons.Default.CheckCircle), // Slate-500
    CANCELLED("cancelled", "Cancelado", Color(0xFFEF4444), Icons.Outlined.Cancel); // Red-500

    companion object {
        fun fromKey(key: String): OrderStatus = values().find { it.key == key } ?: QUEUED
    }
}

data class DashboardResponse(
    val orders: Map<String, List<FoodOrder>>
)

// Modelo para la LISTA (Resumen)
// Según tu backend: items viene vacío y deliveryWorkOrder.status viene null aquí.
data class FoodOrder(
    val id: String,
    val status: String,
    val totalPrice: Double,
    val createdAt: String,
    val notes: String?,
    val items: List<DashboardOrderItem>?, // Viene vacío en la lista
    val consumer: ConsumerInfo?,
    val deliveryWorkOrder: DeliveryWorkOrderLite?
)

// Versión ligera de WorkOrder que viene en la lista
data class DeliveryWorkOrderLite(
    val id: String,
    val status: String? // Puede ser null en la lista según foodOrdersModel.ts
)

data class DashboardOrderDetailResponse(
    val order: DashboardOrderDetail
)

// Modelo para el DETALLE (Full info)
data class DashboardOrderDetail(
    val consumer: ConsumerInfo?,
    val items: List<DashboardOrderItem>,
    val deliveryWorkOrder: DeliveryWorkOrderInfo?
)

data class DeliveryWorkOrderInfo(
    val id: String,
    val status: String,
    val assignedTo: DeliveryUserInfo?
)

data class DeliveryUserInfo(val id: String, val name: String)

data class DashboardOrderItem(
    val quantity: Int,
    val menuItem: DashboardMenuItem,
    val itemPrice: Double,
    val totalPrice: Double
)

data class DashboardMenuItem(
    val id: String,
    val name: String,
    val imageUrl: String?
)

data class ConsumerInfo(
    val id: String,
    val name: String
)