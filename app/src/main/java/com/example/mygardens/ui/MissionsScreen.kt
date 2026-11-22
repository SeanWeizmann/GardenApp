package com.example.mygardens.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mygardens.R
import com.example.mygardens.ui.theme.CompletedGreen
import com.example.mygardens.ui.theme.PriorityHigh
import com.example.mygardens.ui.theme.PriorityLow
import com.example.mygardens.ui.theme.PriorityMedium
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionsScreen(
    viewModel: MissionsViewModel = viewModel(),
    authMessage: String? = null,
    onSignInClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    isSignedIn: Boolean = false
) {
    val missions by viewModel.allMissions.collectAsState()
    val filteredMissions by viewModel.filteredMissions.collectAsState()
    val filter by viewModel.currentFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val serverError by viewModel.serverError.collectAsState()
    val calendarMessage by viewModel.calendarMessage.collectAsState()

    var selectedMissionForDetails by remember { mutableStateOf<Mission?>(null) }
    var selectedMissionForEdit by remember { mutableStateOf<Mission?>(null) }
    var showAddMissionDialog by remember { mutableStateOf(false) }
    var showGoogleSignInDialog by remember { mutableStateOf(false) }

    var showCustomersDialog by remember { mutableStateOf(false) }
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var selectedCustomerForEdit by remember { mutableStateOf<Customer?>(null) }

    LaunchedEffect(isSignedIn) {
        if (isSignedIn) showGoogleSignInDialog = false
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                MissionsTopBar(
                    openCount = missions.count { !it.isCompleted },
                    isDarkMode = isDarkMode,
                    isSignedIn = isSignedIn,
                    onToggleDarkMode = { viewModel.toggleDarkMode() },
                    onCustomersClick = { showCustomersDialog = true },
                    onSignInClick = { showGoogleSignInDialog = true },
                    onLogoutClick = onLogoutClick,
                    onNewMissionClick = {
                        if (isSignedIn) showAddMissionDialog = true else showGoogleSignInDialog =
                            true
                    }
                )
            },
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.systemBars
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
            ) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.setSearchQuery(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                FilterChips(
                    selectedFilter = filter,
                    onFilterSelected = { viewModel.setFilter(it) },
                    missionsSize = missions.size,
                    openCount = missions.count { !it.isCompleted },
                    doneCount = missions.count { it.isCompleted },
                    todayCount = missions.count {
                        it.date == SimpleDateFormat(
                            "dd/MM/yyyy",
                            Locale.getDefault()
                        ).format(Date())
                    },
                    urgentCount = missions.count { it.priority == "גבוהה" }
                )

                Spacer(modifier = Modifier.height(20.dp))

                InfoBanner()

                serverError?.let { message ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                calendarMessage?.let { message ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = message,
                        color = if (message.contains("לא נוספה ליומן")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredMissions, key = { it.id }) { mission ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                if (isSignedIn) viewModel.toggleMissionCompletion(mission.id)
                                else showGoogleSignInDialog = true
                                false
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromEndToStart = true,
                            enableDismissFromStartToEnd = true,
                            backgroundContent = {
                                val color = if (mission.isCompleted) Color.Gray else CompletedGreen
                                val icon =
                                    if (mission.isCompleted) Icons.AutoMirrored.Filled.Undo else Icons.Default.Check

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(color)
                                        .padding(horizontal = 24.dp),
                                    contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
                                ) {
                                    Icon(
                                        icon,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            },
                            modifier = Modifier.animateItem()
                        ) {
                            MissionCard(
                                mission = mission,
                                onToggleComplete = {
                                    if (isSignedIn) viewModel.toggleMissionCompletion(mission.id)
                                    else showGoogleSignInDialog = true
                                },
                                onEditClick = {
                                    if (isSignedIn) selectedMissionForEdit = mission
                                    else showGoogleSignInDialog = true
                                },
                                onClick = { selectedMissionForDetails = mission }
                            )
                        }
                    }

                    if (filteredMissions.isEmpty()) {
                        item {
                            EmptyMissionsState()
                        }
                    }
                }
            }
        }

        // Mission Detail Popup
        selectedMissionForDetails?.let { mission ->
            MissionDetailsDialog(
                mission = mission,
                onDismiss = { selectedMissionForDetails = null }
            )
        }

        // Add Mission Popup
        if (showAddMissionDialog) {
            AddEditMissionDialog(
                customers = customers,
                onDismiss = { showAddMissionDialog = false },
                onConfirm = { title, client, customerId, address, notes, code, date, time, priority, color ->
                    viewModel.addMission(
                        title,
                        client,
                        customerId,
                        address,
                        notes,
                        code,
                        date,
                        time,
                        priority,
                        color
                    )
                }
            )
        }

        // Edit Mission Popup
        selectedMissionForEdit?.let { mission ->
            AddEditMissionDialog(
                missionToEdit = mission,
                customers = customers,
                onDismiss = { selectedMissionForEdit = null },
                onConfirm = { title, client, customerId, address, notes, code, date, time, priority, color ->
                    viewModel.updateMission(
                        mission.copy(
                            title = title,
                            client = client,
                            customerId = customerId,
                            address = address,
                            notes = notes,
                            entranceCode = code,
                            date = date,
                            time = time,
                            priority = priority,
                            priorityColor = color
                        )
                    )
                    true
                }
            )
        }

        // Customers List Popup
        if (showCustomersDialog) {
            CustomersListDialog(
                customers = customers,
                onDismiss = { showCustomersDialog = false },
                onAddNewCustomerClick = {
                    if (isSignedIn) showAddCustomerDialog = true else showGoogleSignInDialog = true
                },
                onEditCustomerClick = {
                    if (isSignedIn) selectedCustomerForEdit = it else showGoogleSignInDialog = true
                }
            )
        }

        // Add Customer Popup
        if (showAddCustomerDialog) {
            AddEditCustomerDialog(
                onDismiss = { showAddCustomerDialog = false },
                onConfirm = { name, phone, address, email, notes, entryPassword ->
                    viewModel.addCustomer(name, phone, address, email, notes, entryPassword)
                }
            )
        }

        // Edit Customer Popup
        selectedCustomerForEdit?.let { customer ->
            AddEditCustomerDialog(
                customerToEdit = customer,
                onDismiss = { selectedCustomerForEdit = null },
                onConfirm = { name, phone, address, email, notes, entryPassword ->
                    viewModel.updateCustomer(
                        customer.copy(
                            name = name,
                            phone = phone,
                            address = address,
                            email = email,
                            notes = notes,
                            entryPassword = entryPassword
                        )
                    )
                }
            )
        }

        if (showGoogleSignInDialog && !isSignedIn) {
            GoogleLoginScreen(
                clientConfigured = com.example.mygardens.BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank(),
                message = authMessage,
                onSignIn = onSignInClick,
                onDismiss = { showGoogleSignInDialog = false }
            )
        }
    }
}

