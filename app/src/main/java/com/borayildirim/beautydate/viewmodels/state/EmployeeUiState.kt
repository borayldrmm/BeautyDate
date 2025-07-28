package com.borayildirim.beautydate.viewmodels.state

import com.borayildirim.beautydate.data.models.Employee
import com.borayildirim.beautydate.data.models.EmployeeGender
import com.borayildirim.beautydate.data.models.EmployeePermission

/**
 * UI state for employee management screens
 * Holds all state needed for employee listing and management
 * Memory efficient: immutable data class with computed properties
 */
data class EmployeeUiState(
    val employees: List<Employee> = emptyList(),
    val filteredEmployees: List<Employee> = emptyList(),
    val searchQuery: String = "",
    val selectedGender: EmployeeGender? = null,
    val selectedPermission: EmployeePermission? = null,
    val selectedSkill: String? = null,
    val showActiveOnly: Boolean = true,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val totalEmployees: Int = 0,
    val selectedEmployee: Employee? = null,
    val showAddEmployeeSheet: Boolean = false,
    val isSyncing: Boolean = false,
    // Selection and bulk operations state
    val selectedEmployees: Set<String> = emptySet(),
    val isInSelectionMode: Boolean = false,
    val showPermissionUpdateSheet: Boolean = false,
    val showSkillUpdateSheet: Boolean = false
) {
    /**
     * Returns employees to display based on search query and filters
     * Memory efficient: computed property with lazy evaluation
     */
    val displayEmployees: List<Employee>
        get() {
            val baseList = if (searchQuery.isBlank()) employees else filteredEmployees
            var filtered = baseList
            
            // Apply active filter
            if (showActiveOnly) {
                filtered = filtered.filter { it.isActive }
            }
            
            // Apply gender filter
            if (selectedGender != null) {
                filtered = filtered.filter { it.gender == selectedGender }
            }
            
            // Apply permission filter
            if (selectedPermission != null) {
                filtered = filtered.filter { it.hasPermission(selectedPermission) }
            }
            
            // Apply skill filter
            if (selectedSkill != null) {
                filtered = filtered.filter { it.hasSkill(selectedSkill) }
            }
            
            return filtered
        }
        
    /**
     * Returns true if no employees are available
     */
    val isEmpty: Boolean
        get() = employees.isEmpty() && !isLoading
        
    /**
     * Returns true if search results are empty but there are employees
     */
    val isSearchEmpty: Boolean
        get() = searchQuery.isNotBlank() && filteredEmployees.isEmpty() && employees.isNotEmpty()
        
    /**
     * Returns true if filter results are empty but there are employees
     */
    val isFilterEmpty: Boolean
        get() = (selectedGender != null || selectedPermission != null || selectedSkill != null) && 
                displayEmployees.isEmpty() && employees.isNotEmpty()
        
    /**
     * Returns employees grouped by gender for display
     * Memory efficient: groupBy with minimal object creation
     */
    val employeesByGender: Map<EmployeeGender, List<Employee>>
        get() = displayEmployees.groupBy { it.gender }
        
    /**
     * Returns employees grouped by skills for analysis
     * Memory efficient: flat mapping with grouping
     */
    val employeesBySkill: Map<String, List<Employee>>
        get() = displayEmployees.flatMap { employee ->
            employee.skills.map { skill -> skill to employee }
        }.groupBy({ it.first }, { it.second })
        
    /**
     * Returns total selected employees count for bulk operations
     */
    val selectedCount: Int
        get() = selectedEmployees.size
        
    /**
     * Returns true if all visible employees are selected
     */
    val isAllSelected: Boolean
        get() = displayEmployees.isNotEmpty() && 
                displayEmployees.all { it.id in selectedEmployees }
                
    /**
     * Returns active employees count from current display
     */
    val activeEmployeesCount: Int
        get() = displayEmployees.count { it.isActive }
        
    /**
     * Returns inactive employees count from current display
     */
    val inactiveEmployeesCount: Int
        get() = displayEmployees.count { !it.isActive }
        
    /**
     * Returns employee statistics for current displayed employees
     * Memory efficient: computed on-demand
     */
    val employeeStats: EmployeeStats
        get() {
            val totalDisplayed = displayEmployees.size
            val active = activeEmployeesCount
            val inactive = inactiveEmployeesCount
            
            // Calculate skills statistics
            val allSkills = displayEmployees.flatMap { it.skills }
            val avgSkills = if (totalDisplayed > 0) allSkills.size.toDouble() / totalDisplayed else 0.0
            
            return EmployeeStats(
                totalEmployees = totalDisplayed,
                activeEmployees = active,
                inactiveEmployees = inactive,
                averageSkillsPerEmployee = avgSkills
            )
        }
        
    /**
     * Returns all unique skills from current employees
     * Memory efficient: Set operation for uniqueness
     */
    val allAvailableSkills: List<String>
        get() = employees.flatMap { it.skills }.distinct().sorted()
        
    /**
     * Returns permission distribution for current displayed employees
     * Memory efficient: Map with count aggregation
     */
    val permissionDistribution: Map<EmployeePermission, Int>
        get() = EmployeePermission.values().associateWith { permission ->
            displayEmployees.count { it.hasPermission(permission) }
        }
}

/**
 * Employee statistics data class for UI display
 * Memory efficient: simple data holder
 */
data class EmployeeStats(
    val totalEmployees: Int,
    val activeEmployees: Int,
    val inactiveEmployees: Int,
    val averageSkillsPerEmployee: Double
) {
    /**
     * Returns formatted active percentage
     */
    val activePercentage: Int
        get() = if (totalEmployees > 0) ((activeEmployees * 100) / totalEmployees) else 0
        
    /**
     * Returns formatted average skills
     */
    val formattedAverageSkills: String
        get() = String.format("%.1f", averageSkillsPerEmployee)
} 