package com.borayildirim.beautydate.data.models

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

/**
 * Tutorial step data model
 * Represents a single step in a tutorial or guided tour
 * Memory efficient: immutable data class with validation
 */
data class TutorialStep(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String = "",
    val category: TutorialCategory,
    val order: Int = 0,
    val isCompleted: Boolean = false,
    val targetScreen: String = "",
    val targetElement: String = "",
    val actionType: TutorialActionType = TutorialActionType.INFO,
    val duration: Int = 3000 // milliseconds
) {
    
    /**
     * Returns appropriate icon for the step
     */
    fun getIcon(): ImageVector {
        return when (iconName.lowercase()) {
            "people", "customers" -> Icons.Default.People
            "calendar", "appointments" -> Icons.Default.Event
            "work", "services" -> Icons.Default.Build
            "badge", "employees" -> Icons.Default.Badge
            "analytics", "statistics" -> Icons.Default.BarChart
            "payment", "finance" -> Icons.Default.AttachMoney
            "settings" -> Icons.Default.Settings
            "home" -> Icons.Default.Home
            "menu" -> Icons.Default.Menu
            "add" -> Icons.Default.Add
            "edit" -> Icons.Default.Edit
            "delete" -> Icons.Default.Delete
            "search" -> Icons.Default.Search
            "filter" -> Icons.Default.FilterList
            "sync" -> Icons.Default.Sync
            "notifications" -> Icons.Default.Notifications
            "help" -> Icons.Default.Help
            "info" -> Icons.Default.Info
            "check" -> Icons.Default.Check
            "star" -> Icons.Default.Star
            else -> Icons.Default.Info
        }
    }
    
    /**
     * Validates tutorial step data
     */
    fun validate(): ValidationResult {
        if (title.isBlank()) {
            return ValidationResult.invalid("Başlık boş olamaz")
        }
        if (description.isBlank()) {
            return ValidationResult.invalid("Açıklama boş olamaz")
        }
        if (order < 0) {
            return ValidationResult.invalid("Sıra numarası negatif olamaz")
        }
        return ValidationResult.valid()
    }
    
    /**
     * Marks step as completed
     */
    fun markCompleted(): TutorialStep {
        return copy(isCompleted = true)
    }
    
    /**
     * Resets step completion
     */
    fun resetCompletion(): TutorialStep {
        return copy(isCompleted = false)
    }
}

/**
 * Tutorial category enumeration
 * Groups tutorial steps by functionality area
 */
enum class TutorialCategory(val displayName: String, val description: String) {
    GETTING_STARTED("Başlangıç", "Uygulamaya giriş ve temel özellikler"),
    CUSTOMER_MANAGEMENT("Müşteri Yönetimi", "Müşteri ekleme, düzenleme ve arama"),
    APPOINTMENT_SYSTEM("Randevu Sistemi", "Randevu oluşturma ve takvim kullanımı"),
    SERVICE_MANAGEMENT("Hizmet Yönetimi", "Hizmet tanımlama ve fiyatlandırma"),
    EMPLOYEE_MANAGEMENT("Personel Yönetimi", "Çalışan ekleme ve yetki yönetimi"),
    FINANCIAL_TRACKING("Finansal Takip", "Gelir-gider takibi ve raporlar"),
    STATISTICS_ANALYTICS("İstatistik ve Analiz", "Business analitik ve raporlama"),
    SETTINGS_PREFERENCES("Ayarlar", "Uygulama ayarları ve kişiselleştirme"),
    ADVANCED_FEATURES("Gelişmiş Özellikler", "İleri düzey fonksiyonlar")
}

/**
 * Tutorial action type enumeration
 * Defines what type of interaction the tutorial step requires
 */
enum class TutorialActionType(val displayName: String) {
    INFO("Bilgilendirme"),           // Just show information
    TAP("Dokunma"),                  // Tap on element
    SWIPE("Kaydırma"),              // Swipe gesture
    LONG_PRESS("Uzun Basma"),        // Long press gesture
    SCROLL("Kaydırma"),              // Scroll action
    TYPE("Yazma"),                   // Type text
    NAVIGATE("Yönlendirme"),         // Navigate to screen
    WAIT("Bekleme")                  // Wait for user action
}

/**
 * Complete tutorial data
 * Contains all tutorial steps organized by category
 */
