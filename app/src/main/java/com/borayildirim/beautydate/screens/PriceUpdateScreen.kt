package com.borayildirim.beautydate.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import com.borayildirim.beautydate.data.models.Service
import com.borayildirim.beautydate.data.models.ServiceCategory
import com.borayildirim.beautydate.screens.components.BulkPriceUpdateSheet
import com.borayildirim.beautydate.viewmodels.ServiceViewModel
import com.borayildirim.beautydate.viewmodels.state.PendingPriceUpdate
import com.borayildirim.beautydate.utils.ToastUtils

/**
 * Price update screen for bulk service price management
 * Material Design 3 compliant with dynamic colors and modern UI
 * Features: Service list with prices, bulk update actions, category filtering
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceUpdateScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    serviceViewModel: ServiceViewModel = hiltViewModel()
) {
    // Collect states from ServiceViewModel
    val uiState by serviceViewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Screen state
    var showBulkUpdateSheet by remember { mutableStateOf(false) }
    var selectedAction by remember { mutableStateOf<PriceAction?>(null) }
    var expandedActions by remember { mutableStateOf(false) }
    
    // Load services when screen opens
    LaunchedEffect(Unit) {
        serviceViewModel.initializeServices()
    }
    
    // Handle success messages
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            ToastUtils.showSuccess(context, message)
            serviceViewModel.clearSuccessMessage()
        }
    }
    
    // Handle error messages
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            ToastUtils.showError(context, message)
            serviceViewModel.clearError()
        }
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Fiyatları Güncelle",
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
                // Manual sync button
                IconButton(
                    onClick = { serviceViewModel.manualSync() }
                ) {
                    if (uiState.isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Senkronize Et",
                            tint = MaterialTheme.colorScheme.onSurface
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
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Category filter section
                CategoryFilterSection(
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = { category ->
                        serviceViewModel.filterByCategory(category)
                    }
                )
                
                // Services list
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    ServicesList(
                        services = uiState.displayServices,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Action buttons section
                PriceActionButtons(
                    expanded = expandedActions,
                    onToggleExpanded = { expandedActions = !expandedActions },
                    onActionSelected = { action ->
                        selectedAction = action
                        showBulkUpdateSheet = true
                        expandedActions = false
                    }
                )
            }
        }
    }
    
    // Bulk update bottom sheet
    if (showBulkUpdateSheet && selectedAction != null) {
        BulkPriceUpdateSheet(
            onDismiss = { 
                showBulkUpdateSheet = false
                selectedAction = null
            },
            serviceViewModel = serviceViewModel,
            actionType = selectedAction!!.id
        )
    }
    
    // Price Update Confirmation Dialog
    if (uiState.showPriceUpdateConfirmation && uiState.pendingPriceUpdate != null) {
        PriceUpdateConfirmationDialog(
            pendingUpdate = uiState.pendingPriceUpdate!!,
            onConfirm = { serviceViewModel.confirmBulkUpdate() },
            onCancel = { serviceViewModel.cancelBulkUpdate() }
        )
    }
}

/**
 * Category filter section with chips
 */
@Composable
private fun CategoryFilterSection(
    selectedCategory: ServiceCategory?,
    onCategorySelected: (ServiceCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Kategori Filtresi",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Category chips with horizontal scrolling
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // All categories chip
                item {
                    FilterChip(
                        onClick = { onCategorySelected(null) },
                        label = { Text("Tümü") },
                        selected = selectedCategory == null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
                
                // Individual category chips
                items(ServiceCategory.values().toList()) { category ->
                    FilterChip(
                        onClick = { onCategorySelected(category) },
                        label = { Text(category.getDisplayName()) },
                        selected = selectedCategory == category,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }
    }
}

/**
 * Services list with prices
 */
@Composable
private fun ServicesList(
    services: List<Service>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp) // Space for action buttons
    ) {
        if (services.isEmpty()) {
            item {
                EmptyServicesCard()
            }
        } else {
            items(
                items = services,
                key = { it.id }
            ) { service ->
                ServicePriceCard(service = service)
            }
        }
    }
}

/**
 * Individual service card showing price information
 */
@Composable
private fun ServicePriceCard(
    service: Service,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
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
            // Service info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Category badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.wrapContentSize()
                ) {
                    Text(
                        text = service.category.getDisplayName(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                
                // Service name
                Text(
                    text = service.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Description if available
                if (service.description.isNotBlank()) {
                    Text(
                        text = service.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Price section
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = service.formattedPrice,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

/**
 * Empty state card when no services found
 */
@Composable
private fun EmptyServicesCard() {
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
                text = "Bu kategoride hizmet bulunamadı",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Farklı bir kategori seçin veya yeni hizmet ekleyin",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Price action buttons with expandable menu
 */
@Composable
private fun PriceActionButtons(
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onActionSelected: (PriceAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Main toggle button
        Button(
            onClick = onToggleExpanded,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
                Text(
                    text = "Fiyat İşlemleri",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
        
        // Expandable action buttons
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                getPriceActions().forEach { action ->
                    ActionButton(
                        action = action,
                        onClick = { onActionSelected(action) }
                    )
                }
            }
        }
    }
}

/**
 * Individual action button
 */
@Composable
private fun ActionButton(
    action: PriceAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = action.backgroundColor.copy(alpha = 0.1f),
            contentColor = action.backgroundColor
        ),
        border = BorderStroke(
            width = 2.dp,
            color = action.backgroundColor
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = action.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Price action data class
 */
data class PriceAction(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val backgroundColor: Color
)

/**
 * Available price actions
 */
private fun getPriceActions(): List<PriceAction> = listOf(
    PriceAction(
        id = "increase_percentage",
        title = "Yüzde Artış Uygula",
        icon = Icons.Default.TrendingUp,
        backgroundColor = Color(0xFF4CAF50) // Green
    ),
    PriceAction(
        id = "decrease_percentage", 
        title = "Yüzde İndirim Uygula",
        icon = Icons.Default.TrendingDown,
        backgroundColor = Color(0xFFFF5722) // Deep Orange
    ),
    PriceAction(
        id = "set_fixed_amount",
        title = "Sabit Tutar Belirle",
        icon = Icons.Default.AttachMoney,
        backgroundColor = Color(0xFF2196F3) // Blue
    ),
    PriceAction(
        id = "round_prices",
        title = "Fiyatları Yuvarla",
        icon = Icons.Default.Refresh,
        backgroundColor = Color(0xFF9C27B0) // Purple
    )
)

/**
 * Price Update Confirmation Dialog
 * Shows confirmation before executing bulk price updates
 */
@Composable
private fun PriceUpdateConfirmationDialog(
    pendingUpdate: PendingPriceUpdate,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Fiyat Güncelleme Onayı") },
        text = {
            Column {
                Text("İşletmenizdeki hizmetlerin fiyatlarında aşağıdaki değişikliği yapmak istediğinizden emin misiniz?")
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = pendingUpdate.getUpdateDescription(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Bu işlem geri alınamaz.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Evet, Onayla")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("İptal Et")
            }
        }
    )
} 