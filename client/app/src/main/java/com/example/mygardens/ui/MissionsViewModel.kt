package com.example.mygardens.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mygardens.data.GardenersApi
import com.example.mygardens.network.toCustomerRequest
import com.example.mygardens.network.toGardenersCustomer
import com.example.mygardens.network.toGardenersRequest
import com.example.mygardens.network.toMission
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class MissionFilter {
    ALL, OPEN, DONE, TODAY, HIGH_PRIORITY
}

class MissionsViewModel : ViewModel() {
    private var loadedSessionToken: String? = null

    private val _missions = MutableStateFlow<List<Mission>>(emptyList())
    val allMissions: StateFlow<List<Mission>> = _missions.asStateFlow()

    private val _customers = MutableStateFlow<List<Customer>>(emptyList())
    val customers: StateFlow<List<Customer>> = _customers.asStateFlow()

    private val _currentFilter = MutableStateFlow(MissionFilter.ALL)
    val currentFilter: StateFlow<MissionFilter> = _currentFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _serverError = MutableStateFlow<String?>(null)
    val serverError: StateFlow<String?> = _serverError.asStateFlow()

    private val _calendarMessage = MutableStateFlow<String?>(null)
    val calendarMessage: StateFlow<String?> = _calendarMessage.asStateFlow()

