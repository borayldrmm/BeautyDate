/**
 * Calendar Screen - Main appointment management interface
 * Dynamic, modern UI with Material Design 3 and working hours integration
 * Memory efficient: LazyColumn with computed properties and reactive state
 */
package com.borayildirim.beautydate.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.borayildirim.beautydate.components.LoadingWithBreathingLogo
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.borayildirim.beautydate.R
import com.borayildirim.beautydate.viewmodels.CalendarViewModel
import com.borayildirim.beautydate.viewmodels.state.TimeSlot
import com.borayildirim.beautydate.viewmodels.state.TimeSlotStatus
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel(),
    onNavigateToAddAppointment: (String, String) -> Unit, // date, time
    onNavigateToAppointmentDetail: (String) -> Unit, // appointment id
    onNavigateToWorkingHours: () -> Unit = {} // working hours navigation
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Initialize calendar when screen loads
    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.uid?.let { businessId ->
            viewModel.initializeCalendar()
        }
    }
    
    // Handle loading states with breathing logo
    if (uiState.isLoading) {
        LoadingWithBreathingLogo(
            message = "Takvim yükleniyor...",
            subMessage = "Randevular hazırlanıyor",
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        item {
            CalendarTopAppBar(
                currentDate = uiState.selectedDate,
                onSyncClick = { viewModel.syncAppointments() },
                isSyncing = uiState.isSyncing,
                isOnline = uiState.isOnline
            )
        }
        
        // Professional Monthly Calendar - All-in-one
        item {
            ProfessionalMonthlyCalendar(
                monthDays = uiState.currentMonthDays,
                selectedDate = uiState.selectedDate,
                selectedMonth = uiState.selectedMonth,
                selectedYear = uiState.selectedYear,
                onDateSelected = { date -> viewModel.selectDate(date) },
                onPreviousMonth = { viewModel.navigateToPreviousMonth() },
                onNextMonth = { viewModel.navigateToNextMonth() },
                onPreviousYear = { viewModel.navigatePrevious12Months() },
                onNextYear = { viewModel.navigateNext12Months() },
                appointments = uiState.appointments
            )
        }
        
        // Working Hours Warning (if not configured)
        if (uiState.workingHours == null) {
            item {
                WorkingHoursWarningCard(
                    onNavigateToWorkingHours = onNavigateToWorkingHours
                )
            }
        }
        
        // Time Slots Grid
        item {
            TimeSlotsSection(
                timeSlots = uiState.timeSlots,
                onSlotClick = { slot -> viewModel.selectTimeSlot(slot) },
                isWorkingDay = uiState.isWorkingDay
            )
        }
        
        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
    
    // Slot Selection Card (Animated)
    AnimatedVisibility(
        visible = uiState.showSlotSelectionCard,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeOut()
    ) {
        SlotSelectionCard(
            selectedSlot = uiState.selectedTimeSlot,
            selectedDate = uiState.selectedDate,
            onCreateAppointment = { date, time ->
                onNavigateToAddAppointment(date, time)
                viewModel.hideSlotSelectionCard()
            },
            onDismiss = { viewModel.hideSlotSelectionCard() }
        )
    }
    
    // Note: Month/Year selection now handled directly in calendar header
    // No popup dialogs needed - cleaner professional interface
    
    // Error/Success Messages
    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            // Show snackbar or toast - implement as needed
        }
    }
    
    uiState.successMessage?.let { message ->
        LaunchedEffect(message) {
            // Show snackbar or toast - implement as needed
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarTopAppBar(
    currentDate: LocalDate,
    onSyncClick: () -> Unit,
    isSyncing: Boolean,
    isOnline: Boolean
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Takvim",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        actions = {
            // Network status indicator
            if (!isOnline) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = "Çevrimdışı",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
            
            // Sync button
            IconButton(
                onClick = onSyncClick,
                enabled = !isSyncing && isOnline
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Senkronize Et"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun MonthSelectorRow(
    selectedDate: LocalDate,
    onMonthClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onMonthClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Ay Seç",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ProfessionalMonthlyCalendar(
    monthDays: List<LocalDate>,
    selectedDate: LocalDate,
    selectedMonth: Int,
    selectedYear: Int,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    appointments: List<com.borayildirim.beautydate.data.models.Appointment>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Compact Navigation Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Year navigation section - more compact
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 12 months backward (compact button)
                    IconButton(
                        onClick = onPreviousYear,
                        enabled = selectedYear > 2020,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "12 Ay Geri",
                            tint = if (selectedYear > 2020) 
                                MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    // Year display - smaller chip
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = selectedYear.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                    
                    // 12 months forward (compact button)
                    IconButton(
                        onClick = onNextYear,
                        enabled = selectedYear < 2030,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "12 Ay İleri",
                            tint = if (selectedYear < 2030) 
                                MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                
                // Month navigation section - more compact
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onPreviousMonth,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Önceki Ay",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    // Month display - smaller chip
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = LocalDate.of(selectedYear, selectedMonth, 1).format(
                                DateTimeFormatter.ofPattern("MMM", java.util.Locale.forLanguageTag("tr-TR"))
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = onNextMonth,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Sonraki Ay",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            // Minimal navigation helper
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "◀ 12Ay  •  1Ay ▶",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
                        // Compact days of week header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val dayNames = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
                dayNames.forEach { dayName ->
                    Text(
                        text = dayName,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            
                        // Compact Calendar grid (6 rows x 7 columns)
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(200.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(monthDays) { date ->
                    ProfessionalDayItem(
                        date = date,
                        isSelected = date == selectedDate,
                        isToday = date == LocalDate.now(),
                        isCurrentMonth = date.monthValue == selectedMonth,
                        appointmentCount = appointments.count { appointment ->
                            appointment.appointmentDate == date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        },
                        onDateSelected = onDateSelected
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Compact Selected Date Info
            Text(
                text = selectedDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy", java.util.Locale.forLanguageTag("tr-TR"))),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ProfessionalDayItem(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    isCurrentMonth: Boolean,
    appointmentCount: Int,
    onDateSelected: (LocalDate) -> Unit
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.secondary
        else -> Color.Transparent
    }
    
    val contentColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.onSecondary
        !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .clickable { if (isCurrentMonth) onDateSelected(date) },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = contentColor,
                fontSize = 12.sp
            )
            
            if (appointmentCount > 0 && isCurrentMonth) {
                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                   else if (isToday) MaterialTheme.colorScheme.onSecondary
                                   else MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Composable
private fun MonthlyCalendarSection(
    monthDays: List<LocalDate>,
    selectedDate: LocalDate,
    selectedMonth: Int,
    selectedYear: Int,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    appointments: List<com.borayildirim.beautydate.data.models.Appointment>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Month navigation header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Önceki Ay"
                    )
                }
                
                Text(
                    text = LocalDate.of(selectedYear, selectedMonth, 1).format(
                        DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale.forLanguageTag("tr-TR"))
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                IconButton(onClick = onNextMonth) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Sonraki Ay"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Days of week header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val dayNames = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
                dayNames.forEach { dayName ->
                    Text(
                        text = dayName,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Calendar grid (6 rows x 7 columns)
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(240.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(monthDays) { date ->
                    MonthDayItem(
                        date = date,
                        isSelected = date == selectedDate,
                        isToday = date == LocalDate.now(),
                        isCurrentMonth = date.monthValue == selectedMonth,
                        appointmentCount = appointments.count { appointment ->
                            appointment.appointmentDate == date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        },
                        onDateSelected = onDateSelected
                    )
                }
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
private fun MonthDayItem(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    isCurrentMonth: Boolean,
    appointmentCount: Int,
    onDateSelected: (LocalDate) -> Unit
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.secondary
        else -> Color.Transparent
    }
    
    val contentColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.onSecondary
        !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { onDateSelected(date) },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = contentColor,
                fontSize = 12.sp
            )
            
            if (appointmentCount > 0 && isCurrentMonth) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                   else if (isToday) MaterialTheme.colorScheme.onSecondary
                                   else MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Composable
private fun WeeklyCalendarSection(
    weekDays: List<LocalDate>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    appointments: List<com.borayildirim.beautydate.data.models.Appointment>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Week navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousWeek) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Önceki Hafta"
                    )
                }
                
                IconButton(onClick = onNextWeek) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Sonraki Hafta"
                    )
                }
            }
            
            // Week days
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(weekDays) { date ->
                    WeekDayItem(
                        date = date,
                        isSelected = date == selectedDate,
                        isToday = date == LocalDate.now(),
                        appointmentCount = appointments.count { appointment ->
                            appointment.appointmentDate == date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        },
                        onDateSelected = onDateSelected
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
private fun WeekDayItem(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    appointmentCount: Int,
    onDateSelected: (LocalDate) -> Unit
) {
    val dayName = when (date.dayOfWeek) {
        java.time.DayOfWeek.MONDAY -> "Pzt"
        java.time.DayOfWeek.TUESDAY -> "Sal"
        java.time.DayOfWeek.WEDNESDAY -> "Çar"
        java.time.DayOfWeek.THURSDAY -> "Per"
        java.time.DayOfWeek.FRIDAY -> "Cum"
        java.time.DayOfWeek.SATURDAY -> "Cmt"
        java.time.DayOfWeek.SUNDAY -> "Paz"
    }
    
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.surface
    }
    
    val contentColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.onSecondary
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable { onDateSelected(date) }
            .padding(12.dp)
            .width(60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = dayName,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor.copy(alpha = 0.8f)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
        
        if (appointmentCount > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                               else MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun WorkingHoursInfoCard(
    isWorkingDay: Boolean,
    workingHours: String,
    selectedDate: LocalDate
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isWorkingDay) 
                MaterialTheme.colorScheme.secondaryContainer 
            else 
                MaterialTheme.colorScheme.errorContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isWorkingDay) Icons.Default.Schedule else Icons.Default.EventBusy,
                contentDescription = null,
                tint = if (isWorkingDay) 
                    MaterialTheme.colorScheme.onSecondaryContainer 
                else 
                    MaterialTheme.colorScheme.onErrorContainer
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Çalışma Saatleri",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isWorkingDay) 
                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    else 
                        MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
                Text(
                    text = workingHours,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isWorkingDay) 
                        MaterialTheme.colorScheme.onSecondaryContainer 
                    else 
                        MaterialTheme.colorScheme.onErrorContainer
                )
            }
            
            if (!isWorkingDay) {
                Text(
                    text = "KAPALI",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.error,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Working Hours Warning Card - Shows when working hours are not configured
 * Modern Material Design 3 warning card with navigation action
 */
@Composable
private fun WorkingHoursWarningCard(
    onNavigateToWorkingHours: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Warning Icon
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Uyarı",
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(32.dp)
            )
            
            // Message Text
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Çalışma Saatleri Belirlenmemiş",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "Randevularınızı yönetmek için \"İşlemler\" menüsünden işletmenizin çalışma saatlerini güncelleyiniz.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )
            }
            
            // Action Button
            FilledTonalButton(
                onClick = onNavigateToWorkingHours,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                )
            ) {
                Text("Ayarla")
            }
        }
    }
}

@Composable
private fun TimeSlotsSection(
    timeSlots: List<TimeSlot>,
    onSlotClick: (TimeSlot) -> Unit,
    isWorkingDay: Boolean
) {
    if (!isWorkingDay) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.EventBusy,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Bu gün çalışma günü değil",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Randevu oluşturulamaz",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        return
    }
    
    if (timeSlots.isEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 3.dp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Saatler yükleniyor...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }
    
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        timeSlots.forEach { slot ->
            TimeSlotCard(
                timeSlot = slot,
                onClick = { onSlotClick(slot) }
            )
        }
    }
}

@Composable
private fun TimeSlotCard(
    timeSlot: TimeSlot,
    onClick: () -> Unit
) {
    // Custom color scheme for the new design
    val backgroundColor = when (timeSlot.status) {
        TimeSlotStatus.AVAILABLE -> Color(0xFFE8F5E8) // Light green for available
        TimeSlotStatus.BOOKED -> Color(0xFFFFEBEE) // Light red for booked
        TimeSlotStatus.PAST -> Color(0xFFF5F5F5) // Light gray for past
        TimeSlotStatus.OUTSIDE_WORKING_HOURS -> MaterialTheme.colorScheme.outline
    }
    
    val contentColor = when (timeSlot.status) {
        TimeSlotStatus.AVAILABLE -> Color(0xFF2E7D32) // Dark green
        TimeSlotStatus.BOOKED -> Color(0xFFC62828) // Dark red
        TimeSlotStatus.PAST -> Color(0xFF757575) // Dark gray
        TimeSlotStatus.OUTSIDE_WORKING_HOURS -> MaterialTheme.colorScheme.onSurface
    }
    
    val statusText = when (timeSlot.status) {
        TimeSlotStatus.AVAILABLE -> "Boş"
        TimeSlotStatus.BOOKED -> "Planlandı"
        TimeSlotStatus.PAST -> "Geçti"
        TimeSlotStatus.OUTSIDE_WORKING_HOURS -> "Çalışma Dışı"
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = timeSlot.isClickable) { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (timeSlot.isClickable) 2.dp else 0.dp
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time
            Text(
                text = timeSlot.formattedTime,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                modifier = Modifier.width(80.dp)
            )
            
            // Separator line
            VerticalDivider(
                modifier = Modifier
                    .height(30.dp)
                    .width(1.dp),
                color = contentColor.copy(alpha = 0.3f)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Content area (customer name or empty)
            if (timeSlot.status == TimeSlotStatus.BOOKED && timeSlot.appointment != null) {
                // Show customer name for booked slots
                Text(
                    text = timeSlot.appointment.customerName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = contentColor,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                // Empty space for other slots
                Spacer(modifier = Modifier.weight(1f))
            }
            
            // Status text (aligned to right)
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

@Composable
private fun SlotSelectionCard(
    selectedSlot: TimeSlot?,
    selectedDate: LocalDate,
    onCreateAppointment: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    selectedSlot ?: return
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Seçilen Slot Bilgileri",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Kapat"
                    )
                }
            }
            
            HorizontalDivider()
            
            // Date and time info
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy EEEE")),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${selectedSlot.formattedTime} - ${selectedSlot.time.plusHours(1).format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Müsait",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            
            // Create appointment button
            Button(
                onClick = {
                    val dateString = selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    val timeString = selectedSlot.formattedTime
                    onCreateAppointment(dateString, timeString)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Randevu Oluştur",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MonthSelectorDialog(
    currentMonth: Int,
    currentYear: Int,
    onMonthSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMonth by remember { mutableStateOf(currentMonth) }
    var selectedYear by remember { mutableStateOf(currentYear) }
    
    val months = listOf(
        "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
        "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"
    )
    
    val currentYearValue = LocalDate.now().year
    val yearRange = (currentYearValue - 2)..(currentYearValue + 5)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Ay ve Yıl Seç",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Month Selection
                Text(
                    text = "Ay",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(150.dp)
                ) {
                    items(months.size) { index ->
                        val month = index + 1
                        FilterChip(
                            onClick = { selectedMonth = month },
                            label = { 
                                Text(
                                    text = months[index],
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            selected = selectedMonth == month,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                // Year Selection
                Text(
                    text = "Yıl",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(yearRange.toList()) { year ->
                        FilterChip(
                            onClick = { selectedYear = year },
                            label = { 
                                Text(
                                    text = year.toString(),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            selected = selectedYear == year
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    onMonthSelected(selectedMonth, selectedYear)
                    onDismiss()
                }
            ) {
                Text("Tamam")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
} 