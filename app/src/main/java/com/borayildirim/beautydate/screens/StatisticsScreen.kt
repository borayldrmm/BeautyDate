package com.borayildirim.beautydate.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.borayildirim.beautydate.data.models.StatisticsPeriod
import com.borayildirim.beautydate.data.models.StatisticsCategory
import com.borayildirim.beautydate.viewmodels.StatisticsViewModel
import com.borayildirim.beautydate.utils.ToastUtils
import com.borayildirim.beautydate.components.LoadingWithBreathingLogo

/**
 * Statistics screen with comprehensive business analytics
 * Features: Period filtering, category tabs, charts, KPI cards
 * Memory efficient: LazyColumn with minimal recomposition
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    statisticsViewModel: StatisticsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by statisticsViewModel.uiState.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Handle error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            ToastUtils.showError(context, error)
            statisticsViewModel.clearError()
        }
    }
    
    // Handle export messages
    LaunchedEffect(uiState.exportMessage) {
        uiState.exportMessage?.let { message ->
            ToastUtils.showInfo(context, message)
            statisticsViewModel.clearExportMessage()
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "İstatistikler",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                }
            },
            actions = {
                IconButton(onClick = { statisticsViewModel.refreshStatistics() }) {
                    Icon(
                        Icons.Default.Refresh, 
                        contentDescription = "Yenile",
                        tint = if (uiState.isRefreshing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )
        
        if (uiState.isLoading) {
            // Loading state with breathing logo
            LoadingWithBreathingLogo(
                message = "İstatistikler hesaplanıyor...",
                subMessage = "Lütfen bekleyiniz",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Period Filter
                item {
                    PeriodFilterSection(
                        selectedPeriod = uiState.selectedPeriod,
                        onPeriodSelected = { statisticsViewModel.changePeriod(it) }
                    )
                }
                
                // Category Tabs
                item {
                    CategoryTabsSection(
                        selectedCategory = uiState.selectedCategory,
                        onCategorySelected = { statisticsViewModel.changeCategory(it) }
                    )
                }
                
                // Key Metrics Overview (always shown)
                if (uiState.hasData) {
                    item {
                        KeyMetricsOverview(metrics = uiState.keyMetrics)
                    }
                }
                
                // Financial Statistics
                if (statisticsViewModel.shouldShowFinancialStats() && uiState.financialStats != null) {
                    item {
                        FinancialStatisticsCard(stats = uiState.financialStats!!)
                    }
                }
                
                // Customer Statistics
                if (statisticsViewModel.shouldShowCustomerStats() && uiState.customerStats != null) {
                    item {
                        CustomerStatisticsCard(stats = uiState.customerStats!!)
                    }
                }
                
                // Appointment Statistics
                if (statisticsViewModel.shouldShowAppointmentStats() && uiState.appointmentStats != null) {
                    item {
                        AppointmentStatisticsCard(stats = uiState.appointmentStats!!)
                    }
                }
                
                // Employee Statistics
                if (statisticsViewModel.shouldShowEmployeeStats() && uiState.employeeStats != null) {
                    item {
                        EmployeeStatisticsCard(stats = uiState.employeeStats!!)
                    }
                }
                
                // Service Statistics
                if (statisticsViewModel.shouldShowServiceStats() && uiState.serviceStats != null) {
                    item {
                        ServiceStatisticsCard(stats = uiState.serviceStats!!)
                    }
                }
                
                // Export Section
                if (uiState.hasData) {
                    item {
                        ExportSection(
                            isExporting = uiState.isExporting,
                            onExport = { format -> statisticsViewModel.exportStatistics(format) }
                        )
                    }
                }
                
                // Empty state
                if (!uiState.hasData && !uiState.isLoading) {
                    item {
                        EmptyStatisticsState()
                    }
                }
            }
        }
    }
}

/**
 * Period filter chips
 */
@Composable
private fun PeriodFilterSection(
    selectedPeriod: StatisticsPeriod,
    onPeriodSelected: (StatisticsPeriod) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Dönem Seçimi",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(StatisticsPeriod.values()) { period ->
                    FilterChip(
                        onClick = { onPeriodSelected(period) },
                        label = { Text(period.displayName) },
                        selected = period == selectedPeriod
                    )
                }
            }
        }
    }
}

/**
 * Category tabs section
 */
@Composable
private fun CategoryTabsSection(
    selectedCategory: StatisticsCategory,
    onCategorySelected: (StatisticsCategory) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Kategori",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(StatisticsCategory.values()) { category ->
                    FilterChip(
                        onClick = { onCategorySelected(category) },
                        label = { Text(category.displayName) },
                        selected = category == selectedCategory
                    )
                }
            }
        }
    }
}

/**
 * Key metrics overview cards
 */
@Composable
private fun KeyMetricsOverview(
    metrics: com.borayildirim.beautydate.viewmodels.state.KeyMetrics
) {
    Column {
        Text(
            text = "Özet Göstergeler",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // Grid layout for better proportions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Toplam Gelir",
                value = metrics.totalRevenue,
                icon = Icons.Default.AttachMoney,
                color = Color(0xFF4CAF50)
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Net Kar",
                value = metrics.netProfit,
                icon = Icons.Default.TrendingUp,
                color = Color(0xFF2196F3)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Müşteriler",
                value = metrics.totalCustomers.toString(),
                icon = Icons.Default.People,
                color = Color(0xFF9C27B0)
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Randevular",
                value = metrics.completedAppointments.toString(),
                icon = Icons.Default.Event,
                color = Color(0xFFFF9800)
            )
        }
    }
}

/**
 * Individual metric card component - Fixed height for consistency
 */
