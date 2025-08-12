package com.borayildirim.beautydate.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.borayildirim.beautydate.data.models.PaymentMethod
import com.borayildirim.beautydate.viewmodels.FinanceViewModel
import com.borayildirim.beautydate.components.AppointmentCard
import java.text.NumberFormat
import java.util.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

/**
 * Finance Screen with Income/Expense separation, monthly filtering
 * Redesigned with proper navigation and enhanced financial overview
 * Memory efficient: tab-based content switching with filtered data
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    modifier: Modifier = Modifier,
    viewModel: FinanceViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("tr-TR"))
    
    var selectedMonth by remember { mutableStateOf(getCurrentMonthKey()) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // DatePicker state
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    
    // Get monthly options (last 12 months)
    val monthOptions = remember { getMonthOptions() }
    
    // Filter data based on selected month
    val monthlyPayments = remember(uiState.payments, selectedMonth) {
        if (selectedMonth == "Tümü") {
            uiState.payments
        } else {
            uiState.payments.filter { payment ->
                getMonthKeyFromTimestamp(payment.createdAt) == selectedMonth
            }
        }
    }
    
    val monthlyTransactions = remember(uiState.transactions, selectedMonth) {
        if (selectedMonth == "Tümü") {
            uiState.transactions
        } else {
            uiState.transactions.filter { transaction ->
                getMonthKeyFromTimestamp(transaction.createdAt) == selectedMonth
            }
        }
    }
    
    // Calculate monthly totals
    val monthlyTotals = remember(monthlyPayments, uiState.expenses, selectedMonth) {
        calculateMonthlyTotals(monthlyPayments, currencyFormat, uiState.expenses, selectedMonth)
    }
    
    // DatePicker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = java.util.Date(millis)
                            val localDate = date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                            val monthName = localDate.month.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("tr"))
                            selectedMonth = "$monthName ${localDate.year}"
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Tamam")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("İptal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    Column(modifier = modifier.fillMaxSize()) {
        // Top App Bar with enhanced navigation
        TopAppBar(
            title = {
                Text(
                    text = "Finans",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.Default.ArrowBack, 
                        contentDescription = "Geri"
                    )
                }
            },
            actions = {
                // Refresh button with loading indicator
                IconButton(onClick = { viewModel.refreshFinanceData() }) {
                    if (uiState.isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Refresh, 
                            contentDescription = "Senkronize Et"
                        )
                    }
                }
            }
        )
        
        // Make entire screen scrollable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Period Selection with DatePicker
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📅 Dönem Seçimi",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        
                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.height(40.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (selectedMonth == "Tümü") "Tarih Seç" else selectedMonth,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Quick filters
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(monthOptions) { month ->
                            FilterChip(
                                onClick = { 
                                    selectedMonth = month
                                    viewModel.filterByPeriod(month)
                                },
                                label = { 
                                    Text(
                                        text = month,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                selected = selectedMonth == month,
                                leadingIcon = if (selectedMonth == month) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                } else null
                            )
                        }
                    }
                }
            }
            
            // Enhanced Financial Summary with Income/Expense separation
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📊 Finansal Özet - ${if (selectedMonth == "Tümü") "Genel" else selectedMonth}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Income Section with fixed card sizes
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "💚 GELİRLER",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SummaryItem(
                                    title = "Toplam Gelir",
                                    amount = monthlyTotals.totalRevenue,
                                    icon = Icons.Default.TrendingUp,
                                    modifier = Modifier.weight(1f),
                                    isPositive = true
                                )
                                
                                SummaryItem(
                                    title = "Nakit",
                                    amount = monthlyTotals.cashRevenue,
                                    icon = Icons.Default.Payments,
                                    modifier = Modifier.weight(1f),
                                    isPositive = true
                                )
                                
                                SummaryItem(
                                    title = "Kart",
                                    amount = monthlyTotals.cardRevenue,
                                    icon = Icons.Default.CreditCard,
                                    modifier = Modifier.weight(1f),
                                    isPositive = true
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Expense Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "💸 GİDERLER",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.error
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SummaryItem(
                                    title = "Toplam Gider",
                                    amount = monthlyTotals.totalExpenses,
                                    icon = Icons.Default.TrendingDown,
                                    modifier = Modifier.weight(1f),
                                    isPositive = false
                                )
                                
                                SummaryItem(
                                    title = "Net Kar",
                                    amount = monthlyTotals.netProfit,
                                    icon = if (monthlyTotals.netProfitValue >= 0) Icons.Default.AttachMoney else Icons.Default.MoneyOff,
                                    modifier = Modifier.weight(1f),
                                    isPositive = monthlyTotals.netProfitValue >= 0
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Enhanced Tab Row with icons
            val tabs = listOf(
                "💰 Gelirler" to Icons.Default.TrendingUp,
                "📋 İşlemler" to Icons.Default.Receipt,
                "💸 Giderler" to Icons.Default.TrendingDown
            )
            
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, (title, icon) ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) },
                        icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp)) }
                    )
                }
            }
            
            // Enhanced content based on selected tab with fixed height
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp) // Fixed height for tab content
            ) {
                when (selectedTabIndex) {
                    0 -> IncomesContent(
                        payments = monthlyPayments,
                        isLoading = uiState.isLoading,
                        selectedPeriod = selectedMonth,
                        modifier = Modifier.fillMaxSize()
                    )
                    1 -> TransactionsContent(
                        transactions = monthlyTransactions,
                        isLoading = uiState.isLoading,
                        selectedPeriod = selectedMonth,
                        modifier = Modifier.fillMaxSize()
                    )
                    2 -> ExpensesContent(
                        selectedPeriod = selectedMonth,
                        isLoading = uiState.isLoading,
                        modifier = Modifier.fillMaxSize(),
                        viewModel = viewModel
                    )
                }
            }
            
            // Add bottom padding
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    
    // Handle error messages
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            // Show error message (can be enhanced with SnackBar)
        }
    }
}

// Helper functions
private fun getCurrentMonthKey(): String {
    val now = LocalDate.now()
    return "${now.month.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("tr"))} ${now.year}"
}

private fun getMonthOptions(): List<String> {
    val options = mutableListOf("Tümü")
    val current = LocalDate.now()
    
    for (i in 0..11) {
        val date = current.minusMonths(i.toLong())
        val monthName = date.month.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("tr"))
        options.add("$monthName ${date.year}")
    }
    
    return options
}

private fun getMonthKeyFromTimestamp(timestamp: com.google.firebase.Timestamp?): String {
    return timestamp?.let {
        val date = it.toDate()
        val localDate = date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
        val monthName = localDate.month.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("tr"))
        "$monthName ${localDate.year}"
    } ?: ""
}

data class MonthlyTotals(
    val totalRevenue: String,
    val cashRevenue: String,
    val cardRevenue: String,
    val totalExpenses: String,
    val netProfit: String,
    val netProfitValue: Double
)

private fun calculateMonthlyTotals(payments: List<com.borayildirim.beautydate.data.models.Payment>, currencyFormat: NumberFormat, expenses: List<com.borayildirim.beautydate.data.models.Expense> = emptyList(), selectedMonth: String = "Tümü"): MonthlyTotals {
    val completedPayments = payments.filter { it.status == com.borayildirim.beautydate.data.models.PaymentStatus.COMPLETED }
    
    val totalRevenue = completedPayments.sumOf { it.amount }
    val cashRevenue = completedPayments.filter { it.paymentMethod == PaymentMethod.CASH }.sumOf { it.amount }
    val cardRevenue = completedPayments.filter { it.paymentMethod == PaymentMethod.CREDIT_CARD }.sumOf { it.amount }
    
    // Calculate real expense data for selected month
    val monthlyExpenses = if (selectedMonth == "Tümü") {
        expenses
    } else {
        expenses.filter { expense ->
            getMonthKeyFromExpenseDate(expense.expenseDate) == selectedMonth
        }
    }
    
    val totalExpenses = monthlyExpenses.sumOf { it.amount }
    val netProfitValue = totalRevenue - totalExpenses
    
    return MonthlyTotals(
        totalRevenue = currencyFormat.format(totalRevenue),
        cashRevenue = currencyFormat.format(cashRevenue),
        cardRevenue = currencyFormat.format(cardRevenue),
        totalExpenses = currencyFormat.format(totalExpenses),
        netProfit = currencyFormat.format(netProfitValue),
        netProfitValue = netProfitValue
    )
}

@Composable
private fun SummaryItem(
    title: String,
    amount: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    isPositive: Boolean = true
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun IncomesContent(
    payments: List<com.borayildirim.beautydate.data.models.Payment>,
    isLoading: Boolean,
    selectedPeriod: String,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Gelirler yükleniyor...")
                    }
                }
            }
        } else if (payments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                            Icons.Default.AttachMoney,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "${if (selectedPeriod == "Tümü") "Henüz" else selectedPeriod + " için"} gelir bulunamadı",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tamamlanan randevulardan gelirler bu bölümde görünecek",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(
                items = payments,
                key = { payment -> payment.id }
            ) { payment ->
                PaymentCard(payment = payment)
            }
        }
    }
}

@Composable
private fun PaymentsContent(
    payments: List<com.borayildirim.beautydate.data.models.Payment>,
    isLoading: Boolean,
    selectedPeriod: String,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (payments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                            Icons.Default.Payment,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "$selectedPeriod için ödeme bulunamadı",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(
                items = payments,
                key = { payment -> payment.id }
            ) { payment ->
                PaymentCard(payment = payment)
            }
        }
    }
}

@Composable
private fun TransactionsContent(
    transactions: List<com.borayildirim.beautydate.data.models.Transaction>,
    isLoading: Boolean,
    selectedPeriod: String,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (transactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                            Icons.Default.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "$selectedPeriod için işlem bulunamadı",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(transactions) { transaction ->
                TransactionCard(transaction = transaction)
            }
        }
    }
}

@Composable
private fun PaymentCard(
    payment: com.borayildirim.beautydate.data.models.Payment
) {
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
                        text = payment.customerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = payment.serviceName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${payment.amount.toInt()} ₺",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (payment.paymentMethod) {
                                PaymentMethod.CASH -> Icons.Default.Money
                                PaymentMethod.CREDIT_CARD -> Icons.Default.CreditCard
                                PaymentMethod.BANK_TRANSFER -> Icons.Default.AccountBalance
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = payment.paymentMethod.getDisplayName(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionCard(
    transaction: com.borayildirim.beautydate.data.models.Transaction
) {
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
                        text = transaction.description,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = transaction.category.getDisplayName(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = "${transaction.amount.toInt()} ₺",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when (transaction.type) {
                        com.borayildirim.beautydate.data.models.TransactionType.INCOME -> MaterialTheme.colorScheme.primary
                        com.borayildirim.beautydate.data.models.TransactionType.EXPENSE -> MaterialTheme.colorScheme.error
                    }
                )
            }
        }
    }
}

/**
 * Expenses content with ExpenseRepository integration
 */