data class TutorialData(
    val id: String,
    val title: String,
    val description: String,
    val category: TutorialCategory,
    val steps: List<TutorialStep> = emptyList(),
    val isCompleted: Boolean = false,
    val estimatedDuration: Int = 0, // minutes
    val difficulty: TutorialDifficulty = TutorialDifficulty.BEGINNER,
    val prerequisites: List<String> = emptyList() // Required tutorial IDs
) {
    
    /**
     * Returns completion percentage
     */
    val completionPercentage: Double
        get() = if (steps.isEmpty()) 0.0 else (steps.count { it.isCompleted }.toDouble() / steps.size) * 100
    
    /**
     * Returns next uncompleted step
     */
    val nextStep: TutorialStep?
        get() = steps.firstOrNull { !it.isCompleted }
    
    /**
     * Returns completed steps count
     */
    val completedStepsCount: Int
        get() = steps.count { it.isCompleted }
    
    /**
     * Returns total steps count
     */
    val totalStepsCount: Int
        get() = steps.size
    
    /**
     * Checks if tutorial is fully completed
     */
    val isFullyCompleted: Boolean
        get() = steps.isNotEmpty() && steps.all { it.isCompleted }
    
    /**
     * Returns formatted completion text
     */
    val completionText: String
        get() = "$completedStepsCount / $totalStepsCount adım tamamlandı"
    
    /**
     * Returns formatted duration text
     */
    val durationText: String
        get() = if (estimatedDuration > 0) "${estimatedDuration} dakika" else "Kısa"
    
    /**
     * Validates tutorial data
     */
    fun validate(): ValidationResult {
        if (title.isBlank()) {
            return ValidationResult.invalid("Tutorial başlığı boş olamaz")
        }
        if (steps.isEmpty()) {
            return ValidationResult.invalid("Tutorial en az bir adım içermeli")
        }
        
        // Validate all steps
        steps.forEach { step ->
            val stepValidation = step.validate()
            if (!stepValidation.isValid) {
                return ValidationResult.invalid("Adım hatası: ${stepValidation.errorMessage}")
            }
        }
        
        return ValidationResult.valid()
    }
    
    /**
     * Marks tutorial as completed
     */
    fun markCompleted(): TutorialData {
        return copy(
            isCompleted = true,
            steps = steps.map { it.markCompleted() }
        )
    }
    
    /**
     * Resets tutorial progress
     */
    fun resetProgress(): TutorialData {
        return copy(
            isCompleted = false,
            steps = steps.map { it.resetCompletion() }
        )
    }
}

/**
 * Tutorial difficulty levels
 */
enum class TutorialDifficulty(val displayName: String, val color: Long) {
    BEGINNER("Başlangıç", 0xFF4CAF50),      // Green
    INTERMEDIATE("Orta", 0xFFFF9800),        // Orange  
    ADVANCED("İleri", 0xFFE91E63)            // Pink
}

/**
 * Tutorial preferences for tracking user progress
 */
data class TutorialPreferences(
    val hasSeenWelcome: Boolean = false,
    val hasCompletedOnboarding: Boolean = false,
    val completedTutorials: List<String> = emptyList(),
    val skippedTutorials: List<String> = emptyList(),
    val showHints: Boolean = true,
    val autoPlayTutorials: Boolean = false,
    val tutorialSpeed: TutorialSpeed = TutorialSpeed.NORMAL
) {
    
    /**
     * Checks if tutorial is completed
     */
    fun isTutorialCompleted(tutorialId: String): Boolean {
        return completedTutorials.contains(tutorialId)
    }
    
    /**
     * Checks if tutorial is skipped
     */
    fun isTutorialSkipped(tutorialId: String): Boolean {
        return skippedTutorials.contains(tutorialId)
    }
    
    /**
     * Marks tutorial as completed
     */
    fun markTutorialCompleted(tutorialId: String): TutorialPreferences {
        return copy(
            completedTutorials = (completedTutorials + tutorialId).distinct(),
            skippedTutorials = skippedTutorials - tutorialId
        )
    }
    
    /**
     * Marks tutorial as skipped
     */
    fun markTutorialSkipped(tutorialId: String): TutorialPreferences {
        return copy(
            skippedTutorials = (skippedTutorials + tutorialId).distinct(),
            completedTutorials = completedTutorials - tutorialId
        )
    }
    
    /**
     * Resets tutorial progress
     */
    fun resetProgress(): TutorialPreferences {
        return copy(
            completedTutorials = emptyList(),
            skippedTutorials = emptyList()
        )
    }
}

/**
 * Tutorial playback speed
 */
enum class TutorialSpeed(val displayName: String, val multiplier: Float) {
    SLOW("Yavaş", 1.5f),
    NORMAL("Normal", 1.0f),
    FAST("Hızlı", 0.7f)
}

/**
 * Tutorial configuration
 */
data class TutorialConfig(
    val enableGuidedTour: Boolean = true,
    val enableTooltips: Boolean = true,
    val enableHelpBadges: Boolean = true,
    val showProgressBar: Boolean = true,
    val allowSkipping: Boolean = true,
    val enableVoiceOver: Boolean = false,
    val darkModeSupport: Boolean = true
) 