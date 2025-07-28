package com.borayildirim.beautydate.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FirebaseFirestore
import com.borayildirim.beautydate.data.local.dao.PaymentDao
import com.borayildirim.beautydate.data.local.entities.PaymentEntity
import com.borayildirim.beautydate.data.models.Payment
import com.borayildirim.beautydate.data.models.PaymentFirestore
import com.borayildirim.beautydate.data.models.PaymentMethod
import com.borayildirim.beautydate.data.models.PaymentStatus
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Payment repository implementation
 * Follows exact same pattern as CustomerRepositoryImpl
 * Offline-first approach with Firebase sync
 */
@Singleton
class PaymentRepositoryImpl @Inject constructor(
    private val paymentDao: PaymentDao,
    private val firestore: FirebaseFirestore
) : PaymentRepository {
    
    private val paymentsCollection = firestore.collection("payments")
    
    override fun getAllPayments(businessId: String): Flow<List<Payment>> {
        return paymentDao.getAllPayments(businessId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getPaymentsByAppointment(appointmentId: String, businessId: String): Flow<List<Payment>> {
        return paymentDao.getPaymentsByAppointment(appointmentId, businessId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getPaymentsByCustomer(customerId: String, businessId: String): Flow<List<Payment>> {
        return paymentDao.getPaymentsByCustomer(customerId, businessId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getPaymentsByStatus(status: PaymentStatus, businessId: String): Flow<List<Payment>> {
        return paymentDao.getPaymentsByStatus(status.name, businessId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getPaymentsByMethod(method: PaymentMethod, businessId: String): Flow<List<Payment>> {
        return paymentDao.getPaymentsByMethod(method.name, businessId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getPaymentById(id: String, businessId: String): Payment? {
        return paymentDao.getPaymentById(id, businessId)?.toDomain()
    }
    
    override suspend fun getTotalRevenue(businessId: String): Double {
        return paymentDao.getTotalRevenue(businessId)
    }
    
    override suspend fun getRevenueByMethod(method: PaymentMethod, businessId: String): Double {
        return paymentDao.getRevenueByMethod(method.name, businessId)
    }
    
    override suspend fun getPaymentCountByStatus(status: PaymentStatus, businessId: String): Int {
        return paymentDao.getPaymentCountByStatus(status.name, businessId)
    }
    
    override suspend fun addPayment(payment: Payment): Result<String> {
        return try {
            // Save to local database first (offline-first)
            val entity = PaymentEntity.fromDomain(payment)
            paymentDao.insertPayment(entity)
            
            // Sync to Firestore
            val firestoreModel = PaymentFirestore.fromDomainModel(payment)
            paymentsCollection.document(payment.id).set(firestoreModel).await()
            
            Result.success(payment.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updatePayment(payment: Payment): Result<Payment> {
        return try {
            // Update local database first (offline-first)
            val updatedPayment = payment.withUpdatedTimestamp()
            val entity = PaymentEntity.fromDomain(updatedPayment)
            paymentDao.updatePayment(entity)
            
            // Sync to Firestore
            val firestoreModel = PaymentFirestore.fromDomainModel(updatedPayment)
            paymentsCollection.document(updatedPayment.id).set(firestoreModel).await()
            
            Result.success(updatedPayment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deletePayment(id: String, businessId: String): Result<Unit> {
        return try {
            // Delete from local database first (offline-first)
            paymentDao.deletePayment(id, businessId)
            
            // Delete from Firestore
            paymentsCollection.document(id).delete().await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun syncPayments(businessId: String): Result<Unit> {
        return try {
            // Get payments from Firestore
            val snapshot = paymentsCollection
                .whereEqualTo("businessId", businessId)
                .get()
                .await()
            
            val firestorePayments = snapshot.documents.mapNotNull { doc ->
                doc.toObject(PaymentFirestore::class.java)?.toDomainModel()
            }
            
            // Update local database
            val entities = firestorePayments.map { PaymentEntity.fromDomain(it) }
            paymentDao.insertPayments(entities)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun searchPayments(query: String, businessId: String): Flow<List<Payment>> {
        val searchQuery = "%$query%"
        return paymentDao.searchPayments(searchQuery, businessId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
} 