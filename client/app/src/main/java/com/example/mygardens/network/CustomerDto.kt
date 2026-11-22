package com.example.mygardens.network

import com.example.mygardens.ui.Customer
import com.google.gson.annotations.SerializedName

/**
 * Data class representing the Customer JSON structure returned from or sent to the server.
 */
data class CustomerDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("address")
    val address: String,

    @SerializedName("email")
    val email: String? = "",

    @SerializedName("notes")
    val notes: String? = ""
)

/**
 * Extension function to map network CustomerDto to domain Customer model.
 */
fun CustomerDto.toCustomer(): Customer = Customer(
    id = id,
    name = name,
    phone = phone,
    address = address,
    email = email ?: "",
    notes = notes ?: ""
)

/**
 * Extension function to map domain Customer model to network CustomerDto.
 */
fun Customer.toDto(): CustomerDto = CustomerDto(
    id = id,
    name = name,
    phone = phone,
    address = address,
    email = email,
    notes = notes
)