@Composable
private fun ExpensesContent(
    selectedPeriod: String,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    viewModel: FinanceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Filter expenses based on selected period
    val filteredExpenses = remember(uiState.expenses, selectedPeriod) {
        if (selectedPeriod == "Tümü") {
            uiState.expenses
        } else {
            uiState.expenses.filter { expense ->
                getMonthKeyFromExpenseDate(expense.expenseDate) == selectedPeriod
            }
        }
    }
    
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Giderler yükleniyor...")
                    }
                }
            }
        } else if (filteredExpenses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                            Icons.Default.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "${if (selectedPeriod == "Tümü") "Henüz" else selectedPeriod + " için"} gider bulunamadı",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "İşletme giderleri bu bölümde görünecek",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // Summary card for filtered expenses
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Dönem Gider Özeti",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Toplam Gider:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${filteredExpenses.sumOf { it.amount }.toInt()} ₺",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Gider Sayısı:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${filteredExpenses.size} adet",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
            // Expense items
            items(
                items = filteredExpenses,
                key = { expense -> expense.id }
            ) { expense ->
                ExpenseCard(expense = expense)
            }
        }
    }
}

/**
 * Helper function to get month key from expense date
 */
private fun getMonthKeyFromExpenseDate(expenseDate: String): String {
    return try {
        val date = java.time.LocalDate.parse(
            expenseDate,
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
        )
        val monthName = date.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.forLanguageTag("tr"))
        "$monthName ${date.year}"
    } catch (e: Exception) {
        ""
    }
}

/**
 * Expense card component for finance screen
 */
@Composable
private fun ExpenseCard(
    expense: com.borayildirim.beautydate.data.models.Expense
) {
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
                }
            }
        }
    }
}

/**
 * Extension function to get display name for PaymentMethod
 */
private fun PaymentMethod.getDisplayName(): String {
    return when (this) {
        PaymentMethod.CASH -> "Nakit"
        PaymentMethod.CREDIT_CARD -> "Kredi Kartı"
        PaymentMethod.BANK_TRANSFER -> "Banka Transferi"
    }
}