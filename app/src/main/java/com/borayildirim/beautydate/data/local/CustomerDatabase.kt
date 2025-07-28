package com.borayildirim.beautydate.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.borayildirim.beautydate.data.local.dao.AppointmentDao
import com.borayildirim.beautydate.data.local.dao.WorkingHoursDao
import com.borayildirim.beautydate.data.local.dao.PaymentDao
import com.borayildirim.beautydate.data.local.dao.TransactionDao
import com.borayildirim.beautydate.data.local.dao.ExpenseDao
import com.borayildirim.beautydate.data.local.entities.WorkingHoursEntity
import com.borayildirim.beautydate.data.local.entities.PaymentEntity
import com.borayildirim.beautydate.data.local.entities.TransactionEntity
import com.borayildirim.beautydate.data.local.entities.ExpenseEntity

/**
 * Room database for business management (customers, services, employees, notes, appointments, working hours, payments, transactions, expenses)
 * Handles local storage with offline-first approach
 * Memory efficient: single database instance for all business entities
 */
@Database(
    entities = [
        CustomerEntity::class, 
        ServiceEntity::class, 
        EmployeeEntity::class, 
        CustomerNoteEntity::class,
        AppointmentEntity::class,
        WorkingHoursEntity::class,
        PaymentEntity::class,
        TransactionEntity::class,
        ExpenseEntity::class
    ],
    version = 13, // Version updated to include salary field in Employee entity
    exportSchema = false
)
@TypeConverters(DateTimeConverter::class)
abstract class CustomerDatabase : RoomDatabase() {
    
    /**
     * Customer DAO for database operations
     */
    abstract fun customerDao(): CustomerDao
    
    /**
     * Service DAO for database operations
     */
    abstract fun serviceDao(): ServiceDao
    
    /**
     * Employee DAO for database operations
     */
    abstract fun employeeDao(): EmployeeDao
    
    /**
     * Customer Note DAO for database operations
     */
    abstract fun customerNoteDao(): CustomerNoteDao
    
    /**
     * Appointment DAO for database operations
     */
    abstract fun appointmentDao(): AppointmentDao
    
    /**
     * Working Hours DAO for database operations
     */
    abstract fun workingHoursDao(): WorkingHoursDao
    
    /**
     * Payment DAO for database operations
     */
    abstract fun paymentDao(): PaymentDao
    
    /**
     * Transaction DAO for database operations
     */
    abstract fun transactionDao(): TransactionDao
    
    /**
     * Expense DAO for database operations
     */
    abstract fun expenseDao(): ExpenseDao
    
    companion object {
        @Volatile
        private var INSTANCE: CustomerDatabase? = null
        
        /**
         * Gets or creates database instance (Singleton pattern)
         * @param context Application context
         * @return CustomerDatabase instance
         */
        fun getDatabase(context: Context): CustomerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CustomerDatabase::class.java,
                    "customer_database"
                )
                // Fallback to destructive migration for adding new entities
                .fallbackToDestructiveMigration(dropAllTables = true) // Updated API with explicit parameter
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 