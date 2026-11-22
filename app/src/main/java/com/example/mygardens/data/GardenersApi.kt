package com.example.mygardens.data

import com.example.mygardens.network.NetworkConfig
import com.google.gson.JsonObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

data class CustomerDto(
    val id: Long,
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val notes: String? = null,
    val entryPassword: String? = null,
    val entranceCode: String? = null
)

data class CustomerRequest(
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val notes: String? = null,
    val entryPassword: String? = null
)

data class TaskDto(
    val id: Long,
    val title: String,
    val customerId: Long? = null,
    val client: String,
    val date: String,
    val time: String,
    val durationMinutes: Int = 60,
    val address: String? = null,
    val entranceCode: String? = null,
    val notes: String? = null,
    val status: String,
    val priority: String,
    val isCompleted: Boolean,
    val assignee: String? = null
)

data class TaskRequest(
    val title: String,
    val customerId: Long?,
    val client: String,
    val date: String,
    val time: String,
    val durationMinutes: Int = 60,
    val address: String,
    val entranceCode: String,
    val notes: String,
    val status: String,
    val priority: String,
    val assignee: String
)

data class GoogleCodeRequest(val code: String)
data class GoogleSession(
    val sessionToken: String,
    val userId: Long,
    val googleId: String,
    val email: String,
    val displayName: String?
)

interface GardenersApi {
    @POST("api/auth/google/exchange")
    suspend fun exchangeGoogleCode(
        @Header("X-Requested-With") requestOrigin: String = "XMLHttpRequest",
        @Body request: GoogleCodeRequest
    ): GoogleSession

    @POST("api/auth/google/logout")
    suspend fun logout()

    @POST("api/calendar/events")
    suspend fun createCalendarEvent(
        @Header("Authorization") bearerSession: String,
        @Body event: JsonObject
    ): JsonObject

    @GET("api/tasks")
    suspend fun tasks(): List<TaskDto>

    @POST("api/tasks")
    suspend fun createTask(@Body request: TaskRequest): TaskDto

    @PUT("api/tasks/{id}")
    suspend fun updateTask(@Path("id") id: Long, @Body request: TaskRequest): TaskDto

    @PATCH("api/tasks/{id}/completion")
    suspend fun setCompletion(@Path("id") id: Long, @Query("completed") completed: Boolean): TaskDto

    @GET("api/customers")
    suspend fun customers(@Query("q") query: String? = null): List<CustomerDto>

    @POST("api/customers")
    suspend fun createCustomer(@Body request: CustomerRequest): CustomerDto

    @PUT("api/customers/{id}")
    suspend fun updateCustomer(@Path("id") id: Long, @Body request: CustomerRequest): CustomerDto

    companion object {
        @Volatile
        var sessionToken: String? = null
        val instance: GardenersApi by lazy {
            val client = okhttp3.OkHttpClient.Builder().addInterceptor { chain ->
                val request = chain.request().newBuilder().apply {
                    sessionToken?.let { header("Authorization", "Bearer $it") }
                }.build()
                chain.proceed(request)
            }.build()
            Retrofit.Builder().baseUrl(NetworkConfig.baseUrl).client(client)
                .addConverterFactory(GsonConverterFactory.create()).build()
                .create(GardenersApi::class.java)
        }
    }
}
