package com.borayildirim.beautydate.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.borayildirim.beautydate.data.models.ExpenseCategory
import com.borayildirim.beautydate.data.models.Expense
import com.borayildirim.beautydate.viewmodels.ExpenseViewModel
import com.borayildirim.beautydate.components.LoadingWithBreathingLogo
import com.borayildirim.beautydate.utils.ToastUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Business Expenses Screen with ExpenseRepository integration
 * Provides expense management with persistent storage and monthly filtering
 * Memory efficient: ViewModel-based state management with reactive UI updates
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessExpensesScreen(
    onNavigateBack: () -> Unit = {},
    expenseViewModel: ExpenseViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by expenseViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showEditExpenseDialog by remember { mutableStateOf(false) }
    var expenseToEdit by remember { mutableStateOf<Expense?>(null) }
    var selectedMonth by remember { mutableStateOf("Bu Ay") }
    var showMonthFilter by remember { mutableStateOf(false) }
    
    // Handle success messages
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            ToastUtils.showSuccess(context, message)
            expenseViewModel.clearSuccessMessage()
        }
    }
    
    // Handle error messages
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            ToastUtils.showError(context, message)
            expenseViewModel.clearErrorMessage()
        }
    }
    
    Column(modifier = modifier.fillMaxSize()) {
        // Top App Bar with enhanced actions
        TopAppBar(
            title = {
                Text(
                    text = "Giderler",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, 
                        contentDescription = "Geri"
                    )
                }
            },
            actions = {
                // Month filter button
                IconButton(onClick = { showMonthFilter = true }) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = "Aylık Filtre"
                    )
                }
                
                // Sync button
                IconButton(onClick = { expenseViewModel.syncExpenses() }) {
                    if (uiState.isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Sync,
                            contentDescription = "Senkronize Et"
                        )
                    }
                }
                
                // Add expense button
                IconButton(onClick = { showAddExpenseDialog = true }) {
                    Icon(
                        Icons.Default.Add, 
                        contentDescription = "Gider Ekle"
                    )
                }
            }
        )
        
        // Summary Card with monthly filtering
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📊 Gider Özeti - $selectedMonth",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    
                    Text(
                        text = uiState.formattedTotalExpenses,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "${uiState.expenseCount} gider kaydı",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Expenses List with loading and empty states
        when {
            uiState.isLoading -> {
                LoadingWithBreathingLogo(
                    message = "Giderler yükleniyor...",
                    subMessage = "Lütfen bekleyiniz",
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            !uiState.hasExpenses -> {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Henüz gider kaydı bulunmuyor",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Sağ üstteki + butonuna tıklayarak\nilk giderinizi ekleyebilirsiniz",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            else -> {
                LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.filteredExpenses,
                        key = { expense -> expense.id }
                    ) { expense ->
                        ExpenseCard(
                            expense = expense,
                            onEditExpense = { expense ->
                                expenseToEdit = expense
                                showEditExpenseDialog = true
                            },
                            onDeleteExpense = { expenseViewModel.deleteExpense(expense.id) }
                        )
                    }
                }
            }
        }
    }
    
    // Add Expense Dialog
    if (showAddExpenseDialog) {
        AddExpenseDialog(
            onDismiss = { showAddExpenseDialog = false },
            onExpenseAdded = { category, subcategory, amount, description, notes ->
                val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                expenseViewModel.addExpense(
                    category = category,
                    subcategory = subcategory,
                    amount = amount,
                    description = description,
                    expenseDate = currentDate,
                    notes = notes
                )
                showAddExpenseDialog = false
            }
        )
    }
    
    // Edit Expense Dialog
    if (showEditExpenseDialog && expenseToEdit != null) {
        EditExpenseDialog(
            expense = expenseToEdit!!,
            onDismiss = { 
                showEditExpenseDialog = false
                expenseToEdit = null
            },
            onExpenseUpdated = { category, subcategory, amount, description, notes ->
                expenseViewModel.updateExpense(
                    expenseId = expenseToEdit!!.id,
                    category = category,
                    subcategory = subcategory,
                    amount = amount,
                    description = description,
                    notes = notes
                )
                showEditExpenseDialog = false
                expenseToEdit = null
            }
        )
    }
    
    // Month Filter Dialog
    if (showMonthFilter) {
        MonthFilterDialog(
            selectedMonth = selectedMonth,
            onMonthSelected = { month ->
                selectedMonth = month
                showMonthFilter = false
                // TODO: Implement month filtering in ViewModel
            },
            onDismiss = { showMonthFilter = false }
        )
    }
}

