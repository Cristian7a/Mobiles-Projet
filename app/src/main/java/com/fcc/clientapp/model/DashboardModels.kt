package com.fcc.clientapp.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

enum class OrderStatus(
    val key: String,
    val label: String,
    val color: Color,
    val icon: ImageVector
) {
    QUEUED("queued", "En Cola", Color(0xFF0EA5E9), Icons.Default.AccessTime),
    PREPARING("preparing", "Preparando", Color(0xFFF59E0B), Icons.Default.Kitchen),
    READY("ready_for_pickup", "Listo", Color(0xFF10B981), Icons.Default.ShoppingBag),
    OUT("out_for_delivery", "En Reparto", Color(0xFFF97316), Icons.Default.DeliveryDining),
    DELIVERED("delivered", "Entregado", Color(0xFF64748B), Icons.Default.CheckCircle),
    CANCELLED("cancelled", "Cancelado", Color(0xFFD32F2F), Icons.Default.Close);

    companion object {
        fun fromKey(key: String): OrderStatus = values().find { it.key == key } ?: QUEUED
    }
}

data class DashboardResponse(
    val orders: Map<String, List<FoodOrder>>
)

// Info que viene en la LISTA (Resumen)
data class FoodOrder(
    val id: String,
    val status: String,
    val totalPrice: Double,
    val createdAt: String,
    val notes: String?,
    // items viene vacío en la lista según tu backend
    val items: List<DashboardOrderItem>?,
    val consumer: ConsumerInfo?
)

data class DashboardOrderDetailResponse(
    val order: DashboardOrderDetail
)

// Info que viene en el DETALLE (Endpoint específico)
// QUITAMOS: id, status, totalPrice, notes (El back no los manda aquí)
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