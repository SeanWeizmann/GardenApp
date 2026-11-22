package com.example.mygardens.ui

/**
 * Domain data class representing a Customer.
 */
data class Customer(
    val id: Int,
    val name: String,
    val phone: String,
    val address: String,
    val email: String = "",
    val notes: String = "",
    val entryPassword: String = ""
)