@Composable
fun MissionsTopBar(
    openCount: Int,
    isDarkMode: Boolean,
    isSignedIn: Boolean = false,
    onToggleDarkMode: () -> Unit,
    onCustomersClick: () -> Unit,
    onSignInClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onNewMissionClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { /* TODO */ },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .size(44.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Sort,
                    contentDescription = "Sort",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = onCustomersClick,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = "Customers",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "לקוחות",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (!isSignedIn) {
                OutlinedButton(
                    onClick = onSignInClick,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "התחבר עם Google",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("התחברות", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                OutlinedButton(
                    onClick = onLogoutClick,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Logout,
                        contentDescription = "התנתקות",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "התנתקות",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            IconButton(
                onClick = onToggleDarkMode,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Toggle Dark Mode",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onNewMissionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.new_mission),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = stringResource(R.string.missions),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "$openCount פתוחות - 0 באיחור",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            onClick = { /* TODO */ },
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Tune,
                    contentDescription = "Filter",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.search_hint), fontSize = 14.sp) },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.Gray
                )
            },
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
fun FilterChips(
    selectedFilter: MissionFilter,
    onFilterSelected: (MissionFilter) -> Unit,
    missionsSize: Int,
    openCount: Int,
    doneCount: Int,
    todayCount: Int,
    urgentCount: Int
) {
    val filters = listOf(
        MissionFilter.ALL to (stringResource(R.string.filter_all) to missionsSize.toString()),
        MissionFilter.OPEN to (stringResource(R.string.filter_open) to openCount.toString()),
        MissionFilter.DONE to (stringResource(R.string.filter_done) to doneCount.toString()),
        MissionFilter.TODAY to (stringResource(R.string.filter_today) to todayCount.toString()),
        MissionFilter.HIGH_PRIORITY to (stringResource(R.string.filter_high) to urgentCount.toString())
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(filters) { (filterType, info) ->
            FilterChipItem(
                label = info.first,
                count = info.second,
                isSelected = selectedFilter == filterType,
                onClick = { onFilterSelected(filterType) }
            )
        }
    }
}

@Composable
fun FilterChipItem(label: String, count: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        tonalElevation = if (isSelected) 0.dp else 2.dp,
        border = if (isSelected) null else BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier.height(44.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(count, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                label,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp
            )
            if (!isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    count,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun InfoBanner() {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = stringResource(R.string.swipe_info),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.TouchApp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun MissionCard(
    mission: Mission,
    onToggleComplete: () -> Unit,
    onEditClick: () -> Unit,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    color = mission.priorityColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(mission.priorityColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            mission.priority,
                            color = mission.priorityColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = mission.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (mission.isCompleted) TextDecoration.LineThrough else null,
                        color = if (mission.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(
                        onClick = onToggleComplete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (mission.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = "Toggle Complete",
                            tint = if (mission.isCompleted) CompletedGreen else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    mission.client,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    Icons.Default.Business,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        mission.date,
                        color = if (mission.date == SimpleDateFormat(
                                "dd/MM/yyyy",
                                Locale.getDefault()
                            ).format(Date())
                        ) PriorityHigh else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        mission.time,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        color = if (mission.isCompleted) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = mission.status,
                            color = if (mission.isCompleted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSecondaryContainer,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEditClick, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            mission.assigneeInitial,
                            color = MaterialTheme.colorScheme.onTertiary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MissionDetailsDialog(mission: Mission, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.mission_details),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DetailItem(
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    label = "משימה:",
                    value = mission.title
                )
                DetailItem(
                    icon = Icons.Default.Person,
                    label = stringResource(R.string.customer),
                    value = mission.client
                )
                DetailItem(
                    icon = Icons.Default.Event,
                    label = stringResource(R.string.date_time),
                    value = "${mission.date} בשעה ${mission.time}"
                )
                DetailItem(
                    icon = Icons.Default.LocationOn,
                    label = stringResource(R.string.address),
                    value = mission.address
                )
                DetailItem(
                    icon = Icons.Default.Key,
                    label = "קוד כניסה:",
                    value = mission.entranceCode.ifEmpty { "אין קוד" })
                DetailItem(
                    icon = Icons.AutoMirrored.Filled.Notes,
                    label = stringResource(R.string.notes),
                    value = mission.notes
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close), fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMissionDialog(
    missionToEdit: Mission? = null,
    customers: List<Customer> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        client: String,
        customerId: Long?,
        address: String,
        notes: String,
        entranceCode: String,
        date: String,
        time: String,
        priority: String,
        priorityColor: Color
    ) -> Boolean
) {
    var title by remember { mutableStateOf(missionToEdit?.title ?: "") }
    var client by remember { mutableStateOf(missionToEdit?.client ?: "") }
    var selectedCustomerId by remember { mutableStateOf<Long?>(missionToEdit?.customerId) }
    var address by remember { mutableStateOf(missionToEdit?.address ?: "") }
    var notes by remember { mutableStateOf(missionToEdit?.notes ?: "") }
    var entranceCode by remember { mutableStateOf(missionToEdit?.entranceCode ?: "") }
    var date by remember {
        mutableStateOf(
            missionToEdit?.date ?: SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
            ).format(Date())
        )
    }
    var time by remember { mutableStateOf(missionToEdit?.time ?: "08:00") }

    var expandedClientDropdown by remember { mutableStateOf(false) }

    val lowLabel = stringResource(R.string.priority_low)
    val medLabel = stringResource(R.string.priority_medium)
    val highLabel = stringResource(R.string.priority_high)

    val priorities = remember(lowLabel, medLabel, highLabel) {
        listOf(
            Triple(lowLabel, PriorityLow, "Low"),
            Triple(medLabel, PriorityMedium, "Medium"),
            Triple(highLabel, PriorityHigh, "High")
        )
    }

    var selectedPriority by remember(priorities) {
        mutableStateOf(
            priorities.find { it.first == (missionToEdit?.priority ?: medLabel) } ?: priorities[1]
        )
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (missionToEdit == null) "הוספת משימה חדשה" else "עדכון פרטי משימה",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; showError = false },
                    label = { Text("כותרת המשימה") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Client Selection Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedClientDropdown,
                    onExpandedChange = { expandedClientDropdown = !expandedClientDropdown },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = client,
                        onValueChange = {
                            client = it
                            selectedCustomerId = null
                            showError = false
                        },
                        readOnly = customers.isNotEmpty(),
                        label = { Text("בחירת לקוח") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedClientDropdown)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                            .fillMaxWidth()
                    )

                    if (customers.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = expandedClientDropdown,
                            onDismissRequest = { expandedClientDropdown = false }
                        ) {
                            customers.forEach { customer ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(customer.name, fontWeight = FontWeight.Bold)
                                            if (customer.address.isNotBlank()) {
                                                Text(
                                                    customer.address,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        client = customer.name
                                        selectedCustomerId = customer.id.toLong()
                                        address = customer.address
                                        entranceCode = customer.entryPassword
                                        expandedClientDropdown = false
                                        showError = false
                                    }
                                )
                            }
                        }
                    }
                }

                Text(
                    "רמת דחיפות",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    priorities.forEach { priority ->
                        val isSelected = selectedPriority == priority
                        Surface(
                            onClick = { selectedPriority = priority },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) priority.second else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            border = if (isSelected) null else BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = priority.first,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("כתובת") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = entranceCode,
                    onValueChange = { entranceCode = it },
                    readOnly = selectedCustomerId != null && entranceCode.isNotBlank(),
                    label = { Text("קוד כניסה (סיסמת שער)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(date)
                    }
                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(time)
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("הערות") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                if (showError) {
                    Text(
                        "משימה זו כבר קיימת ברשימה!",
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && client.isNotBlank()) {
                        val success = onConfirm(
                            title,
                            client,
                            selectedCustomerId,
                            address,
                            notes,
                            entranceCode,
                            date,
                            time,
                            selectedPriority.first,
                            selectedPriority.second
                        )
                        if (success) onDismiss() else showError = true
                    }
                }
            ) {
                Text(if (missionToEdit == null) "הוסף" else "עדכן")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("ביטול") }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
                    }
                    showDatePicker = false
                }) { Text("אישור") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = time.split(":")[0].toInt(),
            initialMinute = time.split(":")[1].toInt()
        )
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "בחירת שעה",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                    TimePicker(state = timePickerState)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimePicker = false }) { Text("ביטול") }
                        TextButton(onClick = {
                            time = String.format(
                                Locale.getDefault(),
                                "%02d:%02d",
                                timePickerState.hour,
                                timePickerState.minute
                            )
                            showTimePicker = false
                        }) { Text("אישור") }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun EmptyMissionsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Inbox,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outlineVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "אין משימות תואמות לחיפוש",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CustomersListDialog(
    customers: List<Customer>,
    onDismiss: () -> Unit,
    onAddNewCustomerClick: () -> Unit,
    onEditCustomerClick: (Customer) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxHeight(0.85f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "רשימת לקוחות",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "סגור")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onAddNewCustomerClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "לקוח חדש", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (customers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "אין לקוחות ברשימה",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(customers, key = { it.id }) { customer ->
                            CustomerItemCard(
                                customer = customer,
                                onEditClick = { onEditCustomerClick(customer) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerItemCard(
    customer: Customer,
    onEditClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(onClick = onEditClick) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "ערוך לקוח",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            DetailItem(icon = Icons.Default.Phone, label = "טלפון:", value = customer.phone)
            Spacer(modifier = Modifier.height(6.dp))
            DetailItem(icon = Icons.Default.LocationOn, label = "כתובת:", value = customer.address)
            if (customer.entryPassword.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                DetailItem(
                    icon = Icons.Default.Key,
                    label = "קוד כניסה:",
                    value = customer.entryPassword
                )
            }
            if (customer.email.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                DetailItem(icon = Icons.Default.Email, label = "אימייל:", value = customer.email)
            }
            if (customer.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                DetailItem(
                    icon = Icons.AutoMirrored.Filled.Notes,
                    label = "הערות:",
                    value = customer.notes
                )
            }
        }
    }
}

@Composable
fun AddEditCustomerDialog(
    customerToEdit: Customer? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, address: String, email: String, notes: String, entryPassword: String) -> Unit
) {
    var name by remember { mutableStateOf(customerToEdit?.name ?: "") }
    var phone by remember { mutableStateOf(customerToEdit?.phone ?: "") }
    var address by remember { mutableStateOf(customerToEdit?.address ?: "") }
    var email by remember { mutableStateOf(customerToEdit?.email ?: "") }
    var notes by remember { mutableStateOf(customerToEdit?.notes ?: "") }
    var entryPassword by remember { mutableStateOf(customerToEdit?.entryPassword ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (customerToEdit == null) "הוספת לקוח חדש" else "עדכון פרטי לקוח",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("שם לקוח") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("מספר טלפון") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("כתובת") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("דוא\"ל") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = entryPassword,
                    onValueChange = { entryPassword = it },
                    label = { Text("קוד כניסה (סיסמת שער)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("הערות") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, phone, address, email, notes, entryPassword)
                        onDismiss()
                    }
                }
            ) {
                Text(if (customerToEdit == null) "הוסף" else "עדכן")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ביטול")
            }
        }
    )
}
