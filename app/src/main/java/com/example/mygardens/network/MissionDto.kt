package com.example.mygardens.network

import com.example.mygardens.ui.Mission
import com.example.mygardens.ui.theme.PriorityHigh
import com.example.mygardens.ui.theme.PriorityLow
import com.example.mygardens.ui.theme.PriorityMedium
import com.google.gson.annotations.SerializedName

/**
 * Data class representing the JSON structure returned from the server.
 */
data class MissionDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("client")
    val client: String,

    @SerializedName("customer_id")
    val customerId: Long? = null,

    @SerializedName("date")
    val date: String,

    @SerializedName("time")
    val time: String? = null,

    @SerializedName("address")
    val address: String? = null,

    @SerializedName("notes")
    val notes: String? = null,

    @SerializedName("status")
    val status: String,

    @SerializedName("priority")
    val priority: String,

    @SerializedName("is_completed")
    val isCompleted: Boolean = false,

    @SerializedName("assignee")
    val assignee: String? = null,

    @SerializedName("assignee_initial")
    val assigneeInitial: String? = null,

    @SerializedName("entrance_code")
    val entranceCode: String? = null
)

/**
 * Extension function to map network DTO to domain model.
 */
fun MissionDto.toMission(): Mission {
    val color = when (priority) {
        "גבוהה" -> PriorityHigh
        "נמוכה" -> PriorityLow
        else -> PriorityMedium
    }
    return Mission(
        id = id,
        title = title,
        client = client,
        customerId = customerId,
        date = date,
        time = time ?: "08:00",
        address = address ?: "רחוב הגנים 12, כפר סבא",
        notes = notes ?: "",
        status = status,
        priority = priority,
        priorityColor = color,
        isCompleted = isCompleted,
        assignee = assignee ?: "מאיה נחמן",
        assigneeInitial = assigneeInitial ?: (assignee?.firstOrNull()?.toString() ?: "מ"),
        entranceCode = entranceCode ?: ""
    )
}

/**
 * Extension function to map domain Mission model to network MissionDto.
 */
fun Mission.toDto(): MissionDto = MissionDto(
    id = id,
    title = title,
    client = client,
    customerId = customerId,
    date = date,
    time = time,
    address = address,
    notes = notes,
    status = status,
    priority = priority,
    isCompleted = isCompleted,
    assignee = assignee,
    assigneeInitial = assigneeInitial,
    entranceCode = entranceCode
)