@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String = "",
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier.height(100.dp), // Fixed height for all cards
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = color,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1
            )
            
            // Remove subtitle (percentage) to prevent overflow
            // if (subtitle.isNotBlank()) {
            //     Text(
            //         text = subtitle,
            //         style = MaterialTheme.typography.bodySmall,
            //         color = color.copy(alpha = 0.8f),
            //         textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            //         maxLines = 1
            //     )
            // }
        }
    }
}

/**
 * Financial statistics detailed card
 */
@Composable
private fun FinancialStatisticsCard(
    stats: com.borayildirim.beautydate.data.models.FinancialStatistics
) {
    StatisticsCard(
        title = "Finansal Durum",
        icon = Icons.Default.AccountBalance,
        color = Color(0xFF4CAF50)
    ) {
        StatisticRow("Toplam Gelir", stats.formattedTotalRevenue)
        StatisticRow("Toplam Gider", "${stats.totalExpenses.toInt()} ₺")
        StatisticRow("Net Kar", stats.formattedNetProfit)
        
        Spacer(modifier = Modifier.height(8.dp))
        
        StatisticRow("Nakit Ödemeler", "${stats.cashPayments.toInt()} ₺")
        StatisticRow("Kredi Kartı", "${stats.creditPayments.toInt()} ₺")
        StatisticRow("Bekleyen Ödemeler", "${stats.pendingPayments.toInt()}")
    }
}

/**
 * Customer statistics card
 */
@Composable
private fun CustomerStatisticsCard(
    stats: com.borayildirim.beautydate.data.models.CustomerStatistics
) {
    StatisticsCard(
        title = "Müşteri Analizi",
        icon = Icons.Default.People,
        color = Color(0xFF9C27B0)
    ) {
        StatisticRow("Toplam Müşteri", stats.totalCustomers.toString())
        StatisticRow("Aktif Müşteri", stats.activeCustomers.toString())
        StatisticRow("Bu Ay Yeni", stats.newCustomersThisMonth.toString())
        StatisticRow("Büyüme Oranı", stats.formattedGrowthRate)
        StatisticRow("Ortalama Değer", stats.formattedAverageValue)
        StatisticRow("Notlu Müşteri", stats.customersWithNotes.toString())
    }
}

/**
 * Appointment statistics card
 */
@Composable
private fun AppointmentStatisticsCard(
    stats: com.borayildirim.beautydate.data.models.AppointmentStatistics
) {
    StatisticsCard(
        title = "Randevu Analizi",
        icon = Icons.Default.Event,
        color = Color(0xFFFF9800)
    ) {
        StatisticRow("Toplam Randevu", stats.totalAppointments.toString())
        StatisticRow("Tamamlanan", stats.completedAppointments.toString())
        StatisticRow("Yaklaşan", stats.upcomingAppointments.toString())
        StatisticRow("Kaçırılan", stats.missedAppointments.toString())
        StatisticRow("İptal Edilen", stats.cancelledAppointments.toString())
        StatisticRow("Tamamlanma Oranı", stats.formattedCompletionRate)
        StatisticRow("Ortalama Değer", stats.formattedAverageValue)
    }
}

/**
 * Employee statistics card
 */
@Composable
private fun EmployeeStatisticsCard(
    stats: com.borayildirim.beautydate.data.models.EmployeeStatistics
) {
    StatisticsCard(
        title = "Personel Analizi",
        icon = Icons.Default.Badge,
        color = Color(0xFF2196F3)
    ) {
        StatisticRow("Toplam Personel", stats.totalEmployees.toString())
        StatisticRow("Aktif Personel", stats.activeEmployees.toString())
        StatisticRow("Aktif Oranı", stats.formattedActivePercentage)
        StatisticRow("Ortalama Yetenek", stats.formattedAverageSkills)
        if (stats.mostSkillfulEmployee.isNotEmpty()) {
            StatisticRow("En Yetenekli", stats.mostSkillfulEmployee)
        }
    }
}

/**
 * Service statistics card
 */
@Composable
private fun ServiceStatisticsCard(
    stats: com.borayildirim.beautydate.data.models.ServiceStatistics
) {
    StatisticsCard(
        title = "Hizmet Analizi",
        icon = Icons.Default.Build,
        color = Color(0xFFE91E63)
    ) {
        StatisticRow("Toplam Hizmet", stats.totalServices.toString())
        StatisticRow("Aktif Hizmet", stats.activeServices.toString())
        StatisticRow("Ortalama Fiyat", stats.formattedAveragePrice)
        StatisticRow("Fiyat Aralığı", stats.formattedPriceRange)
        if (stats.mostPopularService.isNotEmpty()) {
            StatisticRow("En Popüler", stats.mostPopularService)
            StatisticRow("Popüler Sayısı", stats.mostPopularServiceCount.toString())
        }
    }
}

/**
 * Reusable statistics card wrapper
 */
@Composable
private fun StatisticsCard(
    title: String,
    icon: ImageVector,
    color: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                )
            }
            
            content()
        }
    }
}

/**
 * Individual statistic row
 */
@Composable
private fun StatisticRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Export section
 */
@Composable
private fun ExportSection(
    isExporting: Boolean,
    onExport: (com.borayildirim.beautydate.data.repository.ExportFormat) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Rapor Dışa Aktarma",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            if (isExporting) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Rapor hazırlanıyor...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(com.borayildirim.beautydate.data.repository.ExportFormat.values()) { format ->
                        OutlinedButton(
                            onClick = { onExport(format) }
                        ) {
                            Text(format.displayName)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Empty state when no statistics available
 */
@Composable
private fun EmptyStatisticsState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "İstatistik Verisi Bulunamadı",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Henüz yeterli veri bulunmuyor. Sistem kullanıldıkça istatistikler burada görünecek.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
} 