package com.fcc.clientapp.model
import com.google.gson.annotations.SerializedName

// Respuesta raíz del endpoint
data class UserSessionData(
    val userId: String,
    val name: String?,
    val imageUrl: String?,
    val token: String
)
data class MembershipsResponse(
    val memberships: List<UserMembership>?
)

// La estructura principal de cada item
data class UserMembership(
    val id: String,
    val userId: String,
    val membership: MembershipInfo,
    val extras: MembershipExtras?, // Puede ser null
    val isDisabled: Boolean = false
)

data class MembershipInfo(
    val id: String,
    val name: String, // Ej: "Owner", "Waiter"
    val type: String  // Ej: "FoodOrganizationWithDelivery", "DeliveryService"
)

data class MembershipExtras(
    // Para Restaurantes y Organizaciones de Comida
    val business: BusinessInfo?,
    // Para Servicios de Delivery (Drivers)
    val profile: ProfileInfo?,

    // IDs útiles para navegación
    val foodOrganizationId: String?,
    val restaurantId: String?
)

data class BusinessInfo(
    val id: String,
    val name: String,
    val logoUrl: String?
)

data class ProfileInfo(
    val id: String,
    val displayName: String
)
data class DetailedUserResponse(
    val id: String,
    val name: String,
    val email: String,
    val phone: String?, // Agregamos teléfono si el back lo tiene
    @SerializedName("image_url")
    val profileImage: String?,
    val createdAt: String?, // <--- NUEVO: Fecha de registro
    val businessMemberships: List<UserMembership>? = null
)
// ... el resto sigue igual

// ... (Tus modelos de Membresía anteriores) ...

// --- MODELOS PARA HISTORIAL ---

data class PastOrdersResponse(
    val foodOrders: List<PastOrder>,
    val totalCount: Int,
    val page: Int,
    val totalPages: Int
)

data class PastOrder(
    val id: String,
    val foodOrganization: BusinessInfo, // Reusamos BusinessInfo que ya tenías
    val totalPrice: Double,
    val status: String, // 'delivered', 'cancelled', etc.
    val createdAt: String, // Viene como ISO String
    val items: List<OrderItem>
)

data class OrderItem(
    val id: String,
    val menuItem: MenuItemInfo,
    val quantity: Int,
    val itemPrice: Double,
    val totalPrice: Double
)

data class MenuItemInfo(
    val id: String,
    val name: String,
    val imageUrl: String?
)

data class PastOrderDetailResponse(
    val foodOrder: PastOrderDetail
)

data class PastOrderDetail(
    val id: String,
    val foodOrganization: BusinessInfo,
    val totalPrice: Double,
    val status: String,
    val createdAt: String,
    val notes: String?, // <--- NUEVO CAMPO
    val items: List<OrderDetailItem>
)

data class OrderDetailItem(
    val id: String?,
    val menuItem: MenuItemInfo,
    val quantity: Int,
    val itemPrice: Double,
    val totalPrice: Double
)