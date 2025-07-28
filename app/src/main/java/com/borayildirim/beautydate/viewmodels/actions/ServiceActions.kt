package com.borayildirim.beautydate.viewmodels.actions

import com.borayildirim.beautydate.data.models.Service
import com.borayildirim.beautydate.data.models.ServiceCategory
import com.borayildirim.beautydate.data.repository.PriceUpdateType

/**
 * Actions for service management operations
 * Separates business logic from ViewModel for better maintainability
 * Memory efficient: action-based state management
 */
sealed class ServiceAction {
    data class AddService(val service: Service) : ServiceAction()
    data class UpdateService(val service: Service) : ServiceAction()
    data class DeleteService(val serviceId: String) : ServiceAction()
    data class SearchServices(val query: String) : ServiceAction()
    data class FilterByCategory(val category: ServiceCategory?) : ServiceAction()
    data class BulkUpdatePrices(
        val updateType: PriceUpdateType,
        val value: Double,
        val category: ServiceCategory? = null,
        val serviceIds: List<String> = emptyList()
    ) : ServiceAction()
    data class ToggleServiceStatus(val serviceId: String) : ServiceAction()
    data class SyncServices(val businessId: String) : ServiceAction()
    data class InitializeServices(val businessId: String) : ServiceAction()
    data class ClearError(val message: String? = null) : ServiceAction()
    data class ClearSuccess(val message: String? = null) : ServiceAction()
    data class ShowAddServiceSheet(val show: Boolean) : ServiceAction()
    data class ShowBulkUpdateSheet(val show: Boolean) : ServiceAction()
    data class SetSelectedService(val service: Service?) : ServiceAction()
    data class SetSelectedServices(val serviceIds: Set<String>) : ServiceAction()
    data class SetSelectionMode(val isSelectionMode: Boolean) : ServiceAction()
} 