@Composable
private fun ExpenseCard(
    expense: Expense,
    onDeleteExpense: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = expense.description,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${expense.category.icon} ${expense.subcategory}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (expense.notes.isNotBlank()) {
                        Text(
                            text = expense.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = expense.formattedAmount,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = expense.expenseDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Delete button
                    IconButton(
                        onClick = { showDeleteConfirmation = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Gider Sil",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Gider Silme Onayı") },
            text = { 
                Text("Bu gider kaydını kalıcı olarak silmek istediğinizden emin misiniz?\n\n${expense.description} - ${expense.formattedAmount}")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteExpense()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("İptal")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onExpenseAdded: (ExpenseCategory, String, Double, String, String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<ExpenseCategory?>(null) }
    var selectedSubcategory by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showSubcategoryPicker by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Row {
                TextButton(onClick = onDismiss) {
                    Text("İptal")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (selectedCategory != null && 
                            selectedSubcategory.isNotBlank() && 
                            amount.isNotBlank() && 
                            description.isNotBlank()) {
                            showConfirmDialog = true
                        } else {
                            ToastUtils.showError(context, "Lütfen tüm alanları doldurun")
                        }
                    }
                ) {
                    Text("Ekle")
                }
            }
        },
        title = {
            Text(
                text = "Yeni Gider Ekle",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Category Selection
                OutlinedButton(
                    onClick = { showCategoryPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Category, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = selectedCategory?.displayName ?: "Kategori Seçin",
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Subcategory Selection
                if (selectedCategory != null) {
                    OutlinedButton(
                        onClick = { showSubcategoryPicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.List, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedSubcategory.ifBlank { "Alt Kategori Seçin" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Amount Input
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Tutar (₺)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.AttachMoney, contentDescription = null)
                    }
                )
                
                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Açıklama") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Description, contentDescription = null)
                    }
                )
                
                // Notes Input
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notlar (Opsiyonel)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Note, contentDescription = null)
                    }
                )
            }
        }
    )
    
    // Category Picker Dialog
    if (showCategoryPicker) {
        CategoryPickerDialog(
            onCategorySelected = { category ->
                selectedCategory = category
                selectedSubcategory = "" // Reset subcategory when category changes
                showCategoryPicker = false
            },
            onDismiss = { showCategoryPicker = false }
        )
    }
    
    // Subcategory Picker Dialog
    if (showSubcategoryPicker && selectedCategory != null) {
        SubcategoryPickerDialog(
            category = selectedCategory!!,
            onSubcategorySelected = { subcategory ->
                selectedSubcategory = subcategory
                showSubcategoryPicker = false
            },
            onDismiss = { showSubcategoryPicker = false }
        )
    }
    
    // Confirmation Dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            confirmButton = {
                Row {
                    TextButton(onClick = { showConfirmDialog = false }) {
                        Text("İptal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onExpenseAdded(
                                selectedCategory!!,
                                selectedSubcategory,
                                amount.toDoubleOrNull() ?: 0.0,
                                description,
                                notes
                            )
                            showConfirmDialog = false
                        }
                    ) {
                        Text("Onayla")
                    }
                }
            },
            title = { Text("Gider Onayı") },
            text = {
                Column {
                    Text("Aşağıdaki gider kaydedilecek:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Kategori: ${selectedCategory?.displayName}")
                    Text("• Alt Kategori: $selectedSubcategory") 
                    Text("• Tutar: ${amount} ₺")
                    Text("• Açıklama: $description")
                    if (notes.isNotBlank()) {
                        Text("• Notlar: $notes")
                    }
                }
            }
        )
    }
}

