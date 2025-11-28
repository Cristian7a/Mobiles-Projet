// File: com.fcc.clientapp.model/FoodCategoryModels.kt

package com.fcc.clientapp.model

data class FoodCategory(
    val id: String,
    val foodOrganizationId: String,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val parentCategoryId: String?,
    val isActive: Boolean,
    val children: List<FoodCategory>? = null,
    // items y FoodItem no son necesarios para la vista principal de categorías
)

data class FoodCategoryResponse(
    val categories: List<FoodCategory>
)

// Respuesta al crear/actualizar (asumiendo { "category": FoodCategory })
data class FoodCategoryWrapperResponse(
    val category: FoodCategory
)