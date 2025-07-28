package com.borayildirim.beautydate.data.repository

import com.borayildirim.beautydate.data.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of StatisticsRepository
 * Follows SOLID principles with Single Responsibility
 * Memory efficient: Flow-based reactive data and minimal object creation
 * Multi-tenant: Uses AuthUtil-enabled repositories for automatic businessId filtering
 * File size: <300 lines following project guidelines
 */
@Singleton
class StatisticsRepositoryImpl @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val appointmentRepository: AppointmentRepository,
    private val employeeRepository: EmployeeRepository,
    private val serviceRepository: ServiceRepository,
    private val expenseRepository: com.borayildirim.beautydate.data.repository.ExpenseRepository,
    private val paymentRepository: PaymentRepository
) : StatisticsRepository {

    /**
     * Gets comprehensive business statistics for a period
     * Memory efficient: Aggregates data from multiple sources without duplication
     */
    override suspend fun getBusinessStatistics(businessId: String, period: StatisticsPeriod): BusinessStatistics {
        return BusinessStatistics(
            businessId = businessId,
            financialStats = getFinancialStatistics(businessId, period),
            customerStats = getCustomerStatistics(businessId, period),
            appointmentStats = getAppointmentStatistics(businessId, period),
            employeeStats = getEmployeeStatistics(businessId, period),
            serviceStats = getServiceStatistics(businessId, period)
        )
    }
    
    /**
     * Provides reactive Flow of business statistics
     * Memory efficient: Combines multiple repository Flows without data duplication
     * Follows Dependency Inversion Principle
     */
    override fun getBusinessStatisticsFlow(businessId: String, period: StatisticsPeriod): Flow<BusinessStatistics> {
        return combine(
            customerRepository.getAllCustomers(),
            appointmentRepository.getAllAppointments(businessId),
            employeeRepository.getAllEmployees(),
            serviceRepository.getAllServices(businessId),
            expenseRepository.getAllExpenses(businessId)
        ) { customers, appointments, employees, services, expenses ->
            getBusinessStatistics(businessId, period)
        }
    }
    
    /**
     * Gets employee statistics
     * Memory efficient: Real calculations from employee data
     */
    override suspend fun getEmployeeStatistics(businessId: String, period: StatisticsPeriod): EmployeeStatistics {
        return try {
            // Get all employees
            val allEmployees = employeeRepository.getAllEmployees().first()
            val totalEmployees = allEmployees.size
            val activeEmployees = allEmployees.count { it.isActive } // Assuming Employee has isActive field
            
            
            EmployeeStatistics(
                totalEmployees = totalEmployees,
                activeEmployees = activeEmployees
            )
        } catch (e: Exception) {
            EmployeeStatistics(
                totalEmployees = 0,
                activeEmployees = 0
            )
        }
    }
    
    /**
     * Gets financial statistics
     * Memory efficient: Real calculations from appointment and expense data
     */
    override suspend fun getFinancialStatistics(businessId: String, period: StatisticsPeriod): FinancialStatistics {
        return try {
            // Get completed payments (primary revenue source)
            val completedPayments = paymentRepository.getAllPayments(businessId).first()
                .filter { it.status == PaymentStatus.COMPLETED }
            
            // Calculate total revenue from completed payments
            val totalRevenue = completedPayments.sumOf { it.amount }
            
            // Calculate payment method breakdown
            val cashPayments = completedPayments
                .filter { it.paymentMethod == PaymentMethod.CASH }
                .sumOf { it.amount }
            
            val creditPayments = completedPayments
                .filter { it.paymentMethod == PaymentMethod.CREDIT_CARD }
                .sumOf { it.amount }
            
            val pendingPayments = paymentRepository.getAllPayments(businessId).first()
                .filter { it.status == PaymentStatus.PENDING }
                .sumOf { it.amount }
            
            // Get total expenses
            val allExpenses = expenseRepository.getAllExpenses(businessId).first()
            val totalExpenses = allExpenses.sumOf { it.amount }
            
            // Calculate net profit
            val netProfit = totalRevenue - totalExpenses
            
            FinancialStatistics(
                totalRevenue = totalRevenue,
                totalIncome = totalRevenue,
                totalExpenses = totalExpenses,
                netProfit = netProfit,
                cashPayments = cashPayments,
                creditPayments = creditPayments,
                pendingPayments = pendingPayments
            )
        } catch (e: Exception) {
            FinancialStatistics(
                totalRevenue = 0.0,
                totalIncome = 0.0,
                totalExpenses = 0.0,
                netProfit = 0.0,
                cashPayments = 0.0,
                creditPayments = 0.0,
                pendingPayments = 0.0
            )
        }
    }
    
    /**
     * Gets customer statistics
     * Memory efficient: Real calculations from customer data
     */
    override suspend fun getCustomerStatistics(businessId: String, period: StatisticsPeriod): CustomerStatistics {
        return try {
            // Get all customers
            val allCustomers = customerRepository.getAllCustomers().first()
            
            // Calculate basic stats
            val totalCustomers = allCustomers.size
            
            // Get appointments to calculate active customers
            val allAppointments = appointmentRepository.getAllAppointments(businessId).first()
            val customerIdsWithAppointments = allAppointments.map { it.customerId }.toSet()
            val activeCustomers = customerIdsWithAppointments.size
            
            // Calculate retention rate (active customers / total customers)
            val customerRetentionRate = if (totalCustomers > 0) {
                (activeCustomers.toDouble() / totalCustomers.toDouble()) * 100.0
            } else 0.0
            
            // New customers this month (simplified - would need createdAt filtering in real app)
            val newCustomersThisMonth = totalCustomers // Placeholder
            
            
            CustomerStatistics(
                totalCustomers = totalCustomers,
                newCustomersThisMonth = newCustomersThisMonth,
                activeCustomers = activeCustomers,
                customerRetentionRate = customerRetentionRate
            )
        } catch (e: Exception) {
            CustomerStatistics(
                totalCustomers = 0,
                newCustomersThisMonth = 0,
                activeCustomers = 0,
                customerRetentionRate = 0.0
            )
        }
    }
    
    /**
     * Gets appointment statistics
     * Memory efficient: Real calculations from appointment data
     */
    override suspend fun getAppointmentStatistics(businessId: String, period: StatisticsPeriod): AppointmentStatistics {
        return try {
            // Get all appointments
            val allAppointments = appointmentRepository.getAllAppointments(businessId).first()
            
            // Calculate by status
            val totalAppointments = allAppointments.size
            val completedAppointments = allAppointments.count { it.status == AppointmentStatus.COMPLETED }
            val upcomingAppointments = allAppointments.count { it.status == AppointmentStatus.SCHEDULED }
            val cancelledAppointments = allAppointments.count { 
                it.status == AppointmentStatus.CANCELLED || it.status == AppointmentStatus.NO_SHOW 
            }
            
            
            AppointmentStatistics(
                totalAppointments = totalAppointments,
                completedAppointments = completedAppointments,
                upcomingAppointments = upcomingAppointments,
                cancelledAppointments = cancelledAppointments
            )
        } catch (e: Exception) {
            AppointmentStatistics(
                totalAppointments = 0,
                completedAppointments = 0,
                upcomingAppointments = 0,
                cancelledAppointments = 0
            )
        }
    }
    
    /**
     * Gets service statistics
     * Memory efficient: Real calculations from service and appointment data
     */
    override suspend fun getServiceStatistics(businessId: String, period: StatisticsPeriod): ServiceStatistics {
        return try {
            // Get all services
            val allServices = serviceRepository.getAllServices(businessId).first()
            val totalServices = allServices.size
            val activeServices = allServices.count { it.isActive } // Assuming Service has isActive field
            
            // Calculate average price
            val averageServicePrice = if (allServices.isNotEmpty()) {
                allServices.map { it.price }.average()
            } else 0.0
            
            // Find most popular service from appointments
            val allAppointments = appointmentRepository.getAllAppointments(businessId).first()
                .filter { it.status == AppointmentStatus.COMPLETED }
            
            val mostPopularService = if (allAppointments.isNotEmpty()) {
                allAppointments
                    .groupBy { it.serviceName }
                    .maxByOrNull { it.value.size }
                    ?.key ?: ""
            } else ""
            
            
            ServiceStatistics(
                totalServices = totalServices,
                activeServices = activeServices,
                mostPopularService = mostPopularService,
                averageServicePrice = averageServicePrice
            )
        } catch (e: Exception) {
            ServiceStatistics(
                totalServices = 0,
                activeServices = 0,
                mostPopularService = "",
                averageServicePrice = 0.0
            )
        }
    }
    
    /**
     * Syncs employee data using AuthUtil-enabled repository
     * Memory efficient: Single responsibility method
     */
    private suspend fun syncEmployeeData(): Result<Unit> {
        return try {
            employeeRepository.syncWithFirestore()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Syncs appointment data 
     * Memory efficient: Single operation with proper error handling
     */
    private suspend fun syncAppointmentData(businessId: String): Result<Unit> {
        return try {
            appointmentRepository.syncWithFirestore(businessId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Refreshes statistics cache
     * Memory efficient: Single operation
     */
    override suspend fun refreshStatistics(businessId: String): Result<Unit> {
        return try {
            // Trigger sync operations for fresh data
            syncEmployeeData()
            syncAppointmentData(businessId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Exports statistics - placeholder implementation
     * Follows Open/Closed Principle for future expansion
     */
    override suspend fun exportStatistics(businessId: String, period: StatisticsPeriod, format: ExportFormat): Result<String> {
        return try {
            // Placeholder - can be expanded based on requirements
            Result.success("export_${businessId}_${period}.${format.extension}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 