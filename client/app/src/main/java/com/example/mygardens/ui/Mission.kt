package com.example.mygardens.ui

import androidx.compose.ui.graphics.Color

data class Mission(
    val id: Int,
    val title: String,
    val client: String,
    val customerId: Long? = null,
    val date: String,
    val time: String = "08:00",
    val address: String = "רחוב הגנים 12, כפר סבא",
    val notes: String = "יש להביא כלי עבודה ידניים ודשן מיוחד לוורדים.",
    val status: String,
    val priority: String,
    val priorityColor: Color,
    val isCompleted: Boolean,
    val assignee: String,
    val assigneeInitial: String,
    val entranceCode: String = ""
)