/**
 * Edit Expense Dialog Component
 * Pre-filled with existing expense data for editing
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditExpenseDialog(
    expense: Expense,
    onDismiss: () -> Unit,
    onExpenseUpdated: (ExpenseCategory, String, Double, String, String) -> Unit
) {
    var selectedCategory by remember { 
        mutableStateOf(expense.category) 
    }
    var selectedSubcategory by remember { mutableStateOf(expense.subcategory) }
    var amount by remember { mutableStateOf(expense.amount.toString()) }
    var description by remember { mutableStateOf(expense.description) }
    var notes by remember { mutableStateOf(expense.notes) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showSubcategoryPicker by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Row {
                TextButton(onClick = onDismiss) {
                    Text("İptal")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (selectedSubcategory.isNotBlank() && 
                            amount.isNotBlank() && 
                            description.isNotBlank()) {
                            showConfirmDialog = true
                        } else {
                            ToastUtils.showError(context, "Lütfen tüm alanları doldurun")
                        }
                    }
                ) {
                    Text("Güncelle")
                }
            }
        },
        title = {
            Text(
                text = "Gider Düzenle",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Category selection
                OutlinedTextField(
                    value = selectedCategory.displayName,
                    onValueChange = { },
                    label = { Text("Kategori") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showCategoryPicker = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Subcategory selection
                OutlinedTextField(
                    value = selectedSubcategory,
                    onValueChange = { },
                    label = { Text("Alt Kategori") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showSubcategoryPicker = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Amount input
                OutlinedTextField(
                    value = amount,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            amount = newValue
                        }
                    },
                    label = { Text("Tutar (₺)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Description input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Açıklama") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Notes input
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notlar (Opsiyonel)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        }
    )
    
    // Category picker dialog
    if (showCategoryPicker) {
        CategoryPickerDialog(
            onCategorySelected = { category ->
                selectedCategory = category
                selectedSubcategory = ""
                showCategoryPicker = false
            },
            onDismiss = { showCategoryPicker = false }
        )
    }
    
    // Subcategory picker dialog
    if (showSubcategoryPicker) {
        SubcategoryPickerDialog(
            category = selectedCategory,
            onSubcategorySelected = { subcategory ->
                selectedSubcategory = subcategory
                showSubcategoryPicker = false
            },
            onDismiss = { showSubcategoryPicker = false }
        )
    }
    
    // Confirmation dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        amount.toDoubleOrNull()?.let { amountValue ->
                            onExpenseUpdated(selectedCategory, selectedSubcategory, amountValue, description, notes)
                        }
                        showConfirmDialog = false
                    }
                ) {
                    Text("Onayla")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("İptal")
                }
            },
            title = { Text("Gider Güncelleme Onayı") },
            text = {
                Column {
                    Text("Aşağıdaki değişiklikler kaydedilecek:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Kategori: ${selectedCategory.displayName}")
                    Text("• Alt Kategori: $selectedSubcategory") 
                    Text("• Tutar: ${amount} ₺")
                    Text("• Açıklama: $description")
                    if (notes.isNotBlank()) {
                        Text("• Notlar: $notes")
                    }
                }
            }
        )
    }
}

@Composable
private fun CategoryPickerDialog(
    onCategorySelected: (ExpenseCategory) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Kategori Seçin") },
        text = {
            LazyColumn {
                items(ExpenseCategory.values()) { category ->
                    TextButton(
                        onClick = { onCategorySelected(category) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = category.displayName,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

@Composable
private fun SubcategoryPickerDialog(
    category: ExpenseCategory,
    onSubcategorySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Alt Kategori Seçin") },
        text = {
            LazyColumn {
                items(category.subcategories) { subcategory ->
                    TextButton(
                        onClick = { onSubcategorySelected(subcategory) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = subcategory,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

@Composable
private fun MonthFilterDialog(
    selectedMonth: String,
    onMonthSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val months = remember {
        listOf(
            "Bu Ay",
            "Geçen Ay", 
            "Son 3 Ay",
            "Son 6 Ay",
            "Bu Yıl",
            "Tümü"
        )
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dönem Seçin") },
        text = {
            LazyColumn {
                items(months) { month ->
                    TextButton(
                        onClick = { onMonthSelected(month) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = month)
                            if (month == selectedMonth) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat")
            }
        }
    )
}

/**
 * Expense Card Component with Edit and Delete actions
 * Memory efficient: minimal state with Material Design 3 styling
 */
@Composable
private fun ExpenseCard(
    expense: Expense,
    onEditExpense: (Expense) -> Unit,
    onDeleteExpense: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Açıklama ve Fiyat (üst satır)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = expense.description,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = expense.formattedAmount,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            // Kategori İkonu + İsmi ve Tarih (ikinci satır)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = expense.category.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = expense.expenseDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Alt kategori (girintili)
            Text(
                text = expense.subcategory,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 32.dp)
            )
            
            // Notes (eğer varsa)
            if (expense.notes.isNotBlank()) {
                Text(
                    text = expense.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 32.dp)
                )
            }
            
            // Düzenle ve Sil butonları
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = { onEditExpense(expense) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Düzenle",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                IconButton(
                    onClick = { showDeleteConfirmation = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Sil",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Gider Silme Onayı") },
            text = { 
                Text("Bu gider kaydını silmek istediğinizden emin misiniz?\n\n\"${expense.description}\" - ${expense.formattedAmount}") 
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteExpense()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("İptal")
                }
            }
        )
    }
}