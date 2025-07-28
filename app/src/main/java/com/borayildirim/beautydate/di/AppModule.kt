package com.borayildirim.beautydate.di

import android.content.Context
import com.borayildirim.beautydate.data.local.UserPreferences
import com.borayildirim.beautydate.data.local.ThemePreferences
import com.borayildirim.beautydate.data.local.CustomerDatabase
import com.borayildirim.beautydate.data.local.CustomerDao
import com.borayildirim.beautydate.data.local.ServiceDao
import com.borayildirim.beautydate.data.local.EmployeeDao
import com.borayildirim.beautydate.data.local.CustomerNoteDao
import com.borayildirim.beautydate.data.local.dao.PaymentDao
import com.borayildirim.beautydate.data.local.dao.TransactionDao
import com.borayildirim.beautydate.data.local.dao.ExpenseDao
import com.borayildirim.beautydate.utils.NetworkMonitor
import com.borayildirim.beautydate.utils.AuthUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing dependencies
 * Only contains @Provides methods for object creation
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    /**
     * Provides Firebase Auth instance
     */
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
    
    /**
     * Provides Firebase Firestore instance
     */
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
    
    /**
     * Provides NetworkMonitor instance
     */
    @Provides
    @Singleton
    fun provideNetworkMonitor(
        @ApplicationContext context: Context
    ): NetworkMonitor {
        return NetworkMonitor(context)
    }
    
    /**
     * Provides UserPreferences instance
     */
    @Provides
    @Singleton
    fun provideUserPreferences(
        @ApplicationContext context: Context
    ): UserPreferences {
        return UserPreferences(context)
    }
    
    /**
     * Provides ThemePreferences instance
     */
    @Provides
    @Singleton
    fun provideThemePreferences(
        @ApplicationContext context: Context
    ): ThemePreferences {
        return ThemePreferences(context)
    }
    
    /**
     * Provides Room database instance
     */
    @Provides
    @Singleton
    fun provideCustomerDatabase(
        @ApplicationContext context: Context
    ): CustomerDatabase {
        return CustomerDatabase.getDatabase(context)
    }
    
    /**
     * Provides CustomerDao from Room database
     */
    @Provides
    fun provideCustomerDao(database: CustomerDatabase): CustomerDao {
        return database.customerDao()
    }
    
    /**
     * Provides ServiceDao from Room database
     */
    @Provides
    fun provideServiceDao(database: CustomerDatabase): ServiceDao {
        return database.serviceDao()
    }
    
    /**
     * Provides EmployeeDao from Room database
     */
    @Provides
    fun provideEmployeeDao(database: CustomerDatabase): EmployeeDao {
        return database.employeeDao()
    }
    
    /**
     * Provides CustomerNoteDao from Room database
     */
    @Provides
    fun provideCustomerNoteDao(database: CustomerDatabase): CustomerNoteDao {
        return database.customerNoteDao()
    }
    
    /**
     * Provides AppointmentDao from Room database
     */
    @Provides
    fun provideAppointmentDao(database: CustomerDatabase): com.borayildirim.beautydate.data.local.dao.AppointmentDao {
        return database.appointmentDao()
    }
    
    /**
     * Provides WorkingHoursDao from Room database
     */
    @Provides
    fun provideWorkingHoursDao(database: CustomerDatabase): com.borayildirim.beautydate.data.local.dao.WorkingHoursDao {
        return database.workingHoursDao()
    }
    
    /**
     * Provides PaymentDao from Room database
     */
    @Provides
    fun providePaymentDao(database: CustomerDatabase): PaymentDao {
        return database.paymentDao()
    }
    
    /**
     * Provides TransactionDao from Room database
     */
    @Provides
    fun provideTransactionDao(database: CustomerDatabase): TransactionDao {
        return database.transactionDao()
    }
    
    /**
     * Provides ExpenseDao from Room database
     */
    @Provides
    fun provideExpenseDao(database: CustomerDatabase): ExpenseDao {
        return database.expenseDao()
    }
    
    /**
     * Provides TutorialPreferences using Application Context
     * Follows Dependency Inversion Principle with Hilt injection
     */
    @Provides
    @Singleton
    fun provideTutorialPreferences(@ApplicationContext context: Context): com.borayildirim.beautydate.data.local.TutorialPreferences {
        return com.borayildirim.beautydate.data.local.TutorialPreferences(context)
    }
    
    /**
     * Provides AuthUtil for multi-tenant business ID management
     * Central authentication utility for secure business context
     */
    @Provides
    @Singleton
    fun provideAuthUtil(
        firebaseAuth: FirebaseAuth
    ): AuthUtil {
        return AuthUtil(firebaseAuth)
    }
} 