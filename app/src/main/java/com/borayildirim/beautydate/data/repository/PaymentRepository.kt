package com.borayildirim.beautydate.data.repository

import kotlinx.coroutines.flow.Flow
import com.borayildirim.beautydate.data.models.Payment
import com.borayildirim.beautydate.data.models.PaymentMethod
import com.borayildirim.beautydate.data.models.PaymentStatus

/**
 * Payment repository interface
 * Follows exact same pattern as CustomerRepository
 * Handles payment data operations with offline-first approach
 */
interface PaymentRepository {
    
    /**
     * Gets all payments for a business
     * Returns Flow for reactive UI updates
     */
    fun getAllPayments(businessId: String): Flow<List<Payment>>
    
    /**
     * Gets payments by appointment
     */
    fun getPaymentsByAppointment(appointmentId: String, businessId: String): Flow<List<Payment>>
    
    /**
     * Gets payments by customer
     */
    fun getPaymentsByCustomer(customerId: String, businessId: String): Flow<List<Payment>>
    
    /**
     * Gets payments by status
     */
    fun getPaymentsByStatus(status: PaymentStatus, businessId: String): Flow<List<Payment>>
    
    /**
     * Gets payments by method
     */
    fun getPaymentsByMethod(method: PaymentMethod, businessId: String): Flow<List<Payment>>
    
    /**
     * Gets payment by ID
     */
    suspend fun getPaymentById(id: String, businessId: String): Payment?
    
    /**
     * Gets total revenue for business
     */
    suspend fun getTotalRevenue(businessId: String): Double
    
    /**
     * Gets revenue by payment method
     */
    suspend fun getRevenueByMethod(method: PaymentMethod, businessId: String): Double
    
    /**
     * Gets payment count by status
     */
    suspend fun getPaymentCountByStatus(status: PaymentStatus, businessId: String): Int
    
    /**
     * Adds new payment
     */
    suspend fun addPayment(payment: Payment): Result<String>
    
    /**
     * Updates existing payment
     */
    suspend fun updatePayment(payment: Payment): Result<Payment>
    
    /**
     * Deletes payment
     */
    suspend fun deletePayment(id: String, businessId: String): Result<Unit>
    
    /**
     * Syncs payments with Firestore
     */
    suspend fun syncPayments(businessId: String): Result<Unit>
    
    /**
     * Searches payments
     */
    fun searchPayments(query: String, businessId: String): Flow<List<Payment>>
} 