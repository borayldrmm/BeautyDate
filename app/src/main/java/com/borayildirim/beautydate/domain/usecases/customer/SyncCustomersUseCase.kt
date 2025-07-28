package com.borayildirim.beautydate.domain.usecases.customer

import com.borayildirim.beautydate.data.repository.CustomerRepository
import javax.inject.Inject

/**
 * Use case for manually syncing customers with Firestore
 * Handles offline-to-online data synchronization
 */
class SyncCustomersUseCase @Inject constructor(
    private val customerRepository: CustomerRepository
) {
    /**
     * Syncs customers with Firestore for current authenticated business
     * BusinessId is handled automatically by repository layer
     */
    suspend operator fun invoke(): Result<Unit> {
        return customerRepository.syncWithFirestore()
    }
} 