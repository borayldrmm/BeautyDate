package com.borayildirim.beautydate.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.borayildirim.beautydate.data.local.entities.PaymentEntity

/**
 * Payment DAO for database operations
 * Follows exact same pattern as CustomerDao
 * Memory efficient: Flow-based reactive queries
 */
@Dao
interface PaymentDao {
    
    /**
     * Gets all payments for a business
     * Memory efficient: Flow for reactive updates
     */
    @Query("SELECT * FROM payments WHERE businessId = :businessId ORDER BY createdAt DESC")
    fun getAllPayments(businessId: String): Flow<List<PaymentEntity>>
    
    /**
     * Gets payments by appointment
     */
    @Query("SELECT * FROM payments WHERE appointmentId = :appointmentId AND businessId = :businessId")
    fun getPaymentsByAppointment(appointmentId: String, businessId: String): Flow<List<PaymentEntity>>
    
    /**
     * Gets payments by customer
     */
    @Query("SELECT * FROM payments WHERE customerId = :customerId AND businessId = :businessId ORDER BY createdAt DESC")
    fun getPaymentsByCustomer(customerId: String, businessId: String): Flow<List<PaymentEntity>>
    
    /**
     * Gets payments by status
     */
    @Query("SELECT * FROM payments WHERE status = :status AND businessId = :businessId ORDER BY createdAt DESC")
    fun getPaymentsByStatus(status: String, businessId: String): Flow<List<PaymentEntity>>
    
    /**
     * Gets payments by method
     */
    @Query("SELECT * FROM payments WHERE paymentMethod = :method AND businessId = :businessId ORDER BY createdAt DESC")
    fun getPaymentsByMethod(method: String, businessId: String): Flow<List<PaymentEntity>>
    
    /**
     * Gets payment by ID
     */
    @Query("SELECT * FROM payments WHERE id = :id AND businessId = :businessId")
    suspend fun getPaymentById(id: String, businessId: String): PaymentEntity?
    
    /**
     * Gets total revenue for business
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM payments WHERE status = 'COMPLETED' AND businessId = :businessId")
    suspend fun getTotalRevenue(businessId: String): Double
    
    /**
     * Gets revenue by payment method
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM payments WHERE status = 'COMPLETED' AND paymentMethod = :method AND businessId = :businessId")
    suspend fun getRevenueByMethod(method: String, businessId: String): Double
    
    /**
     * Gets payment count by status
     */
    @Query("SELECT COUNT(*) FROM payments WHERE status = :status AND businessId = :businessId")
    suspend fun getPaymentCountByStatus(status: String, businessId: String): Int
    
    /**
     * Inserts a payment
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)
    
    /**
     * Inserts multiple payments
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<PaymentEntity>)
    
    /**
     * Updates a payment
     */
    @Update
    suspend fun updatePayment(payment: PaymentEntity)
    
    /**
     * Deletes a payment by ID
     */
    @Query("DELETE FROM payments WHERE id = :id AND businessId = :businessId")
    suspend fun deletePayment(id: String, businessId: String)
    
    /**
     * Searches payments by customer name or service name
     */
    @Query("""
        SELECT * FROM payments 
        WHERE businessId = :businessId 
        AND (customerName LIKE :query OR serviceName LIKE :query OR notes LIKE :query)
        ORDER BY createdAt DESC
    """)
    fun searchPayments(query: String, businessId: String): Flow<List<PaymentEntity>>
} 