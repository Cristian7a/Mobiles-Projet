package com.fccc.clientapp.network

import com.fcc.clientapp.model.LoginRequest
import com.fcc.clientapp.model.LoginResponse
import com.fcc.clientapp.model.RegisterRequest
import com.fcc.clientapp.model.ForgotPasswordRequest
import com.fcc.clientapp.model.GenericResponse
import com.fcc.clientapp.model.MembershipsResponse
import com.fcc.clientapp.model.PastOrdersResponse
import com.fcc.clientapp.model.PastOrderDetailResponse
import com.fcc.clientapp.model.DetailedUserResponse
import com.fcc.clientapp.model.DashboardResponse
import com.fcc.clientapp.model.DashboardOrderDetailResponse
import retrofit2.Response
import retrofit2.http.*
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface ApiService {

    // Definimos el endpoint relativo
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // NUEVO: Registro
    @POST("auth/signup")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>
    // Nota: A veces signup devuelve el token directo (LoginResponse) o solo un OK.
    // Si tu backend hace login automático al registrar, usa LoginResponse.

    // NUEVO: Olvidé contraseña
    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<GenericResponse>

    @GET("a/o/memberships")
    suspend fun getUserMemberships(
        @Header("Authorization") token: String
    ): Response<MembershipsResponse>

    @GET("a/c/fo/past-orders")
    suspend fun getPastOrders(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20, // Traemos 20 por defecto
        @Query("sortBy") sortBy: String = "o.createdAt",
        @Query("sortDirection") sortDirection: String = "desc"
    ): Response<PastOrdersResponse>

    @GET("a/c/fo/past-orders/{orderId}")
    suspend fun getPastOrderDetail(
        @Path("orderId") orderId: String,
        @Header("Authorization") token: String
    ): Response<PastOrderDetailResponse>

    @GET("a/c/profile")
    suspend fun getUserProfile(
        @Header("Authorization") token: String
    ): Response<DetailedUserResponse>

    @Multipart
    @PUT("a/c/profile")
    suspend fun updateUserProfile(
        @Header("Authorization") token: String,
        @Part("name") name: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<DetailedUserResponse>

    @GET("a/o/fo/{foodOrgId}/orders/dashboard")
    suspend fun getOrdersDashboard(
        @Path("foodOrgId") foodOrgId: String,
        @Header("Authorization") token: String
    ): Response<DashboardResponse>

    // Ruta: /api/v1/a/o/fo/{foodOrgId}/orders/dashboard/{orderId}
    @GET("a/o/fo/{foodOrgId}/orders/dashboard/{orderId}")
    suspend fun getDashboardOrderDetail(
        @Path("foodOrgId") foodOrgId: String,
        @Path("orderId") orderId: String,
        @Header("Authorization") token: String
    ): Response<DashboardOrderDetailResponse>

    @PUT("a/o/fo/{foodOrgId}/orders/{orderId}/status")
    suspend fun updateOrderStatus(
        @Path("foodOrgId") foodOrgId: String,
        @Path("orderId") orderId: String,
        @Body statusBody: Map<String, String>, // { "targetStatus": "preparing" }
        @Header("Authorization") token: String
    ): Response<Unit> // Devuelve 200 OK o error
}