    // Derived state for the UI, automatically updates when any source flow changes
    val filteredMissions: StateFlow<List<Mission>> = combine(
        _missions, _currentFilter, _searchQuery
    ) { missions, filter, query ->
        val lowercaseQuery = query.lowercase()
        missions.filter { mission ->
            (mission.title.lowercase().contains(lowercaseQuery) || mission.client.lowercase()
                .contains(lowercaseQuery)) &&
                    when (filter) {
                        MissionFilter.ALL -> !mission.isCompleted
                        MissionFilter.OPEN -> !mission.isCompleted
                        MissionFilter.DONE -> mission.isCompleted
                        MissionFilter.TODAY -> mission.date == SimpleDateFormat(
                            "dd/MM/yyyy",
                            Locale.getDefault()
                        ).format(Date())

                        MissionFilter.HIGH_PRIORITY -> mission.priority == "גבוהה"
                    }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSessionChanged(sessionToken: String?) {
        if (sessionToken == loadedSessionToken) return
        loadedSessionToken = sessionToken
        if (sessionToken == null) {
            _missions.value = emptyList()
            _customers.value = emptyList()
            _serverError.value = null
            _calendarMessage.value = null
            return
        }
        fetchMissionsFromServer()
        fetchCustomersFromServer()
    }

    fun fetchMissionsFromServer() {
        viewModelScope.launch {
            try {
                val networkMissions = GardenersApi.instance.tasks()
                _missions.value = networkMissions.map { it.toMission() }
                _serverError.value = null
            } catch (e: Exception) {
                _serverError.value = "מצב לא מקוון: ${e.message ?: "שגיאת תקשורת"}"
                e.printStackTrace()
            }
        }
    }

    fun fetchCustomersFromServer() {
        viewModelScope.launch {
            try {
                val networkCustomers = GardenersApi.instance.customers()
                _customers.value = networkCustomers.map { it.toGardenersCustomer() }
                _serverError.value = null
            } catch (e: Exception) {
                _serverError.value = "מצב לא מקוון: ${e.message ?: "שגיאת תקשורת"}"
                e.printStackTrace()
            }
        }
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun toggleMissionCompletion(missionId: Int) {
        if (GardenersApi.sessionToken.isNullOrBlank()) return
        val originalMissions = _missions.value
        val mission = _missions.value.firstOrNull { it.id == missionId } ?: return
        val updated = mission.copy(
            isCompleted = !mission.isCompleted,
            status = if (mission.isCompleted) "פתוח" else "הושלם"
        )
        // Local update for immediate feedback
        _missions.update { list -> list.map { if (it.id == missionId) updated else it } }

        viewModelScope.launch {
            try {
                val saved =
                    GardenersApi.instance.setCompletion(missionId.toLong(), updated.isCompleted)
                        .toMission()
                _missions.update { list -> list.map { if (it.id == missionId) saved else it } }
                _serverError.value = null
            } catch (e: Exception) {
                _missions.value = originalMissions
                _serverError.value = "העדכון לא נשמר: ${failureMessage(e)}"
            }
        }
    }

    fun addMission(
        title: String,
        client: String,
        customerId: Long? = null,
        address: String,
        notes: String,
        entranceCode: String,
        date: String,
        time: String,
        priority: String,
        priorityColor: Color
    ): Boolean {
        if (GardenersApi.sessionToken.isNullOrBlank()) return false
        if (_missions.value.any { it.title == title && it.client == client }) {
            return false
        }
        val originalMissions = _missions.value
        _calendarMessage.value = null

        val newMission = Mission(
            id = (_missions.value.maxOfOrNull { it.id } ?: 0) + 1,
            title = title,
            client = client,
            customerId = customerId,
            address = address,
            notes = notes,
            entranceCode = entranceCode,
            date = date,
            time = time,
            status = "חדש",
            priority = priority,
            priorityColor = priorityColor,
            isCompleted = false,
            assignee = "מאיה נחמן",
            assigneeInitial = "מ"
        )
        // Local update first
        _missions.update { it + newMission }

        viewModelScope.launch {
            try {
                val saved =
                    GardenersApi.instance.createTask(newMission.toGardenersRequest()).toMission()
                _missions.update { list -> list.map { if (it.title == title && it.client == client) saved else it } }
                _serverError.value = null
                try {
                    addTaskToGoogleCalendar(saved)
                    _calendarMessage.value = "המשימה נוספה ליומן Google."
                } catch (calendarError: Exception) {
                    _calendarMessage.value =
                        "המשימה נשמרה, אך לא נוספה ליומן: ${failureMessage(calendarError)}"
                }
            } catch (e: Exception) {
                _missions.value = originalMissions
                _serverError.value = "המשימה לא נשמרה: ${failureMessage(e)}"
            }
        }
        return true
    }

    fun updateMission(updatedMission: Mission) {
        if (GardenersApi.sessionToken.isNullOrBlank()) return
        val originalMissions = _missions.value
        _missions.update { list -> list.map { if (it.id == updatedMission.id) updatedMission else it } }

        viewModelScope.launch {
            try {
                val saved = GardenersApi.instance.updateTask(
                    updatedMission.id.toLong(),
                    updatedMission.toGardenersRequest()
                ).toMission()
                _missions.update { list -> list.map { if (it.id == updatedMission.id) saved else it } }
                _serverError.value = null
            } catch (e: Exception) {
                _missions.value = originalMissions
                _serverError.value = "העדכון לא נשמר: ${failureMessage(e)}"
            }
        }
    }

    fun addCustomer(
        name: String,
        phone: String,
        address: String,
        email: String,
        notes: String,
        entryPassword: String = ""
    ): Boolean {
        if (GardenersApi.sessionToken.isNullOrBlank()) return false
        if (name.isBlank()) return false
        val originalCustomers = _customers.value

        val newCustomer = Customer(
            id = (_customers.value.maxOfOrNull { it.id } ?: 0) + 1,
            name = name,
            phone = phone,
            address = address,
            email = email,
            notes = notes,
            entryPassword = entryPassword
        )
        _customers.update { it + newCustomer }

        viewModelScope.launch {
            try {
                val saved = GardenersApi.instance.createCustomer(newCustomer.toCustomerRequest())
                    .toGardenersCustomer()
                _customers.update { list -> list.map { if (it.name == name && it.phone == phone) saved else it } }
                _serverError.value = null
            } catch (e: Exception) {
                _customers.value = originalCustomers
                _serverError.value = "הלקוח לא נשמר: ${failureMessage(e)}"
            }
        }
        return true
    }

    fun updateCustomer(updatedCustomer: Customer) {
        if (GardenersApi.sessionToken.isNullOrBlank()) return
        val originalCustomers = _customers.value
        _customers.update { list -> list.map { if (it.id == updatedCustomer.id) updatedCustomer else it } }

        viewModelScope.launch {
            try {
                val saved = GardenersApi.instance.updateCustomer(
                    updatedCustomer.id.toLong(),
                    updatedCustomer.toCustomerRequest()
                ).toGardenersCustomer()
                _customers.update { list -> list.map { if (it.id == updatedCustomer.id) saved else it } }
                _serverError.value = null
            } catch (e: Exception) {
                _customers.value = originalCustomers
                _serverError.value = "העדכון לא נשמר: ${failureMessage(e)}"
            }
        }
    }

    fun setFilter(filter: MissionFilter) {
        _currentFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private fun failureMessage(error: Exception): String {
        val body = (error as? HttpException)?.response()?.errorBody()?.string()
        val serverMessage = body?.let {
            runCatching {
                val json = JsonParser.parseString(it).asJsonObject
                json.get("error")?.takeIf { value -> !value.isJsonNull }?.asString
            }.getOrNull()
        }
        return serverMessage ?: error.message ?: "שגיאת תקשורת"
    }

    private suspend fun addTaskToGoogleCalendar(mission: Mission) {
        val session = GardenersApi.sessionToken
            ?: throw IllegalStateException("יש להתחבר עם Google כדי לסנכרן את היומן")
        val parser =
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).apply { isLenient = false }
        val start = parser.parse("${mission.date} ${mission.time}")
            ?: throw IllegalArgumentException("לא ניתן לקרוא את תאריך ושעת המשימה")
        val endCalendar = Calendar.getInstance().apply {
            time = start
            add(Calendar.MINUTE, 60)
        }
        val dateTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
        val timeZone = TimeZone.getDefault().id
        val event = JsonObject().apply {
            addProperty("summary", mission.title)
            if (mission.notes.isNotBlank()) addProperty("description", mission.notes)
            if (mission.address.isNotBlank()) addProperty("location", mission.address)
            add("start", JsonObject().apply {
                addProperty("dateTime", dateTime.format(start))
                addProperty("timeZone", timeZone)
            })
            add("end", JsonObject().apply {
                addProperty("dateTime", dateTime.format(endCalendar.time))
                addProperty("timeZone", timeZone)
            })
        }
        GardenersApi.instance.createCalendarEvent("Bearer $session", event)
    }
}
