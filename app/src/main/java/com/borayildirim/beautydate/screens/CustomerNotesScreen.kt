package com.borayildirim.beautydate.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.borayildirim.beautydate.data.models.CustomerNote
import com.borayildirim.beautydate.screens.components.CustomerNoteCard
import com.borayildirim.beautydate.screens.components.CustomerNoteFormSheet
import com.borayildirim.beautydate.screens.components.CustomerSearchBar
import com.borayildirim.beautydate.viewmodels.CustomerNoteViewModel
import androidx.compose.ui.platform.LocalContext
import com.borayildirim.beautydate.utils.ToastUtils

/**
 * Customer notes screen for managing customer-specific notes
 * Material Design 3 compliant with modern UI/UX
 * Features: Search, Add, Edit, Delete notes with customer selection
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerNotesScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    customerNoteViewModel: CustomerNoteViewModel = hiltViewModel()
) {
    // Collect UI state
    val uiState by customerNoteViewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Initialize notes when screen opens
    LaunchedEffect(Unit) {
        customerNoteViewModel.initializeNotes()
    }
    
    // Show success messages with Toast
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            ToastUtils.showSuccess(context, message)
            customerNoteViewModel.clearMessages()
        }
    }
    
    // Show error messages with Toast
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            ToastUtils.showError(context, message)
            customerNoteViewModel.clearMessages()
        }
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Müşteri Notları",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Geri"
                    )
                }
            },
            actions = {
                // Sync button
                IconButton(
                    onClick = { customerNoteViewModel.syncNotes() },
                    enabled = !uiState.isSyncing
                ) {
                    if (uiState.isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
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
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search bar
            CustomerSearchBar(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = { customerNoteViewModel.searchNotes(it) },
                placeholder = "Müşteri adı, telefon veya not içeriği ara..."
            )
            
            // Statistics card
            if (uiState.notes.isNotEmpty()) {
                NoteStatisticsCard(
                    stats = uiState.noteStats,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Notes list
            Box(
                modifier = Modifier.weight(1f)
            ) {
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else if (uiState.isEmpty) {
                    EmptyNotesCard()
                } else if (uiState.isSearchEmpty) {
                    EmptySearchCard()
                } else {
                    NotesList(
                        notes = uiState.displayNotes,
                        onEditNote = { customerNoteViewModel.showEditNoteSheet(it) },
                        onDeleteNote = { customerNoteViewModel.deleteNote(it.id) },
                        isDeleting = uiState.isDeletingNote
                    )
                }
                
                // FAB for adding new note
                ExtendedFloatingActionButton(
                    onClick = { customerNoteViewModel.showAddNoteSheet() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Yeni Not")
                }
            }
        }
    }
    
    // Success/Error messages
    uiState.successMessage?.let { message ->
        LaunchedEffect(message) {
            // Show snackbar or toast
        }
    }
    
    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            // Show error snackbar or toast
        }
    }
    
    // Note form sheets
    if (uiState.showAddNoteSheet) {
        CustomerNoteFormSheet(
            title = "Yeni Not Ekle",
            customers = uiState.customers,
            selectedCustomer = uiState.selectedCustomer,
            noteTitle = uiState.noteTitle,
            noteContent = uiState.noteContent,
            isImportant = uiState.isImportant,
            isLoading = uiState.isCreatingNote,
            onCustomerSelected = { customerNoteViewModel.selectCustomer(it) },
            onTitleChanged = { customerNoteViewModel.updateNoteTitle(it) },
            onContentChanged = { customerNoteViewModel.updateNoteContent(it) },
            onImportantChanged = { customerNoteViewModel.updateIsImportant(it) },
            onSave = { customerNoteViewModel.createNote() },
            onDismiss = { customerNoteViewModel.hideNoteSheets() }
        )
    }
    
    if (uiState.showEditNoteSheet) {
        CustomerNoteFormSheet(
            title = "Not Düzenle",
            customers = uiState.customers,
            selectedCustomer = uiState.selectedCustomer,
            noteTitle = uiState.noteTitle,
            noteContent = uiState.noteContent,
            isImportant = uiState.isImportant,
            isLoading = uiState.isUpdatingNote,
            onCustomerSelected = { customerNoteViewModel.selectCustomer(it) },
            onTitleChanged = { customerNoteViewModel.updateNoteTitle(it) },
            onContentChanged = { customerNoteViewModel.updateNoteContent(it) },
            onImportantChanged = { customerNoteViewModel.updateIsImportant(it) },
            onSave = { customerNoteViewModel.updateNote() },
            onDismiss = { customerNoteViewModel.hideNoteSheets() },
            isEditMode = true
        )
    }
}

/**
 * Statistics card showing note counts
 */
@Composable
private fun NoteStatisticsCard(
    stats: com.borayildirim.beautydate.viewmodels.state.NoteStats,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                icon = Icons.Default.Notes,
                label = "Toplam",
                value = stats.totalNotes.toString(),
                color = MaterialTheme.colorScheme.primary
            )
            StatItem(
                icon = Icons.Default.Star,
                label = "Önemli",
                value = stats.importantNotes.toString(),
                color = Color(0xFFFF9800) // Orange
            )
            StatItem(
                icon = Icons.Default.People,
                label = "Müşteri",
                value = stats.customersWithNotes.toString(),
                color = Color(0xFF4CAF50) // Green
            )
            StatItem(
                icon = Icons.Default.Schedule,
                label = "Son 7 Gün",
                value = stats.recentNotes.toString(),
                color = Color(0xFF2196F3) // Blue
            )
        }
    }
}

/**
 * Individual statistic item
 */
@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

/**
 * Notes list with lazy loading
 */
@Composable
private fun NotesList(
    notes: List<CustomerNote>,
    onEditNote: (CustomerNote) -> Unit,
    onDeleteNote: (CustomerNote) -> Unit,
    isDeleting: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp) // Space for FAB
    ) {
        items(
            items = notes,
            key = { it.id }
        ) { note ->
            CustomerNoteCard(
                note = note,
                onEditClick = { onEditNote(note) },
                onDeleteClick = { onDeleteNote(note) },
                isDeleting = isDeleting
            )
        }
    }
}

/**
 * Empty state when no notes exist
 */
@Composable
private fun EmptyNotesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.NoteAdd,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "Henüz not eklenmemiş",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Müşterileriniz için not eklemek üzere + butonuna tıklayın",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Empty search results
 */
@Composable
private fun EmptySearchCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "Arama sonucu bulunamadı",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Farklı anahtar kelimeler deneyin",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
} 