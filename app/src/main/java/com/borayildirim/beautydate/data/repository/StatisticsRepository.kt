package com.borayildirim.beautydate.data.repository

import com.borayildirim.beautydate.data.models.BusinessStatistics
import com.borayildirim.beautydate.data.models.StatisticsPeriod
import kotlinx.coroutines.flow.Flow

/**
 * Statistics repository interface
 * Aggregates data from all business repositories for dashboard analytics
 * Memory efficient: Flow-based reactive data with calculated statistics
 */
interface StatisticsRepository {
    
    /**
     * Gets complete business statistics
     * Aggregates data from all repositories
     */
    suspend fun getBusinessStatistics(businessId: String, period: StatisticsPeriod = StatisticsPeriod.ALL_TIME): BusinessStatistics
    
    /**
     * Gets business statistics as Flow for reactive updates
     */
    fun getBusinessStatisticsFlow(businessId: String, period: StatisticsPeriod = StatisticsPeriod.ALL_TIME): Flow<BusinessStatistics>
    
    /**
     * Gets financial statistics only
     */
    suspend fun getFinancialStatistics(businessId: String, period: StatisticsPeriod = StatisticsPeriod.ALL_TIME): com.borayildirim.beautydate.data.models.FinancialStatistics
    
    /**
     * Gets customer statistics only
     */
    suspend fun getCustomerStatistics(businessId: String, period: StatisticsPeriod = StatisticsPeriod.ALL_TIME): com.borayildirim.beautydate.data.models.CustomerStatistics
    
    /**
     * Gets appointment statistics only
     */
    suspend fun getAppointmentStatistics(businessId: String, period: StatisticsPeriod = StatisticsPeriod.ALL_TIME): com.borayildirim.beautydate.data.models.AppointmentStatistics
    
    /**
     * Gets employee statistics only  
     */
    suspend fun getEmployeeStatistics(businessId: String, period: StatisticsPeriod = StatisticsPeriod.ALL_TIME): com.borayildirim.beautydate.data.models.EmployeeStatistics
    
    /**
     * Gets service statistics only
     */
    suspend fun getServiceStatistics(businessId: String, period: StatisticsPeriod = StatisticsPeriod.ALL_TIME): com.borayildirim.beautydate.data.models.ServiceStatistics
    
    /**
     * Refreshes statistics cache
     */
    suspend fun refreshStatistics(businessId: String): Result<Unit>
    
    /**
     * Exports statistics to external format (CSV, PDF, etc.)
     */
    suspend fun exportStatistics(businessId: String, period: StatisticsPeriod, format: ExportFormat): Result<String>
}

/**
 * Export format options
 */
enum class ExportFormat(val displayName: String, val extension: String) {
    CSV("CSV Dosyası", "csv"),
    PDF("PDF Raporu", "pdf"),
    EXCEL("Excel Dosyası", "xlsx")
} 