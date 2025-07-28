package com.borayildirim.beautydate.domain.usecases.customer

import com.borayildirim.beautydate.data.models.Customer
import com.borayildirim.beautydate.data.repository.CustomerRepository
import javax.inject.Inject

/**
 * Use case for adding new customers with validation
 * Follows Single Responsibility Principle and validates business rules
 */
class AddCustomerUseCase @Inject constructor(
    private val customerRepository: CustomerRepository
) {
    /**
     * Adds a new customer after validation
     * @param customer Customer to add
     * @return Result with customer or error
     */
    suspend operator fun invoke(customer: Customer): Result<Customer> {
        
        // Validate customer data
        if (!customer.isValid()) {
            return Result.failure(IllegalArgumentException("Müşteri bilgileri eksik veya hatalı"))
        }
        
        
        // Check if phone number already exists
        val phoneExists = customerRepository.phoneNumberExists(
            phoneNumber = customer.phoneNumber,
            excludeCustomerId = "" // Empty since this is a new customer
        )
        
        if (phoneExists) {
            return Result.failure(IllegalArgumentException("Bu telefon numarası zaten kayıtlı"))
        }
        
        
        // Add customer
        return customerRepository.addCustomer(customer)
    }
} 