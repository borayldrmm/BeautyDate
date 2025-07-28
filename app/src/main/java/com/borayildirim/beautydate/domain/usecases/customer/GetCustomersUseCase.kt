package com.borayildirim.beautydate.domain.usecases.customer

import com.borayildirim.beautydate.data.models.Customer
import com.borayildirim.beautydate.data.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving customers with search functionality
 * Follows Single Responsibility Principle
 * Multi-tenant architecture: BusinessId handled automatically by repository layer
 */
class GetCustomersUseCase @Inject constructor(
    private val customerRepository: CustomerRepository
) {
    /**
     * Gets all customers for current authenticated business
     * BusinessId is handled automatically by repository layer
     * @return Flow of customer list
     */
    operator fun invoke(): Flow<List<Customer>> {
        return customerRepository.getAllCustomers()
    }
    
    /**
     * Searches customers by query (name or phone) for current business
     * BusinessId filtering is handled automatically by repository layer
     * @param query Search query
     * @return Flow of filtered customer list
     */
    fun search(query: String): Flow<List<Customer>> {
        return customerRepository.searchCustomers(query)
    }
} 