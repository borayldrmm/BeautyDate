package com.borayildirim.beautydate.di

import com.borayildirim.beautydate.data.repository.FeedbackRepository
import com.borayildirim.beautydate.data.repository.FeedbackRepositoryImpl
import com.borayildirim.beautydate.data.repository.ThemeRepository
import com.borayildirim.beautydate.data.repository.ThemeRepositoryImpl
import com.borayildirim.beautydate.data.repository.CustomerRepository
import com.borayildirim.beautydate.data.repository.CustomerRepositoryImpl
import com.borayildirim.beautydate.data.repository.ServiceRepository
import com.borayildirim.beautydate.data.repository.ServiceRepositoryImpl
import com.borayildirim.beautydate.data.repository.EmployeeRepository
import com.borayildirim.beautydate.data.repository.EmployeeRepositoryImpl
import com.borayildirim.beautydate.data.repository.CustomerNoteRepository
import com.borayildirim.beautydate.data.repository.CustomerNoteRepositoryImpl
import com.borayildirim.beautydate.data.repository.AppointmentRepository
import com.borayildirim.beautydate.data.repository.AppointmentRepositoryImpl
import com.borayildirim.beautydate.data.repository.WorkingHoursRepository
import com.borayildirim.beautydate.data.repository.WorkingHoursRepositoryImpl
import com.borayildirim.beautydate.data.repository.PaymentRepository
import com.borayildirim.beautydate.data.repository.PaymentRepositoryImpl
import com.borayildirim.beautydate.data.repository.TransactionRepository
import com.borayildirim.beautydate.data.repository.TransactionRepositoryImpl
import com.borayildirim.beautydate.data.repository.ExpenseRepository
import com.borayildirim.beautydate.data.repository.ExpenseRepositoryImpl
import com.borayildirim.beautydate.data.repository.StatisticsRepository
import com.borayildirim.beautydate.data.repository.StatisticsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for repository bindings
 * Separates repository interfaces from their implementations
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Binds FeedbackRepository implementation
     */
    @Binds
    @Singleton
    abstract fun bindFeedbackRepository(
        feedbackRepositoryImpl: FeedbackRepositoryImpl
    ): FeedbackRepository

    /**
     * Binds ThemeRepository implementation
     */
    @Binds
    @Singleton
    abstract fun bindThemeRepository(
        themeRepositoryImpl: ThemeRepositoryImpl
    ): ThemeRepository

    /**
     * Binds CustomerRepository implementation
     */
    @Binds
    @Singleton
    abstract fun bindCustomerRepository(
        customerRepositoryImpl: CustomerRepositoryImpl
    ): CustomerRepository

    /**
     * Binds ServiceRepository implementation
     */
    @Binds
    @Singleton
    abstract fun bindServiceRepository(
        serviceRepositoryImpl: ServiceRepositoryImpl
    ): ServiceRepository

    /**
     * Binds EmployeeRepository implementation
     */
    @Binds
    @Singleton
    abstract fun bindEmployeeRepository(
        employeeRepositoryImpl: EmployeeRepositoryImpl
    ): EmployeeRepository

    /**
     * Binds CustomerNoteRepository implementation
     */
    @Binds
    @Singleton
    abstract fun bindCustomerNoteRepository(
        customerNoteRepositoryImpl: CustomerNoteRepositoryImpl
    ): CustomerNoteRepository

    /**
     * Binds AppointmentRepository implementation
     */
    @Binds
    @Singleton
    abstract fun bindAppointmentRepository(
        appointmentRepositoryImpl: AppointmentRepositoryImpl
    ): AppointmentRepository

    /**
     * Binds WorkingHoursRepository implementation
     */
    @Binds
    @Singleton
    abstract fun bindWorkingHoursRepository(
        workingHoursRepositoryImpl: WorkingHoursRepositoryImpl
    ): WorkingHoursRepository

    /**
     * Binds PaymentRepository implementation
     */
    @Binds
    @Singleton
    abstract fun bindPaymentRepository(
        paymentRepositoryImpl: PaymentRepositoryImpl
    ): PaymentRepository

    /**
     * Binds TransactionRepository implementation
     */
    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        transactionRepositoryImpl: TransactionRepositoryImpl
    ): TransactionRepository

    /**
     * Binds StatisticsRepository implementation
     */
    @Binds
    @Singleton
    abstract fun bindStatisticsRepository(
        statisticsRepositoryImpl: StatisticsRepositoryImpl
    ): StatisticsRepository

    /**
     * Binds TutorialRepository implementation
     */
    @Binds
    @Singleton
    abstract fun bindTutorialRepository(
        tutorialRepositoryImpl: com.borayildirim.beautydate.data.repository.TutorialRepositoryImpl
    ): com.borayildirim.beautydate.data.repository.TutorialRepository

    /**
     * Binds ExpenseRepository implementation
     */
    @Binds
    @Singleton
    abstract fun bindExpenseRepository(
        expenseRepositoryImpl: ExpenseRepositoryImpl
    ): ExpenseRepository
} 