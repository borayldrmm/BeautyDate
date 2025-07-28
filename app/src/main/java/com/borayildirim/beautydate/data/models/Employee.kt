package com.borayildirim.beautydate.data.models

import com.google.firebase.Timestamp
import java.util.UUID

/**
 * Employee domain model for BeautyDate business app
 * Represents staff members with their skills and permissions
 * Memory efficient: immutable data class with efficient collections
 */
data class Employee(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val gender: EmployeeGender = EmployeeGender.OTHER,
    val phoneNumber: String = "",
    val email: String = "",
    val address: String = "",
    val hireDate: String = "", // dd/MM/yyyy format
    val skills: List<String> = emptyList(), // e.g., ["Makyaj", "Lazer Epilasyon"]
    val permissions: List<EmployeePermission> = emptyList(),
    val notes: String = "",
    val salary: Double = 0.0, // Monthly salary in TRY
    val isActive: Boolean = true,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val businessId: String = ""
) {
    companion object {
        /**
         * Generates a unique employee ID for new employees
         * Memory efficient: UUID generation
         */
        fun generateEmployeeId(): String = UUID.randomUUID().toString()
        
        /**
         * Common skills for beauty professionals
         * Memory efficient: pre-computed list for UI suggestions
         */
        val commonSkills = listOf(
            "Makyaj",
            "Kaş Şekillendirme",
            "Kirpik Uzatma",
            "Saç Kesimi",
            "Saç Boyama",
            "Cilt Bakımı",
            "Masaj",
            "Lazer Epilasyon",
            "IPL",
            "Botoks",
            "Dolgu",
            "Peeling",
            "Manikür",
            "Pedikür",
            "Nail Art",
            "Zayıflama Seansı",
            "Detoks",
            "Vücut Bakımı"
        )
    }
    
    /**
     * Returns full name of the employee
     * Memory efficient: string concatenation
     */
    val fullName: String
        get() = "$firstName $lastName".trim()
    
    /**
     * Returns skills as comma-separated string
     */
    val skillsText: String
        get() = skills.joinToString(", ")
    
    /**
     * Returns permissions as comma-separated string
     */
    val permissionsText: String
        get() = permissions.joinToString(", ") { it.getDisplayName() }
    
    /**
     * Returns formatted salary with currency symbol
     * Memory efficient: string formatting
     */
    val formattedSalary: String
        get() = if (salary > 0) "${salary.toInt()} ₺" else "Belirtilmemiş"
    
    /**
     * Validates employee data for required fields
     * Business logic: ensures all required fields are properly filled
     */
    fun isValid(): Boolean {
        return firstName.isNotBlank() &&
                lastName.isNotBlank() &&
                phoneNumber.isNotBlank() &&
                hireDate.isNotBlank() &&
                businessId.isNotBlank()
    }
    
    /**
     * Creates a copy with updated timestamp
     * Memory efficient: minimal object creation
     */
    fun withUpdatedTimestamp(): Employee {
        return copy(updatedAt = Timestamp.now())
    }
    
    /**
     * Checks if employee has a specific permission
     */
    fun hasPermission(permission: EmployeePermission): Boolean {
        return permissions.contains(permission)
    }
    
    /**
     * Checks if employee has a specific skill
     */
    fun hasSkill(skill: String): Boolean {
        return skills.any { it.equals(skill, ignoreCase = true) }
    }
}

/**
 * Employee gender enumeration
 * Used for icon selection and categorization
 * Memory efficient: enum class for type safety
 */
enum class EmployeeGender {
    MALE,
    FEMALE,
    OTHER;
    
    /**
     * Returns Turkish display name for gender
     */
    fun getDisplayName(): String {
        return when (this) {
            MALE -> "Erkek"
            FEMALE -> "Kadın"
            OTHER -> "Belirtilmemiş"
        }
    }
    
    /**
     * Returns emoji icon for gender
     */
    fun getEmoji(): String {
        return when (this) {
            MALE -> "👨"
            FEMALE -> "👩"
            OTHER -> "👤"
        }
    }
}

/**
 * Employee permission enumeration
 * Defines what actions an employee can perform
 * Memory efficient: enum class for permission management
 */
enum class EmployeePermission {
    APPOINTMENT_MANAGEMENT,    // Randevu yönetimi
    CUSTOMER_MANAGEMENT,       // Müşteri yönetimi
    SERVICE_MANAGEMENT,        // Hizmet yönetimi
    PRICE_MANAGEMENT,          // Fiyat yönetimi
    EMPLOYEE_MANAGEMENT,       // Çalışan yönetimi
    FINANCIAL_REPORTS,         // Mali raporlar
    SYSTEM_SETTINGS;           // Sistem ayarları
    
    /**
     * Returns Turkish display name for permission
     */
    fun getDisplayName(): String {
        return when (this) {
            APPOINTMENT_MANAGEMENT -> "Randevu Yönetimi"
            CUSTOMER_MANAGEMENT -> "Müşteri Yönetimi"
            SERVICE_MANAGEMENT -> "Hizmet Yönetimi"
            PRICE_MANAGEMENT -> "Fiyat Yönetimi"
            EMPLOYEE_MANAGEMENT -> "Çalışan Yönetimi"
            FINANCIAL_REPORTS -> "Mali Raporlar"
            SYSTEM_SETTINGS -> "Sistem Ayarları"
        }
    }
    
    /**
     * Returns description of what the permission allows
     */
    fun getDescription(): String {
        return when (this) {
            APPOINTMENT_MANAGEMENT -> "Randevu oluşturma, düzenleme ve iptal etme"
            CUSTOMER_MANAGEMENT -> "Müşteri ekleme, düzenleme ve silme"
            SERVICE_MANAGEMENT -> "Hizmet ekleme, düzenleme ve silme"
            PRICE_MANAGEMENT -> "Hizmet fiyatlarını güncelleme"
            EMPLOYEE_MANAGEMENT -> "Çalışan ekleme, düzenleme ve silme"
            FINANCIAL_REPORTS -> "Gelir raporlarını görüntüleme"
            SYSTEM_SETTINGS -> "Uygulama ayarlarını değiştirme"
        }
    }
    
    /**
     * Returns emoji icon for permission
     */
    fun getEmoji(): String {
        return when (this) {
            APPOINTMENT_MANAGEMENT -> "📅"
            CUSTOMER_MANAGEMENT -> "👥"
            SERVICE_MANAGEMENT -> "🛠️"
            PRICE_MANAGEMENT -> "💰"
            EMPLOYEE_MANAGEMENT -> "👨‍💼"
            FINANCIAL_REPORTS -> "📊"
            SYSTEM_SETTINGS -> "⚙️"
        }
    }
} 