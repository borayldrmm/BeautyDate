/**
 * Working Hours Management Screen
 * Provides interface for managing business working hours with default settings and bulk operations
 */
package com.borayildirim.beautydate.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.borayildirim.beautydate.R
import com.borayildirim.beautydate.components.DayScheduleCard
import com.borayildirim.beautydate.data.models.DayOfWeek
import com.borayildirim.beautydate.viewmodels.WorkingHoursViewModel
import com.google.firebase.auth.FirebaseAuth
import com.borayildirim.beautydate.utils.ToastUtils
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkingHoursScreen(
    onNavigateBack: () -> Unit,
    viewModel: WorkingHoursViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // ViewModel automatically initializes working hours through AuthUtil
    // No manual initialization needed - follows MVVM + Dependency Injection pattern
    
    // Handle success messages
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            ToastUtils.showSuccess(context, message)
            viewModel.clearMessages()
        }
    }
    
    // Handle error messages
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            ToastUtils.showError(context, message)
            viewModel.clearMessages()
        }
    }
    
    // Dialog states
    var showApplyAllDialog by remember { mutableStateOf(false) }
    var showWeekdaysDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingDay by remember { mutableStateOf<DayOfWeek?>(null) }
    
    // Time selection states for dialogs
    var selectedStartTime by remember { mutableStateOf("09:00") }
    var selectedEndTime by remember { mutableStateOf("18:00") }
    
    // Dropdown expansion states
    var startTimeExpanded by remember { mutableStateOf(false) }
    var endTimeExpanded by remember { mutableStateOf(false) }
    var weekdaysStartExpanded by remember { mutableStateOf(false) }
    var weekdaysEndExpanded by remember { mutableStateOf(false) }
    
    // Available time options (6:00 to 23:30, 30-minute intervals)
    val timeOptions = remember {
        (6..23).flatMap { hour ->
            listOf("00", "30").map { minute ->
                "%02d:%s".format(hour, minute)
            }
        }.filter { time ->
            val (h, m) = time.split(":").map { it.toInt() }
            !(h == 23 && m == 30)
        }
    }

    // Dynamic weekday button text based on weekend status
    val weekdayButtonText = when (viewModel.getWeekendStatus()) {
        WorkingHoursViewModel.WeekendStatus.CLOSED -> stringResource(R.string.working_hours_apply_weekdays_weekend_closed)
        WorkingHoursViewModel.WeekendStatus.SATURDAY_ONLY -> stringResource(R.string.working_hours_apply_weekdays_weekend_open_without_sunday)
        WorkingHoursViewModel.WeekendStatus.OPEN -> stringResource(R.string.working_hours_apply_weekdays_weekend_open)
    }

    // Dynamic weekday dialog description based on weekend status
    val weekdayDialogDescription = when (viewModel.getWeekendStatus()) {
        WorkingHoursViewModel.WeekendStatus.CLOSED -> stringResource(R.string.working_hours_apply_weekdays_dialog_desc_weekend_closed)
        WorkingHoursViewModel.WeekendStatus.SATURDAY_ONLY -> stringResource(R.string.working_hours_apply_weekdays_dialog_desc_weekend_open)
        WorkingHoursViewModel.WeekendStatus.OPEN -> stringResource(R.string.working_hours_apply_weekdays_dialog_desc_weekend_open)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 20.dp)
    ) {
        // Header section with back button and title only
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Geri",
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Text(
                text = stringResource(R.string.working_hours_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Working hours list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (uiState.errorMessage != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "Bilinmeyen hata",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            } else {
                items(DayOfWeek.values()) { day ->
                    val dayHours = uiState.workingHours?.getDayHours(day)
                    if (dayHours != null) {
                        DayScheduleCard(
                            dayOfWeek = day,
                            dayHours = dayHours,
                            onEditClick = { 
                                editingDay = day
                                showEditDialog = true
                            },
                            onToggleOpen = {
                                viewModel.toggleDay(day)
                            }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Action buttons in 2x2 grid
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // First row: Apply buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Apply to all days button
                Button(
                    onClick = { showApplyAllDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tüm Günler")
                }
                
                // Apply to weekdays button
                Button(
                    onClick = { showWeekdaysDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.BusinessCenter,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(weekdayButtonText)
                }
            }
            
            // Second row: Sync and Reset buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sync button
                Button(
                    onClick = { viewModel.syncWorkingHours() },
                    enabled = !uiState.isSyncing,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    if (uiState.isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onTertiary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Senkronize")
                }
                
                // Reset button
                Button(
                    onClick = { showResetConfirmDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.RestoreFromTrash,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Varsayılan")
                }
            }
        }
    }
    
    // Edit Day Dialog
    if (showEditDialog && editingDay != null) {
        val currentDay = editingDay!!
        val currentDayHours = uiState.workingHours?.getDayHours(currentDay)
        var editStartTime by remember { mutableStateOf(currentDayHours?.startTime ?: "09:00") }
        var editEndTime by remember { mutableStateOf(currentDayHours?.endTime ?: "19:00") }
        var isWorking by remember { mutableStateOf(currentDayHours?.isWorking ?: true) }
        
        // Dropdown expanded states for edit dialog
        var editStartTimeExpanded by remember { mutableStateOf(false) }
        var editEndTimeExpanded by remember { mutableStateOf(false) }
        
        AlertDialog(
            onDismissRequest = { 
                showEditDialog = false
                editingDay = null
                editStartTimeExpanded = false
                editEndTimeExpanded = false
            },
            title = { 
                Text(stringResource(R.string.working_hours_edit_title, currentDay.displayName)) 
            },
            text = {
                Column {
                    // Working toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = isWorking,
                            onCheckedChange = { isWorking = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.working_hours_is_open))
                    }
                    
                    if (isWorking) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Start time dropdown
                        ExposedDropdownMenuBox(
                            expanded = editStartTimeExpanded,
                            onExpandedChange = { editStartTimeExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = editStartTime,
                                onValueChange = { },
                                readOnly = true,
                                label = { Text(stringResource(R.string.working_hours_start_time)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = editStartTimeExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )
                            
                            ExposedDropdownMenu(
                                expanded = editStartTimeExpanded,
                                onDismissRequest = { editStartTimeExpanded = false }
                            ) {
                                timeOptions.forEach { time ->
                                    DropdownMenuItem(
                                        text = { Text(time) },
                                        onClick = { 
                                            editStartTime = time
                                            editStartTimeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // End time dropdown
                        ExposedDropdownMenuBox(
                            expanded = editEndTimeExpanded,
                            onExpandedChange = { editEndTimeExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = editEndTime,
                                onValueChange = { },
                                readOnly = true,
                                label = { Text(stringResource(R.string.working_hours_end_time)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = editEndTimeExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )
                            
                            ExposedDropdownMenu(
                                expanded = editEndTimeExpanded,
                                onDismissRequest = { editEndTimeExpanded = false }
                            ) {
                                timeOptions.forEach { time ->
                                    DropdownMenuItem(
                                        text = { Text(time) },
                                        onClick = { 
                                            editEndTime = time
                                            editEndTimeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newDayHours = com.borayildirim.beautydate.data.models.DayHours(
                            isWorking = isWorking,
                            startTime = if (isWorking) editStartTime else "09:00",
                            endTime = if (isWorking) editEndTime else "19:00"
                        )
                        viewModel.updateDayHoursWithToast(currentDay, newDayHours)
                        showEditDialog = false
                        editingDay = null
                        editStartTimeExpanded = false
                        editEndTimeExpanded = false
                    }
                ) {
                    Text(stringResource(R.string.apply))
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showEditDialog = false
                    editingDay = null
                    editStartTimeExpanded = false
                    editEndTimeExpanded = false
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    
    // Reset Confirmation Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text(stringResource(R.string.working_hours_reset_confirm_title)) },
            text = {
                Text(stringResource(R.string.working_hours_reset_confirm_desc_updated))
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetToDefaults()
                    showResetConfirmDialog = false
                }) {
                    Text(stringResource(R.string.apply))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    
    // Apply to All Days Dialog
    if (showApplyAllDialog) {
        AlertDialog(
            onDismissRequest = { 
                showApplyAllDialog = false
                startTimeExpanded = false
                endTimeExpanded = false
            },
            title = { Text(stringResource(R.string.working_hours_apply_all_dialog_title)) },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.working_hours_apply_all_dialog_desc_updated),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Start time dropdown
                    ExposedDropdownMenuBox(
                        expanded = startTimeExpanded,
                        onExpandedChange = { startTimeExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedStartTime,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text(stringResource(R.string.working_hours_start_time)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = startTimeExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        
                        ExposedDropdownMenu(
                            expanded = startTimeExpanded,
                            onDismissRequest = { startTimeExpanded = false }
                        ) {
                            timeOptions.forEach { time ->
                                DropdownMenuItem(
                                    text = { Text(time) },
                                    onClick = { 
                                        selectedStartTime = time
                                        startTimeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // End time dropdown
                    ExposedDropdownMenuBox(
                        expanded = endTimeExpanded,
                        onExpandedChange = { endTimeExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedEndTime,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text(stringResource(R.string.working_hours_end_time)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = endTimeExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        
                        ExposedDropdownMenu(
                            expanded = endTimeExpanded,
                            onDismissRequest = { endTimeExpanded = false }
                        ) {
                            timeOptions.forEach { time ->
                                DropdownMenuItem(
                                    text = { Text(time) },
                                    onClick = { 
                                        selectedEndTime = time
                                        endTimeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.applyToAllDays(selectedStartTime, selectedEndTime)
                        showApplyAllDialog = false
                        startTimeExpanded = false
                        endTimeExpanded = false
                    }
                ) {
                    Text(stringResource(R.string.apply))
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showApplyAllDialog = false
                    startTimeExpanded = false
                    endTimeExpanded = false
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    
    // Apply to Weekdays Dialog
    if (showWeekdaysDialog) {
        AlertDialog(
            onDismissRequest = { 
                showWeekdaysDialog = false
                weekdaysStartExpanded = false
                weekdaysEndExpanded = false
            },
            title = { Text(stringResource(R.string.working_hours_apply_weekdays_dialog_title)) },
            text = {
                Column {
                    Text(
                        text = weekdayDialogDescription,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Start time dropdown
                    ExposedDropdownMenuBox(
                        expanded = weekdaysStartExpanded,
                        onExpandedChange = { weekdaysStartExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedStartTime,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text(stringResource(R.string.working_hours_start_time)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = weekdaysStartExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        
                        ExposedDropdownMenu(
                            expanded = weekdaysStartExpanded,
                            onDismissRequest = { weekdaysStartExpanded = false }
                        ) {
                            timeOptions.forEach { time ->
                                DropdownMenuItem(
                                    text = { Text(time) },
                                    onClick = { 
                                        selectedStartTime = time
                                        weekdaysStartExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // End time dropdown
                    ExposedDropdownMenuBox(
                        expanded = weekdaysEndExpanded,
                        onExpandedChange = { weekdaysEndExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedEndTime,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text(stringResource(R.string.working_hours_end_time)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = weekdaysEndExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        
                        ExposedDropdownMenu(
                            expanded = weekdaysEndExpanded,
                            onDismissRequest = { weekdaysEndExpanded = false }
                        ) {
                            timeOptions.forEach { time ->
                                DropdownMenuItem(
                                    text = { Text(time) },
                                    onClick = { 
                                        selectedEndTime = time
                                        weekdaysEndExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.applyToWeekdays(selectedStartTime, selectedEndTime)
                        showWeekdaysDialog = false
                        weekdaysStartExpanded = false
                        weekdaysEndExpanded = false
                    }
                ) {
                    Text(stringResource(R.string.apply))
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showWeekdaysDialog = false
                    weekdaysStartExpanded = false
                    weekdaysEndExpanded = false
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
} 