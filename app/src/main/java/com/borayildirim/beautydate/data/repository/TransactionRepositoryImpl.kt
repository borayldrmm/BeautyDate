package com.borayildirim.beautydate.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FirebaseFirestore
import com.borayildirim.beautydate.data.local.dao.TransactionDao
import com.borayildirim.beautydate.data.local.entities.TransactionEntity
import com.borayildirim.beautydate.data.models.Transaction
import com.borayildirim.beautydate.data.models.TransactionFirestore
import com.borayildirim.beautydate.data.models.TransactionType
import com.borayildirim.beautydate.data.models.TransactionCategory
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Transaction repository implementation
 * Follows exact same pattern as CustomerRepositoryImpl
 * Offline-first approach with Firebase sync
 */
@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val firestore: FirebaseFirestore
) : TransactionRepository {
    
    private val transactionsCollection = firestore.collection("transactions")
    
    override fun getAllTransactions(businessId: String): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions(businessId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getTransactionsByType(type: TransactionType, businessId: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByType(type.name, businessId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getTransactionsByCategory(category: TransactionCategory, businessId: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByCategory(category.name, businessId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getTransactionsByPayment(paymentId: String, businessId: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByPayment(paymentId, businessId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getTransactionById(id: String, businessId: String): Transaction? {
        return transactionDao.getTransactionById(id, businessId)?.toDomain()
    }
    
    override suspend fun getTotalIncome(businessId: String): Double {
        return transactionDao.getTotalIncome(businessId)
    }
    
    override suspend fun getTotalExpenses(businessId: String): Double {
        return transactionDao.getTotalExpenses(businessId)
    }
    
    override suspend fun getAmountByCategory(category: TransactionCategory, businessId: String): Double {
        return transactionDao.getAmountByCategory(category.name, businessId)
    }
    
    override suspend fun getTransactionCountByType(type: TransactionType, businessId: String): Int {
        return transactionDao.getTransactionCountByType(type.name, businessId)
    }
    
    override suspend fun addTransaction(transaction: Transaction): Result<String> {
        return try {
            // Save to local database first (offline-first)
            val entity = TransactionEntity.fromDomain(transaction)
            transactionDao.insertTransaction(entity)
            
            // Sync to Firestore
            val firestoreModel = TransactionFirestore.fromDomainModel(transaction)
            transactionsCollection.document(transaction.id).set(firestoreModel).await()
            
            Result.success(transaction.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateTransaction(transaction: Transaction): Result<Transaction> {
        return try {
            // Update local database first (offline-first)
            val updatedTransaction = transaction.withUpdatedTimestamp()
            val entity = TransactionEntity.fromDomain(updatedTransaction)
            transactionDao.updateTransaction(entity)
            
            // Sync to Firestore
            val firestoreModel = TransactionFirestore.fromDomainModel(updatedTransaction)
            transactionsCollection.document(updatedTransaction.id).set(firestoreModel).await()
            
            Result.success(updatedTransaction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteTransaction(id: String, businessId: String): Result<Unit> {
        return try {
            // Delete from local database first (offline-first)
            transactionDao.deleteTransaction(id, businessId)
            
            // Delete from Firestore
            transactionsCollection.document(id).delete().await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun syncTransactions(businessId: String): Result<Unit> {
        return try {
            // Get transactions from Firestore
            val snapshot = transactionsCollection
                .whereEqualTo("businessId", businessId)
                .get()
                .await()
            
            val firestoreTransactions = snapshot.documents.mapNotNull { doc ->
                doc.toObject(TransactionFirestore::class.java)?.toDomainModel()
            }
            
            // Update local database
            val entities = firestoreTransactions.map { TransactionEntity.fromDomain(it) }
            transactionDao.insertTransactions(entities)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun searchTransactions(query: String, businessId: String): Flow<List<Transaction>> {
        val searchQuery = "%$query%"
        return transactionDao.searchTransactions(searchQuery, businessId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
} 