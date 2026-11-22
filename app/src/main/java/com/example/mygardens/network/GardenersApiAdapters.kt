package com.example.mygardens.network

import com.example.mygardens.data.CustomerDto
import com.example.mygardens.data.CustomerRequest
import com.example.mygardens.data.TaskDto
import com.example.mygardens.data.TaskRequest
import com.example.mygardens.ui.Customer
import com.example.mygardens.ui.Mission
import com.example.mygardens.ui.theme.PriorityHigh
import com.example.mygardens.ui.theme.PriorityLow
import com.example.mygardens.ui.theme.PriorityMedium
import java.text.SimpleDateFormat
import java.util.Locale

private fun formatDateForUi(isoDate: String): String {
    return try {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val uiFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val parsed = isoFormat.parse(isoDate)
        if (parsed != null) uiFormat.format(parsed) else isoDate
    } catch (e: Exception) {
        isoDate
    }
}

private fun formatDateForApi(uiDate: String): String {
    return try {
        val uiFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsed = uiFormat.parse(uiDate)
        if (parsed != null) isoFormat.format(parsed) else uiDate
    } catch (e: Exception) {
        uiDate
    }
}

fun TaskDto.toMission(): Mission {
    val hebrewPriority = when (priority.uppercase()) {
        "HIGH" -> "גבוהה"
        "LOW" -> "נמוכה"
        else -> "בינונית"
    }
    return Mission(
        id = id.toInt(),
        title = title,
        client = client,
        customerId = customerId,
        date = formatDateForUi(date),
        time = time,
        address = address.orEmpty(),
        notes = notes.orEmpty(),
        status = when (status.uppercase()) {
            "COMPLETED" -> "הושלם"
            "IN_PROGRESS" -> "בתהליך"
            "CANCELLED" -> "בוטל"
            "OPEN" -> "פתוח"
            else -> "חדש"
        },
        priority = hebrewPriority,
        priorityColor = when (priority.uppercase()) {
            "HIGH" -> PriorityHigh
            "LOW" -> PriorityLow
            else -> PriorityMedium
        },
        isCompleted = isCompleted,
        assignee = assignee.orEmpty(),
        assigneeInitial = assignee?.firstOrNull()?.toString().orEmpty(),
        entranceCode = entranceCode.orEmpty()
    )
}

fun Mission.toGardenersRequest(): TaskRequest = TaskRequest(
    title = title,
    customerId = customerId,
    client = client,
    date = formatDateForApi(date),
    time = time,
    address = address,
    entranceCode = entranceCode,
    notes = notes,
    status = when {
        isCompleted -> "COMPLETED"
        status == "בתהליך" -> "IN_PROGRESS"
        status == "בוטל" -> "CANCELLED"
        status == "פתוח" -> "OPEN"
        else -> "NEW"
    },
    priority = when (priority) {
        "גבוהה" -> "HIGH"
        "נמוכה" -> "LOW"
        else -> "MEDIUM"
    },
    assignee = assignee
)

fun CustomerDto.toGardenersCustomer(): Customer = Customer(
    id = id.toInt(),
    name = name,
    phone = phone.orEmpty(),
    address = address.orEmpty(),
    email = email.orEmpty(),
    notes = notes.orEmpty(),
    entryPassword = entryPassword ?: entranceCode.orEmpty()
)

fun Customer.toCustomerRequest(): CustomerRequest = CustomerRequest(
    name = name,
    phone = phone,
    email = email,
    address = address,
    notes = notes,
    entryPassword = entryPassword
)
