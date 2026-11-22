package com.example.mygardens.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Retrofit API interface for executing HTTP requests to the server.
 */
interface MissionsApiService {

    @GET("missions")
    suspend fun getMissions(): List<MissionDto>

    @GET("missions/{id}")
    suspend fun getMissionById(@Path("id") id: Int): MissionDto

    @POST("missions")
    suspend fun createMission(@Body mission: MissionDto): MissionDto

    @PUT("missions/{id}")
    suspend fun updateMission(@Path("id") id: Int, @Body mission: MissionDto): MissionDto

    @DELETE("missions/{id}")
    suspend fun deleteMission(@Path("id") id: Int): Response<Unit>

    // Customer Endpoints
    @GET("customers")
    suspend fun getCustomers(): List<CustomerDto>

    @GET("customers/{id}")
    suspend fun getCustomerById(@Path("id") id: Int): CustomerDto

    @POST("customers")
    suspend fun createCustomer(@Body customer: CustomerDto): CustomerDto

    @PUT("customers/{id}")
    suspend fun updateCustomer(@Path("id") id: Int, @Body customer: CustomerDto): CustomerDto

    @DELETE("customers/{id}")
    suspend fun deleteCustomer(@Path("id") id: Int): Response<Unit>
}
